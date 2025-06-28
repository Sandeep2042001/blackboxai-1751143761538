package com.blingbag.clone.repository;

import com.blingbag.clone.model.Product;
import com.blingbag.clone.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    
    // Basic finder methods
    Optional<Product> findBySku(String sku);
    
    List<Product> findByCategory(Category category);
    
    Page<Product> findByCategory(Category category, Pageable pageable);
    
    // Featured products
    List<Product> findByIsFeaturedTrue();
    
    Page<Product> findByIsFeaturedTrue(Pageable pageable);
    
    // New arrivals
    List<Product> findByIsNewArrivalTrue();
    
    Page<Product> findByIsNewArrivalTrue(Pageable pageable);
    
    // Search by name or description
    @Query("SELECT p FROM Product p WHERE " +
           "LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    Page<Product> searchProducts(@Param("keyword") String keyword, Pageable pageable);
    
    // Filter by price range
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);
    
    // Filter by material type
    Page<Product> findByMaterialType(String materialType, Pageable pageable);
    
    // Filter by color
    @Query("SELECT DISTINCT p FROM Product p JOIN p.colors c WHERE LOWER(c) = LOWER(:color)")
    Page<Product> findByColor(@Param("color") String color, Pageable pageable);
    
    // Find products by multiple categories
    @Query("SELECT p FROM Product p WHERE p.category IN :categories")
    Page<Product> findByCategories(@Param("categories") List<Category> categories, Pageable pageable);
    
    // Find products by category and price range
    Page<Product> findByCategoryAndPriceBetween(
            Category category, 
            BigDecimal minPrice, 
            BigDecimal maxPrice, 
            Pageable pageable
    );
    
    // Find active products
    Page<Product> findByIsActiveTrue(Pageable pageable);
    
    // Find products with stock
    Page<Product> findByStockQuantityGreaterThan(Integer minStock, Pageable pageable);
    
    // Complex search with multiple criteria
    @Query("SELECT DISTINCT p FROM Product p " +
           "WHERE (:categoryId IS NULL OR p.category.id = :categoryId) " +
           "AND (:minPrice IS NULL OR p.price >= :minPrice) " +
           "AND (:maxPrice IS NULL OR p.price <= :maxPrice) " +
           "AND (:materialType IS NULL OR p.materialType = :materialType) " +
           "AND (p.isActive = true) " +
           "AND (:keyword IS NULL OR LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%')) " +
           "OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Product> searchProductsWithFilters(
            @Param("categoryId") Long categoryId,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("materialType") String materialType,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    
    // Count products by category
    Long countByCategory(Category category);
    
    // Count active products
    Long countByIsActiveTrue();
}
