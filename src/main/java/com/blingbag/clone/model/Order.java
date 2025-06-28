package com.blingbag.clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "orders")
public class Order {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(unique = true, nullable = false)
    private String orderNumber;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;
    
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems = new ArrayList<>();
    
    @Column(nullable = false)
    private LocalDateTime orderDate;
    
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus status;
    
    @Column(nullable = false)
    private BigDecimal subtotal;
    
    private BigDecimal discountAmount;
    
    @Column(nullable = false)
    private BigDecimal shippingCost;
    
    @Column(nullable = false)
    private BigDecimal tax;
    
    @Column(nullable = false)
    private BigDecimal total;
    
    private String couponCode;
    
    @Column(length = 1000)
    private String orderNotes;
    
    // Payment Information
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PaymentMethod paymentMethod;
    
    private String paymentTransactionId;
    
    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;
    
    // Shipping Information
    @Column(nullable = false)
    private String shippingAddressLine1;
    
    private String shippingAddressLine2;
    
    @Column(nullable = false)
    private String shippingCity;
    
    @Column(nullable = false)
    private String shippingState;
    
    @Column(nullable = false)
    private String shippingPostalCode;
    
    @Column(nullable = false)
    private String shippingCountry;
    
    private String trackingNumber;
    
    private LocalDateTime shippedDate;
    
    private LocalDateTime deliveredDate;
    
    @PrePersist
    protected void onCreate() {
        orderDate = LocalDateTime.now();
    }
    
    // Helper methods
    public void addOrderItem(OrderItem item) {
        orderItems.add(item);
        item.setOrder(this);
    }
    
    public void removeOrderItem(OrderItem item) {
        orderItems.remove(item);
        item.setOrder(null);
    }
    
    public void calculateTotals() {
        this.subtotal = orderItems.stream()
                .map(item -> item.getPrice().multiply(new BigDecimal(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
                
        // Apply discount if coupon code exists
        this.discountAmount = BigDecimal.ZERO; // Calculate based on coupon logic
        
        // Calculate tax (assuming 18% GST)
        this.tax = this.subtotal.multiply(new BigDecimal("0.18"));
        
        // Calculate total
        this.total = this.subtotal
                .add(this.shippingCost)
                .add(this.tax)
                .subtract(this.discountAmount);
    }
    
    public enum OrderStatus {
        PENDING,
        CONFIRMED,
        PROCESSING,
        SHIPPED,
        DELIVERED,
        CANCELLED,
        RETURNED
    }
    
    public enum PaymentMethod {
        CASH_ON_DELIVERY,
        CREDIT_CARD,
        DEBIT_CARD,
        UPI,
        NET_BANKING,
        WALLET
    }
    
    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED
    }
}
