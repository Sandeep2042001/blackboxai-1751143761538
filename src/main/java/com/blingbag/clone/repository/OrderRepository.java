package com.blingbag.clone.repository;

import com.blingbag.clone.model.Order;
import com.blingbag.clone.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface OrderRepository extends JpaRepository<Order, Long> {
    
    // Basic finder methods
    Optional<Order> findByOrderNumber(String orderNumber);
    
    List<Order> findByUser(User user);
    
    Page<Order> findByUser(User user, Pageable pageable);
    
    // Find orders by status
    List<Order> findByStatus(Order.OrderStatus status);
    
    Page<Order> findByStatus(Order.OrderStatus status, Pageable pageable);
    
    // Find orders by payment method
    List<Order> findByPaymentMethod(Order.PaymentMethod paymentMethod);
    
    // Find orders by payment status
    List<Order> findByPaymentStatus(Order.PaymentStatus paymentStatus);
    
    // Find orders by date range
    List<Order> findByOrderDateBetween(LocalDateTime startDate, LocalDateTime endDate);
    
    // Find orders by shipping city/state
    List<Order> findByShippingCity(String city);
    
    List<Order> findByShippingState(String state);
    
    // Find orders with tracking number
    List<Order> findByTrackingNumberIsNotNull();
    
    // Find orders by total amount range
    List<Order> findByTotalBetween(BigDecimal minTotal, BigDecimal maxTotal);
    
    // Find orders with specific coupon code
    List<Order> findByCouponCode(String couponCode);
    
    // Custom queries for analytics and reporting
    
    // Count orders by status
    Long countByStatus(Order.OrderStatus status);
    
    // Sum of total orders by date range
    @Query("SELECT SUM(o.total) FROM Order o WHERE o.orderDate BETWEEN :startDate AND :endDate")
    BigDecimal sumTotalByDateRange(
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
    
    // Count orders by payment method
    Long countByPaymentMethod(Order.PaymentMethod paymentMethod);
    
    // Find recent orders
    @Query("SELECT o FROM Order o ORDER BY o.orderDate DESC")
    List<Order> findRecentOrders(Pageable pageable);
    
    // Search orders by multiple criteria
    @Query("SELECT o FROM Order o WHERE " +
           "(:status IS NULL OR o.status = :status) " +
           "AND (:paymentMethod IS NULL OR o.paymentMethod = :paymentMethod) " +
           "AND (:paymentStatus IS NULL OR o.paymentStatus = :paymentStatus) " +
           "AND (:startDate IS NULL OR o.orderDate >= :startDate) " +
           "AND (:endDate IS NULL OR o.orderDate <= :endDate) " +
           "AND (:minTotal IS NULL OR o.total >= :minTotal) " +
           "AND (:maxTotal IS NULL OR o.total <= :maxTotal)")
    Page<Order> searchOrders(
            @Param("status") Order.OrderStatus status,
            @Param("paymentMethod") Order.PaymentMethod paymentMethod,
            @Param("paymentStatus") Order.PaymentStatus paymentStatus,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate,
            @Param("minTotal") BigDecimal minTotal,
            @Param("maxTotal") BigDecimal maxTotal,
            Pageable pageable
    );
    
    // Find unshipped orders
    @Query("SELECT o FROM Order o WHERE o.status = 'CONFIRMED' AND o.trackingNumber IS NULL")
    List<Order> findUnshippedOrders();
    
    // Find orders pending delivery
    @Query("SELECT o FROM Order o WHERE o.status = 'SHIPPED' AND o.deliveredDate IS NULL")
    List<Order> findOrdersPendingDelivery();
    
    // Get daily order statistics
    @Query("SELECT NEW map(COUNT(o) as orderCount, SUM(o.total) as totalAmount) " +
           "FROM Order o " +
           "WHERE DATE(o.orderDate) = CURRENT_DATE")
    Object getDailyOrderStats();
    
    // Get monthly order statistics
    @Query("SELECT NEW map(MONTH(o.orderDate) as month, COUNT(o) as orderCount, SUM(o.total) as totalAmount) " +
           "FROM Order o " +
           "WHERE YEAR(o.orderDate) = :year " +
           "GROUP BY MONTH(o.orderDate)")
    List<Object> getMonthlyOrderStats(@Param("year") int year);
    
    // Find orders with specific product
    @Query("SELECT DISTINCT o FROM Order o JOIN o.orderItems oi WHERE oi.product.id = :productId")
    List<Order> findOrdersContainingProduct(@Param("productId") Long productId);
    
    // Get average order value
    @Query("SELECT AVG(o.total) FROM Order o WHERE o.status != 'CANCELLED'")
    BigDecimal getAverageOrderValue();
    
    // Find orders requiring follow-up (e.g., pending for long time)
    @Query("SELECT o FROM Order o WHERE o.status = 'PENDING' AND o.orderDate < :cutoffDate")
    List<Order> findOrdersRequiringFollowUp(@Param("cutoffDate") LocalDateTime cutoffDate);
}
