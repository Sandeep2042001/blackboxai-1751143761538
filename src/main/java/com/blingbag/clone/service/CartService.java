package com.blingbag.clone.service;

import com.blingbag.clone.model.Order;
import com.blingbag.clone.model.OrderItem;
import com.blingbag.clone.model.Product;
import com.blingbag.clone.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.servlet.http.HttpSession;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class CartService {

    private final ProductService productService;
    private final OrderService orderService;
    private static final String CART_SESSION_KEY = "shopping_cart";
    private static final String CART_COUPON_KEY = "cart_coupon";

    // Cart item structure
    public static class CartItem {
        private Product product;
        private int quantity;
        private String selectedColor;
        private BigDecimal price;
        private BigDecimal total;

        public CartItem(Product product, int quantity, String selectedColor) {
            this.product = product;
            this.quantity = quantity;
            this.selectedColor = selectedColor;
            this.price = product.getDiscountedPrice() != null ? 
                        product.getDiscountedPrice() : 
                        product.getPrice();
            this.total = this.price.multiply(new BigDecimal(quantity));
        }

        // Getters and setters
        public Product getProduct() { return product; }
        public int getQuantity() { return quantity; }
        public String getSelectedColor() { return selectedColor; }
        public BigDecimal getPrice() { return price; }
        public BigDecimal getTotal() { return total; }

        public void setQuantity(int quantity) {
            this.quantity = quantity;
            this.total = this.price.multiply(new BigDecimal(quantity));
        }
    }

    // Cart operations
    @SuppressWarnings("unchecked")
    public void addToCart(HttpSession session, Long productId, int quantity, String selectedColor) {
        Product product = productService.getProductById(productId);
        
        // Validate stock
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            cart = new HashMap<>();
        }

        CartItem existingItem = cart.get(productId);
        if (existingItem != null) {
            // Update existing item
            int newQuantity = existingItem.getQuantity() + quantity;
            if (product.getStockQuantity() < newQuantity) {
                throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
            }
            existingItem.setQuantity(newQuantity);
        } else {
            // Add new item
            cart.put(productId, new CartItem(product, quantity, selectedColor));
        }

        session.setAttribute(CART_SESSION_KEY, cart);
    }

    @SuppressWarnings("unchecked")
    public void updateCartItemQuantity(HttpSession session, Long productId, int quantity) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null || !cart.containsKey(productId)) {
            throw new IllegalArgumentException("Product not found in cart");
        }

        Product product = productService.getProductById(productId);
        if (product.getStockQuantity() < quantity) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }

        if (quantity <= 0) {
            cart.remove(productId);
        } else {
            cart.get(productId).setQuantity(quantity);
        }

        session.setAttribute(CART_SESSION_KEY, cart);
    }

    @SuppressWarnings("unchecked")
    public void removeFromCart(HttpSession session, Long productId) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart != null) {
            cart.remove(productId);
            session.setAttribute(CART_SESSION_KEY, cart);
        }
    }

    @SuppressWarnings("unchecked")
    public void clearCart(HttpSession session) {
        session.removeAttribute(CART_SESSION_KEY);
        session.removeAttribute(CART_COUPON_KEY);
    }

    // Cart retrieval
    @SuppressWarnings("unchecked")
    public List<CartItem> getCartItems(HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(CART_SESSION_KEY);
        return cart != null ? new ArrayList<>(cart.values()) : new ArrayList<>();
    }

    // Cart calculations
    @SuppressWarnings("unchecked")
    public BigDecimal getSubtotal(HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null) {
            return BigDecimal.ZERO;
        }

        return cart.values().stream()
                .map(CartItem::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getShippingCost(HttpSession session) {
        BigDecimal subtotal = getSubtotal(session);
        // Free shipping for orders above Rs. 500
        return subtotal.compareTo(new BigDecimal("500")) >= 0 ? 
               BigDecimal.ZERO : 
               new BigDecimal("50"); // Standard shipping cost
    }

    @SuppressWarnings("unchecked")
    public BigDecimal getDiscount(HttpSession session) {
        String couponCode = (String) session.getAttribute(CART_COUPON_KEY);
        if (couponCode == null) {
            return BigDecimal.ZERO;
        }

        BigDecimal subtotal = getSubtotal(session);
        // Apply discount based on coupon code
        // This is a simplified example - implement actual coupon logic
        String code = couponCode.toUpperCase();
        if ("FIRST10".equals(code)) {
            return subtotal.multiply(new BigDecimal("0.10"));
        } else if ("SUMMER20".equals(code)) {
            return subtotal.multiply(new BigDecimal("0.20"));
        } else {
            return BigDecimal.ZERO;
        }
    }

    public BigDecimal getTotal(HttpSession session) {
        BigDecimal subtotal = getSubtotal(session);
        BigDecimal shipping = getShippingCost(session);
        BigDecimal discount = getDiscount(session);
        
        return subtotal.add(shipping).subtract(discount);
    }

    // Coupon management
    public void applyCoupon(HttpSession session, String couponCode) {
        // Validate coupon code here
        // This is a simplified example - implement actual coupon validation
        if (couponCode != null && !couponCode.trim().isEmpty()) {
            String code = couponCode.trim().toUpperCase();
            if ("FIRST10".equals(code) || "SUMMER20".equals(code)) {
                session.setAttribute(CART_COUPON_KEY, code);
            } else {
                throw new IllegalArgumentException("Invalid coupon code");
            }
        }
    }

    public void removeCoupon(HttpSession session) {
        session.removeAttribute(CART_COUPON_KEY);
    }

    // Cart validation
    @SuppressWarnings("unchecked")
    public void validateCart(HttpSession session) {
        Map<Long, CartItem> cart = (Map<Long, CartItem>) session.getAttribute(CART_SESSION_KEY);
        if (cart == null || cart.isEmpty()) {
            throw new IllegalStateException("Cart is empty");
        }

        // Validate each item
        for (CartItem item : cart.values()) {
            Product product = productService.getProductById(item.getProduct().getId());
            if (!product.isActive()) {
                throw new IllegalStateException("Product is no longer available: " + product.getName());
            }
            if (product.getStockQuantity() < item.getQuantity()) {
                throw new IllegalStateException("Insufficient stock for product: " + product.getName());
            }
        }
    }

    // Checkout process
    @Transactional
    public Order checkout(HttpSession session, User user, String shippingAddress, 
                        Order.PaymentMethod paymentMethod) {
        validateCart(session);
        List<CartItem> cartItems = getCartItems(session);

        Order order = new Order();
        order.setUser(user);
        order.setPaymentMethod(paymentMethod);
        
        // Set shipping address
        String[] addressParts = shippingAddress.split(",");
        order.setShippingAddressLine1(addressParts[0].trim());
        if (addressParts.length > 4) {
            order.setShippingAddressLine2(addressParts[1].trim());
            order.setShippingCity(addressParts[2].trim());
            order.setShippingState(addressParts[3].trim());
            order.setShippingPostalCode(addressParts[4].trim());
        }
        order.setShippingCountry("India"); // Default for this implementation

        // Add items to order
        for (CartItem cartItem : cartItems) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setPrice(cartItem.getPrice());
            orderItem.setSelectedColor(cartItem.getSelectedColor());
            order.addOrderItem(orderItem);
        }

        // Set order totals
        order.setSubtotal(getSubtotal(session));
        order.setShippingCost(getShippingCost(session));
        order.setDiscountAmount(getDiscount(session));
        order.setTotal(getTotal(session));

        // Save order
        Order savedOrder = orderService.createOrder(order);

        // Clear cart after successful checkout
        clearCart(session);

        return savedOrder;
    }
}
