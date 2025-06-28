package com.blingbag.clone.controller;

import com.blingbag.clone.service.ProductService;
import com.blingbag.clone.service.CategoryService;
import com.blingbag.clone.model.Product;
import com.blingbag.clone.model.Category;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequiredArgsConstructor
public class HomeController {

    private final ProductService productService;
    private final CategoryService categoryService;

    @GetMapping("/")
    public String home(Model model) {
        // Featured Products
        Page<Product> featuredProducts = productService.getFeaturedProducts(PageRequest.of(0, 8));
        model.addAttribute("featuredProducts", featuredProducts.getContent());

        // New Arrivals
        Page<Product> newArrivals = productService.getNewArrivals(PageRequest.of(0, 8));
        model.addAttribute("newArrivals", newArrivals.getContent());

        // Main Categories
        List<Category> mainCategories = categoryService.getMainCategories();
        model.addAttribute("mainCategories", mainCategories);

        // Featured Categories
        List<Category> featuredCategories = categoryService.getFeaturedCategories();
        model.addAttribute("featuredCategories", featuredCategories);

        // Navigation Menu Structure
        List<Category> menuStructure = categoryService.getNavigationMenuStructure();
        model.addAttribute("menuStructure", menuStructure);

        return "index";
    }

    @GetMapping("/about")
    public String about(Model model) {
        // Add any necessary data for the about page
        return "about";
    }

    @GetMapping("/contact")
    public String contact(Model model) {
        // Add any necessary data for the contact page
        return "contact";
    }

    @GetMapping("/search")
    public String search(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Long categoryId,
            @RequestParam(required = false) String materialType,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {

        // Convert price to BigDecimal if provided
        java.math.BigDecimal minPriceBd = minPrice != null ? 
            java.math.BigDecimal.valueOf(minPrice) : null;
        java.math.BigDecimal maxPriceBd = maxPrice != null ? 
            java.math.BigDecimal.valueOf(maxPrice) : null;

        // Perform search with filters
        Page<Product> searchResults = productService.searchProductsWithFilters(
            categoryId,
            minPriceBd,
            maxPriceBd,
            materialType,
            keyword,
            PageRequest.of(page, size)
        );

        // Add search results to model
        model.addAttribute("products", searchResults.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", searchResults.getTotalPages());
        model.addAttribute("totalItems", searchResults.getTotalElements());

        // Add search parameters to model for maintaining state
        model.addAttribute("keyword", keyword);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("materialType", materialType);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);

        // Add filter options
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("materialTypes", List.of(
            "Kundan", "American Diamond", "Oxidised", "Temple", "Victorian"
        ));

        return "search";
    }

    @GetMapping("/collections")
    public String collections(Model model) {
        // Get all main collection categories
        List<Category> collections = categoryService.getCategoriesByCollectionType("collection");
        model.addAttribute("collections", collections);
        return "collections";
    }

    @GetMapping("/insta-reels-shop")
    public String instaReelsShop(Model model) {
        // Add Instagram shop related products or content
        Page<Product> trendingProducts = productService.getFeaturedProducts(PageRequest.of(0, 12));
        model.addAttribute("trendingProducts", trendingProducts.getContent());
        return "insta-reels-shop";
    }

    @GetMapping("/budget-store")
    public String budgetStore(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            Model model) {
        // Get products under different price ranges
        model.addAttribute("under499", productService.findByPriceRange(
            java.math.BigDecimal.ZERO, 
            java.math.BigDecimal.valueOf(499),
            PageRequest.of(0, 8)
        ).getContent());

        model.addAttribute("under1999", productService.findByPriceRange(
            java.math.BigDecimal.valueOf(500),
            java.math.BigDecimal.valueOf(1999),
            PageRequest.of(0, 8)
        ).getContent());

        model.addAttribute("under3499", productService.findByPriceRange(
            java.math.BigDecimal.valueOf(2000),
            java.math.BigDecimal.valueOf(3499),
            PageRequest.of(0, 8)
        ).getContent());

        return "budget-store";
    }

    @GetMapping("/style-ideas")
    public String styleIdeas(Model model) {
        // Add style guide content and featured products
        return "style-ideas";
    }

    @GetMapping("/clearance-sale")
    public String clearanceSale(Model model) {
        // Get products with highest discounts
        Page<Product> saleProducts = productService.getFeaturedProducts(PageRequest.of(0, 24));
        model.addAttribute("saleProducts", saleProducts.getContent());
        return "clearance-sale";
    }
}
