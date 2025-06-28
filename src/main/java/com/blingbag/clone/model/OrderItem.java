package com.blingbag.clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "order_items")
public class OrderItem {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;
    
    @Column(nullable = false)
    private Integer quantity;
    
    @Column(nullable = false)
    private BigDecimal price; // Price at the time of order
    
    private BigDecimal discountedPrice;
    
    @Column(length = 100)
    private String selectedColor;
    
    @Column(length = 100)
    private String selectedSize;
    
    // Additional jewelry-specific fields
    private String materialType;
    
    @Column(name = "weight_in_grams")
    private Double weightInGrams;
    
    // Calculated fields
    @Column(nullable = false)
    private BigDecimal subtotal; // price * quantity
    
    private BigDecimal discountAmount;
    
    @Column(nullable = false)
    private BigDecimal total; // subtotal - discountAmount
    
    // Helper methods
    @PrePersist
    @PreUpdate
    protected void calculateTotals() {
        if (quantity != null && price != null) {
            this.subtotal = price.multiply(new BigDecimal(quantity));
            
            // Calculate discount if applicable
            if (discountedPrice != null) {
                BigDecimal regularTotal = price.multiply(new BigDecimal(quantity));
                BigDecimal discountedTotal = discountedPrice.multiply(new BigDecimal(quantity));
                this.discountAmount = regularTotal.subtract(discountedTotal);
                this.total = discountedTotal;
            } else {
                this.discountAmount = BigDecimal.ZERO;
                this.total = this.subtotal;
            }
        }
    }
    
    // Method to copy product details at the time of order
    public void copyProductDetails(Product product) {
        this.price = product.getPrice();
        this.discountedPrice = product.getDiscountedPrice();
        this.materialType = product.getMaterialType();
        this.weightInGrams = product.getWeightInGrams();
    }
    
    // Override toString to prevent infinite recursion
    @Override
    public String toString() {
        return "OrderItem{" +
                "id=" + id +
                ", quantity=" + quantity +
                ", price=" + price +
                ", discountedPrice=" + discountedPrice +
                ", subtotal=" + subtotal +
                ", total=" + total +
                '}';
    }
}
