package com.blingbag.clone.service;

import com.blingbag.clone.model.Category;
import com.blingbag.clone.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class CategoryService {

    private final CategoryRepository categoryRepository;

    // Create operations
    public Category createCategory(Category category) {
        validateCategory(category);
        // Generate slug from name if not provided
        if (!StringUtils.hasText(category.getSlug())) {
            category.setSlug(generateSlug(category.getName()));
        }
        return categoryRepository.save(category);
    }

    // Read operations
    public List<Category> getAllCategories() {
        return categoryRepository.findAll();
    }

    public Page<Category> getAllCategories(Pageable pageable) {
        return categoryRepository.findAll(pageable);
    }

    public Category getCategoryById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + id));
    }

    public Optional<Category> findBySlug(String slug) {
        return categoryRepository.findBySlug(slug);
    }

    // Update operations
    public Category updateCategory(Long id, Category categoryDetails) {
        Category category = getCategoryById(id);
        updateCategoryFields(category, categoryDetails);
        return categoryRepository.save(category);
    }

    // Delete operations
    public void deleteCategory(Long id) {
        Category category = getCategoryById(id);
        if (!category.getProducts().isEmpty()) {
            throw new IllegalStateException("Cannot delete category with associated products");
        }
        categoryRepository.delete(category);
    }

    // Hierarchical operations
    public List<Category> getMainCategories() {
        return categoryRepository.findByParentIsNull();
    }

    public List<Category> getSubcategories(Long parentId) {
        return categoryRepository.findByParentId(parentId);
    }

    public void addSubcategory(Long parentId, Category subcategory) {
        Category parent = getCategoryById(parentId);
        subcategory.setParent(parent);
        categoryRepository.save(subcategory);
    }

    // Featured categories
    public List<Category> getFeaturedCategories() {
        return categoryRepository.findByIsFeaturedTrue();
    }

    // Active categories
    public List<Category> getActiveCategories() {
        return categoryRepository.findByIsActiveTrue();
    }

    // Material type operations
    public List<Category> getCategoriesByMaterialType(String materialType) {
        return categoryRepository.findByMaterialType(materialType);
    }

    // Collection type operations
    public List<Category> getCategoriesByCollectionType(String collectionType) {
        return categoryRepository.findByCollectionType(collectionType);
    }

    // Search operations
    public List<Category> searchCategories(String keyword) {
        return categoryRepository.searchCategories(keyword);
    }

    // Categories with products
    public List<Category> getCategoriesWithActiveProducts() {
        return categoryRepository.findCategoriesWithActiveProducts();
    }

    // Advanced search
    public Page<Category> searchCategoriesWithFilters(
            String materialType,
            String collectionType,
            String keyword,
            Pageable pageable) {
        return categoryRepository.searchCategoriesWithFilters(
                materialType, collectionType, keyword, pageable);
    }

    // Validation methods
    private void validateCategory(Category category) {
        if (!StringUtils.hasText(category.getName())) {
            throw new IllegalArgumentException("Category name cannot be empty");
        }
        
        // Check for duplicate names
        Optional<Category> existingCategory = categoryRepository.findByName(category.getName());
        if (existingCategory.isPresent() && !existingCategory.get().getId().equals(category.getId())) {
            throw new IllegalArgumentException("Category name already exists");
        }

        // Validate parent category if specified
        if (category.getParent() != null) {
            validateParentCategory(category);
        }
    }

    private void validateParentCategory(Category category) {
        // Prevent circular references
        Category parent = category.getParent();
        while (parent != null) {
            if (parent.getId().equals(category.getId())) {
                throw new IllegalArgumentException("Circular reference detected in category hierarchy");
            }
            parent = parent.getParent();
        }
    }

    // Helper methods
    private void updateCategoryFields(Category existingCategory, Category updatedCategory) {
        existingCategory.setName(updatedCategory.getName());
        existingCategory.setDescription(updatedCategory.getDescription());
        existingCategory.setImageUrl(updatedCategory.getImageUrl());
        existingCategory.setActive(updatedCategory.isActive());
        existingCategory.setDisplayOrder(updatedCategory.getDisplayOrder());
        existingCategory.setParent(updatedCategory.getParent());
        existingCategory.setSlug(updatedCategory.getSlug());
        existingCategory.setMetaTitle(updatedCategory.getMetaTitle());
        existingCategory.setMetaDescription(updatedCategory.getMetaDescription());
        existingCategory.setBannerImage(updatedCategory.getBannerImage());
        existingCategory.setFeatured(updatedCategory.isFeatured());
        existingCategory.setMaterialType(updatedCategory.getMaterialType());
        existingCategory.setCollectionType(updatedCategory.getCollectionType());
    }

    private String generateSlug(String name) {
        return name.toLowerCase()
                .replaceAll("[^a-z0-9\\s-]", "") // Remove all non-alphanumeric characters except spaces and hyphens
                .replaceAll("\\s+", "-")         // Replace spaces with hyphens
                .replaceAll("-+", "-")           // Replace multiple hyphens with single hyphen
                .trim();
    }

    // Bulk operations
    public List<Category> createCategories(List<Category> categories) {
        categories.forEach(this::validateCategory);
        return categoryRepository.saveAll(categories);
    }

    public void deactivateCategories(Set<Long> categoryIds) {
        categoryIds.forEach(id -> {
            Category category = getCategoryById(id);
            category.setActive(false);
            categoryRepository.save(category);
        });
    }

    // Navigation menu structure
    public List<Category> getNavigationMenuStructure() {
        List<Category> mainCategories = getMainCategories();
        mainCategories.forEach(category -> {
            category.setSubcategories(getSubcategories(category.getId()));
        });
        return mainCategories;
    }

    // Category path
    public List<Category> getCategoryPath(Long categoryId) {
        Category category = getCategoryById(categoryId);
        List<Category> path = new java.util.ArrayList<>();
        while (category != null) {
            path.add(0, category);
            category = category.getParent();
        }
        return path;
    }
}
