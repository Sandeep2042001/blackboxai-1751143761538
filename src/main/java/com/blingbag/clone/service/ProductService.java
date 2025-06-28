package com.blingbag.clone.service;

import com.blingbag.clone.model.Product;
import com.blingbag.clone.model.Category;
import com.blingbag.clone.repository.ProductRepository;
import com.blingbag.clone.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import javax.persistence.EntityNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Transactional
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    // Create operations
    public Product createProduct(Product product) {
        validateProduct(product);
        return productRepository.save(product);
    }

    // Read operations
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public Page<Product> getAllProducts(Pageable pageable) {
        return productRepository.findAll(pageable);
    }

    public Product getProductById(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    public Optional<Product> findBySku(String sku) {
        return productRepository.findBySku(sku);
    }

    // Update operations
    public Product updateProduct(Long id, Product productDetails) {
        Product product = getProductById(id);
        updateProductFields(product, productDetails);
        return productRepository.save(product);
    }

    // Delete operations
    public void deleteProduct(Long id) {
        Product product = getProductById(id);
        productRepository.delete(product);
    }

    // Soft delete
    public void deactivateProduct(Long id) {
        Product product = getProductById(id);
        product.setActive(false);
        productRepository.save(product);
    }

    // Search and filter operations
    public Page<Product> searchProducts(String keyword, Pageable pageable) {
        return productRepository.searchProducts(keyword, pageable);
    }

    public Page<Product> findByCategory(Category category, Pageable pageable) {
        return productRepository.findByCategory(category, pageable);
    }

    public Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceBetween(minPrice, maxPrice, pageable);
    }

    public Page<Product> findByMaterialType(String materialType, Pageable pageable) {
        return productRepository.findByMaterialType(materialType, pageable);
    }

    public Page<Product> findByColor(String color, Pageable pageable) {
        return productRepository.findByColor(color, pageable);
    }

    // added using chatgpt
    public Page<Product> findByStockQuantityGreaterThan(int quantity, Pageable pageable) {
    return productRepository.findByStockQuantityGreaterThan(quantity, pageable);
    }


    // Featured products
    public List<Product> getFeaturedProducts() {
        return productRepository.findByIsFeaturedTrue();
    }

    public Page<Product> getFeaturedProducts(Pageable pageable) {
        return productRepository.findByIsFeaturedTrue(pageable);
    }

    // New arrivals
    public List<Product> getNewArrivals() {
        return productRepository.findByIsNewArrivalTrue();
    }

    public Page<Product> getNewArrivals(Pageable pageable) {
        return productRepository.findByIsNewArrivalTrue(pageable);
    }

    // Advanced search
    public Page<Product> searchProductsWithFilters(
            Long categoryId,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            String materialType,
            String keyword,
            Pageable pageable) {
        return productRepository.searchProductsWithFilters(
                categoryId, minPrice, maxPrice, materialType, keyword, pageable);
    }

    // Stock management
    public Page<Product> findInStockProducts(Pageable pageable) {
        return productRepository.findByStockQuantityGreaterThan(0, pageable);
    }

    // Validation methods
    private void validateProduct(Product product) {
        if (!StringUtils.hasText(product.getName())) {
            throw new IllegalArgumentException("Product name cannot be empty");
        }
        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Product price must be greater than zero");
        }
        if (product.getStockQuantity() == null || product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        if (product.getCategory() == null) {
            throw new IllegalArgumentException("Product must belong to a category");
        }
        validateCategory(product.getCategory().getId());
    }

    private void validateCategory(Long categoryId) {
        if (!categoryRepository.existsById(categoryId)) {
            throw new EntityNotFoundException("Category not found with id: " + categoryId);
        }
    }

    // Helper methods
    private void updateProductFields(Product existingProduct, Product updatedProduct) {
        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setDiscountedPrice(updatedProduct.getDiscountedPrice());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());
        existingProduct.setStockQuantity(updatedProduct.getStockQuantity());
        existingProduct.setCategory(updatedProduct.getCategory());
        existingProduct.setColors(updatedProduct.getColors());
        existingProduct.setSku(updatedProduct.getSku());
        existingProduct.setActive(updatedProduct.isActive());
        existingProduct.setFeatured(updatedProduct.isFeatured());
        existingProduct.setNewArrival(updatedProduct.isNewArrival());
        existingProduct.setMaterialType(updatedProduct.getMaterialType());
        existingProduct.setWeightInGrams(updatedProduct.getWeightInGrams());
        existingProduct.setAdditionalImages(updatedProduct.getAdditionalImages());
    }

    // Inventory management
    public void updateStock(Long productId, Integer quantity) {
        Product product = getProductById(productId);
        int newQuantity = product.getStockQuantity() + quantity;
        if (newQuantity < 0) {
            throw new IllegalArgumentException("Insufficient stock for product: " + product.getName());
        }
        product.setStockQuantity(newQuantity);
        productRepository.save(product);
    }

    // Bulk operations
    public List<Product> createProducts(List<Product> products) {
        products.forEach(this::validateProduct);
        return productRepository.saveAll(products);
    }

    public void deactivateProducts(Set<Long> productIds) {
        productIds.forEach(this::deactivateProduct);
    }

    // Category-based operations
    public Long getProductCountByCategory(Category category) {
        return productRepository.countByCategory(category);
    }

    public Long getActiveProductCount() {
        return productRepository.countByIsActiveTrue();
    }
}
