package com.blingbag.clone.controller;

import com.blingbag.clone.model.Product;
import com.blingbag.clone.model.Category;
import com.blingbag.clone.service.ProductService;
import com.blingbag.clone.service.CategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/{id}")
    public String getProduct(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);

        // Get related products from same category
        Page<Product> relatedProducts = productService.findByCategory(
            product.getCategory(),
            PageRequest.of(0, 4)
        );
        model.addAttribute("relatedProducts", relatedProducts.getContent());

        // Get other products in same material type
        if (product.getMaterialType() != null) {
            Page<Product> similarMaterialProducts = productService.findByMaterialType(
                product.getMaterialType(),
                PageRequest.of(0, 4)
            );
            model.addAttribute("similarMaterialProducts", similarMaterialProducts.getContent());
        }

        return "product/detail";
    }

    @GetMapping("/shop")
    public String shopProducts(
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String materialType,
            @RequestParam(required = false) String color,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        // Convert price to BigDecimal if provided
        java.math.BigDecimal minPriceBd = minPrice != null ? 
            java.math.BigDecimal.valueOf(minPrice) : null;
        java.math.BigDecimal maxPriceBd = maxPrice != null ? 
            java.math.BigDecimal.valueOf(maxPrice) : null;

        // Create sort object
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        PageRequest pageRequest = PageRequest.of(page, size, sort);

        // Get products with filters
        Page<Product> products = productService.searchProductsWithFilters(
            categoryId,
            minPriceBd,
            maxPriceBd,
            materialType,
            null, // no keyword search in shop view
            pageRequest
        );

        // Add products to model
        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());

        // Add filter parameters to model
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("materialType", materialType);
        model.addAttribute("color", color);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        // Add filter options
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("materialTypes", List.of(
            "Kundan", "American Diamond", "Oxidised", "Temple", "Victorian"
        ));
        model.addAttribute("colors", List.of(
            "Gold", "Silver", "Rose Gold", "Oxidised Silver",
            "Red", "Green", "Blue", "Pink", "Purple",
            "Multi-Color"
        ));

        return "product/shop";
    }

    @GetMapping("/category/{categoryId}")
    public String getProductsByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir,
            Model model) {

        Category category = categoryService.getCategoryById(categoryId);
        model.addAttribute("category", category);

        // Get category path for breadcrumbs
        List<Category> categoryPath = categoryService.getCategoryPath(categoryId);
        model.addAttribute("categoryPath", categoryPath);

        // Get products in category
        Sort sort = Sort.by(Sort.Direction.fromString(sortDir), sortBy);
        Page<Product> products = productService.findByCategory(
            category,
            PageRequest.of(page, size, sort)
        );

        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);

        // Get subcategories if any
        List<Category> subcategories = categoryService.getSubcategories(categoryId);
        model.addAttribute("subcategories", subcategories);

        return "product/category";
    }

    @GetMapping("/new-arrivals")
    public String getNewArrivals(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        Page<Product> newArrivals = productService.getNewArrivals(PageRequest.of(page, size));
        model.addAttribute("products", newArrivals.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", newArrivals.getTotalPages());
        model.addAttribute("totalItems", newArrivals.getTotalElements());

        return "product/new-arrivals";
    }

    @GetMapping("/featured")
    public String getFeaturedProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        Page<Product> featuredProducts = productService.getFeaturedProducts(PageRequest.of(page, size));
        model.addAttribute("products", featuredProducts.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", featuredProducts.getTotalPages());
        model.addAttribute("totalItems", featuredProducts.getTotalElements());

        return "product/featured";
    }

    @GetMapping("/quick-view/{id}")
    public String quickView(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        return "product/quick-view :: productQuickView";
    }
}
