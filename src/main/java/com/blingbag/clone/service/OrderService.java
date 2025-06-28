package com.blingbag.clone.service;

import com.blingbag.clone.model.Order;
import com.blingbag.clone.model.OrderItem;
import com.blingbag.clone.model.Product;
import com.blingbag.clone.model.User;
import com.blingbag.clone.repository.OrderRepository;
import com.blingbag.clone.repository.OrderItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductService productService;
    private final UserService userService;

    // Create operations
    public Order createOrder(Order order) {
        validateOrder(order);
        order.setOrderNumber(generateOrderNumber());
        order.setStatus(Order.OrderStatus.PENDING);
        order.calculateTotals();
        
        // Update product stock
        order.getOrderItems().forEach(item -> {
            productService.updateStock(item.getProduct().getId(), -item.getQuantity());
            item.copyProductDetails(item.getProduct());
        });
        
        return orderRepository.save(order);
    }

    // Read operations
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Page<Order> getAllOrders(Pageable pageable) {
        return orderRepository.findAll(pageable);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Order not found with id: " + id));
    }

    public Optional<Order> findByOrderNumber(String orderNumber) {
        return orderRepository.findByOrderNumber(orderNumber);
    }

    public List<Order> getOrdersByUser(User user) {
        return orderRepository.findByUser(user);
    }

    // Update operations
    public Order updateOrderStatus(Long id, Order.OrderStatus newStatus) {
        Order order = getOrderById(id);
        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);
        
        // Handle status-specific actions
        switch (newStatus) {
            case SHIPPED:
                order.setShippedDate(LocalDateTime.now());
                break;
            case DELIVERED:
                order.setDeliveredDate(LocalDateTime.now());
                break;
            case CANCELLED:
                // Restore product stock
                order.getOrderItems().forEach(item -> 
                    productService.updateStock(item.getProduct().getId(), item.getQuantity())
                );
                break;
        }
        
        return orderRepository.save(order);
    }

    // Payment operations
    public Order updatePaymentStatus(Long id, Order.PaymentStatus newStatus) {
        Order order = getOrderById(id);
        order.setPaymentStatus(newStatus);
        return orderRepository.save(order);
    }

    // Shipping operations
    public Order updateTrackingNumber(Long id, String trackingNumber) {
        Order order = getOrderById(id);
        order.setTrackingNumber(trackingNumber);
        return orderRepository.save(order);
    }

    // Search and filter operations
    public List<Order> findOrdersByStatus(Order.OrderStatus status) {
        return orderRepository.findByStatus(status);
    }

    public List<Order> findOrdersByPaymentMethod(Order.PaymentMethod paymentMethod) {
        return orderRepository.findByPaymentMethod(paymentMethod);
    }

    public List<Order> findOrdersByDateRange(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.findByOrderDateBetween(startDate, endDate);
    }

    // Advanced search
    public Page<Order> searchOrders(
            Order.OrderStatus status,
            Order.PaymentMethod paymentMethod,
            Order.PaymentStatus paymentStatus,
            LocalDateTime startDate,
            LocalDateTime endDate,
            BigDecimal minTotal,
            BigDecimal maxTotal,
            Pageable pageable) {
        return orderRepository.searchOrders(
                status, paymentMethod, paymentStatus, startDate, endDate, minTotal, maxTotal, pageable);
    }

    // Analytics
    public BigDecimal getTotalRevenue(LocalDateTime startDate, LocalDateTime endDate) {
        return orderRepository.sumTotalByDateRange(startDate, endDate);
    }

    public Long getOrderCount(Order.OrderStatus status) {
        return orderRepository.countByStatus(status);
    }

    public List<Order> getRecentOrders(int limit) {
        return orderRepository.findRecentOrders(Pageable.ofSize(limit));
    }

    // Shipping management
    public List<Order> findUnshippedOrders() {
        return orderRepository.findUnshippedOrders();
    }

    public List<Order> findOrdersPendingDelivery() {
        return orderRepository.findOrdersPendingDelivery();
    }

    // Order statistics
    public Object getDailyOrderStats() {
        return orderRepository.getDailyOrderStats();
    }

    public List<Object> getMonthlyOrderStats(int year) {
        return orderRepository.getMonthlyOrderStats(year);
    }

    // Validation methods
    private void validateOrder(Order order) {
        if (order.getUser() == null) {
            throw new IllegalArgumentException("Order must have a user");
        }
        if (order.getOrderItems() == null || order.getOrderItems().isEmpty()) {
            throw new IllegalArgumentException("Order must have at least one item");
        }
        validateShippingAddress(order);
        validateOrderItems(order.getOrderItems());
    }

    private void validateShippingAddress(Order order) {
        if (order.getShippingAddressLine1() == null || order.getShippingAddressLine1().trim().isEmpty() ||
            order.getShippingCity() == null || order.getShippingCity().trim().isEmpty() ||
            order.getShippingState() == null || order.getShippingState().trim().isEmpty() ||
            order.getShippingPostalCode() == null || order.getShippingPostalCode().trim().isEmpty() ||
            order.getShippingCountry() == null || order.getShippingCountry().trim().isEmpty()) {
            throw new IllegalArgumentException("Shipping address is incomplete");
        }
    }

    private void validateOrderItems(List<OrderItem> items) {
        items.forEach(item -> {
            Product product = productService.getProductById(item.getProduct().getId());
            if (!product.isActive()) {
                throw new IllegalArgumentException("Product is not active: " + product.getName());
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }
        });
    }

    private void validateStatusTransition(Order.OrderStatus currentStatus, Order.OrderStatus newStatus) {
        // Define valid status transitions
        switch (currentStatus) {
            case PENDING:
                if (newStatus != Order.OrderStatus.CONFIRMED && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from PENDING to " + newStatus);
                }
                break;
            case CONFIRMED:
                if (newStatus != Order.OrderStatus.PROCESSING && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from CONFIRMED to " + newStatus);
                }
                break;
            case PROCESSING:
                if (newStatus != Order.OrderStatus.SHIPPED && newStatus != Order.OrderStatus.CANCELLED) {
                    throw new IllegalStateException("Invalid status transition from PROCESSING to " + newStatus);
                }
                break;
            case SHIPPED:
                if (newStatus != Order.OrderStatus.DELIVERED) {
                    throw new IllegalStateException("Invalid status transition from SHIPPED to " + newStatus);
                }
                break;
            case DELIVERED:
                if (newStatus != Order.OrderStatus.RETURNED) {
                    throw new IllegalStateException("Invalid status transition from DELIVERED to " + newStatus);
                }
                break;
            case CANCELLED:
            case RETURNED:
                throw new IllegalStateException("Cannot transition from " + currentStatus + " status");
        }
    }

    // Helper methods
    private String generateOrderNumber() {
        return "ORD-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }

    // Order follow-up
    public List<Order> findOrdersRequiringFollowUp() {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(7);
        return orderRepository.findOrdersRequiringFollowUp(cutoffDate);
    }

    // Product-specific order history
    public List<Order> findOrdersContainingProduct(Long productId) {
        return orderRepository.findOrdersContainingProduct(productId);
    }

    // Order metrics
    public BigDecimal getAverageOrderValue() {
        return orderRepository.getAverageOrderValue();
    }
}
