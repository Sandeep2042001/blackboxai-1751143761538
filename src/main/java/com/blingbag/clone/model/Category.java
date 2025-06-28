package com.blingbag.clone.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "categories")
public class Category {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false, unique = true)
    private String name;
    
    @Column(length = 1000)
    private String description;
    
    private String imageUrl;
    
    @Column(nullable = false)
    private boolean isActive = true;
    
    @Column(name = "display_order")
    private Integer displayOrder;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent;
    
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL)
    private List<Category> subcategories = new ArrayList<>();
    
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    private List<Product> products = new ArrayList<>();
    
    // Custom fields for Blingbag categories
    private String slug; // URL-friendly version of name
    
    @Column(name = "meta_title")
    private String metaTitle;
    
    @Column(name = "meta_description")
    private String metaDescription;
    
    @Column(name = "banner_image")
    private String bannerImage;
    
    @Column(name = "is_featured")
    private boolean isFeatured;
    
    // Fields specific to jewelry categories
    @Column(name = "material_type")
    private String materialType; // e.g., "Kundan", "American Diamond", "Oxidised"
    
    @Column(name = "collection_type")
    private String collectionType; // e.g., "Bridal", "Traditional", "Modern"
    
    // Helper methods
    public void addSubcategory(Category subcategory) {
        subcategories.add(subcategory);
        subcategory.setParent(this);
    }
    
    public void removeSubcategory(Category subcategory) {
        subcategories.remove(subcategory);
        subcategory.setParent(null);
    }
    
    public boolean isMainCategory() {
        return parent == null;
    }
    
    public boolean hasSubcategories() {
        return !subcategories.isEmpty();
    }
    
    // Override toString to prevent infinite recursion
    @Override
    public String toString() {
        return "Category{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", isActive=" + isActive +
                ", displayOrder=" + displayOrder +
                '}';
    }
}
