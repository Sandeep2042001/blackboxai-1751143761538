package com.blingbag.clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "products")
public class Product {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String name;
    
    @Column(length = 2000)
    private String description;
    
    @Column(nullable = false)
    private BigDecimal price;
    
    @Column(nullable = false)
    private BigDecimal discountedPrice;
    
    private String imageUrl;
    
    @Column(nullable = false)
    private Integer stockQuantity;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    
    @ElementCollection
    @CollectionTable(name = "product_colors", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "color")
    private Set<String> colors = new HashSet<>();
    
    private String sku;
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    @Column(nullable = false)
    private boolean isFeatured = false;
    
    @Column(nullable = false)
    private boolean isNewArrival = false;
    
    @Column(name = "material_type")
    private String materialType;
    
    @Column(name = "weight_in_grams")
    private Double weightInGrams;
    
    @ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(name = "image_url")
    private Set<String> additionalImages = new HashSet<>();
}
