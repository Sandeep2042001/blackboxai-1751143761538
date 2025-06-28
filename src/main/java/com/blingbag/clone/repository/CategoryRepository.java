package com.blingbag.clone.repository;

import com.blingbag.clone.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    // Basic finder methods
    Optional<Category> findByName(String name);
    
    Optional<Category> findBySlug(String slug);
    
    // Find main categories (categories without parent)
    List<Category> findByParentIsNull();
    
    // Find subcategories of a parent category
    List<Category> findByParentId(Long parentId);
    
    // Find featured categories
    List<Category> findByIsFeaturedTrue();
    
    // Find active categories
    List<Category> findByIsActiveTrue();
    
    // Find categories by material type
    List<Category> findByMaterialType(String materialType);
    
    // Find categories by collection type
    List<Category> findByCollectionType(String collectionType);
    
    // Find categories ordered by display order
    List<Category> findAllByOrderByDisplayOrderAsc();
    
    // Find active categories ordered by display order
    List<Category> findByIsActiveTrueOrderByDisplayOrderAsc();
    
    // Search categories by name
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%'))")
    List<Category> searchCategories(@Param("keyword") String keyword);
    
    // Find categories with products
    @Query("SELECT DISTINCT c FROM Category c JOIN c.products p WHERE p.isActive = true")
    List<Category> findCategoriesWithActiveProducts();
    
    // Count subcategories
    Long countByParentId(Long parentId);
    
    // Find categories by multiple collection types
    @Query("SELECT c FROM Category c WHERE c.collectionType IN :collectionTypes")
    List<Category> findByCollectionTypes(@Param("collectionTypes") List<String> collectionTypes);
    
    // Find categories with specific material and collection type
    List<Category> findByMaterialTypeAndCollectionType(String materialType, String collectionType);
    
    // Find categories with pagination
    Page<Category> findByIsActiveTrue(Pageable pageable);
    
    // Complex category search with filters
    @Query("SELECT c FROM Category c " +
           "WHERE (:materialType IS NULL OR c.materialType = :materialType) " +
           "AND (:collectionType IS NULL OR c.collectionType = :collectionType) " +
           "AND (c.isActive = true) " +
           "AND (:keyword IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Category> searchCategoriesWithFilters(
            @Param("materialType") String materialType,
            @Param("collectionType") String collectionType,
            @Param("keyword") String keyword,
            Pageable pageable
    );
    
    // Find all parent categories with their immediate subcategories
    @Query("SELECT DISTINCT c FROM Category c LEFT JOIN FETCH c.subcategories WHERE c.parent IS NULL")
    List<Category> findAllParentCategoriesWithSubcategories();
    
    // Find categories by multiple material types
    @Query("SELECT c FROM Category c WHERE c.materialType IN :materialTypes")
    List<Category> findByMaterialTypes(@Param("materialTypes") List<String> materialTypes);
    
    // Find featured categories with active products
    @Query("SELECT DISTINCT c FROM Category c JOIN c.products p " +
           "WHERE c.isFeatured = true AND p.isActive = true")
    List<Category> findFeaturedCategoriesWithActiveProducts();
    
    // Find categories by display order range
    List<Category> findByDisplayOrderBetweenOrderByDisplayOrderAsc(
            Integer startOrder, 
            Integer endOrder
    );
}
