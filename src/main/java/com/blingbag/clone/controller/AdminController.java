package com.blingbag.clone.controller;

import com.blingbag.clone.model.*;
import com.blingbag.clone.service.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
@RequiredArgsConstructor
public class AdminController {

    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        // Order Statistics
        Object dailyOrderStats = orderService.getDailyOrderStats();
        List<Object> monthlyOrderStats = orderService.getMonthlyOrderStats(
            LocalDateTime.now().getYear()
        );
        BigDecimal averageOrderValue = orderService.getAverageOrderValue();
        
        model.addAttribute("dailyOrderStats", dailyOrderStats);
        model.addAttribute("monthlyOrderStats", monthlyOrderStats);
        model.addAttribute("averageOrderValue", averageOrderValue);
        
        // Recent Orders
        List<Order> recentOrders = orderService.getRecentOrders(5);
        model.addAttribute("recentOrders", recentOrders);
        
        // Unshipped Orders
        List<Order> unshippedOrders = orderService.findUnshippedOrders();
        model.addAttribute("unshippedOrders", unshippedOrders);
        
        // Low Stock Products
        Page<Product> lowStockProducts = productService.findByStockQuantityGreaterThan(
            0, PageRequest.of(0, 5)
        );
        model.addAttribute("lowStockProducts", lowStockProducts.getContent());
        
        return "admin/dashboard";
    }

    // Product Management
    @GetMapping("/products")
    public String listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir,
            Model model) {
        
        Sort.Direction direction = Sort.Direction.fromString(sortDir);
        Page<Product> products = productService.getAllProducts(
            PageRequest.of(page, size, Sort.by(direction, sortBy))
        );
        
        model.addAttribute("products", products.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", products.getTotalPages());
        model.addAttribute("totalItems", products.getTotalElements());
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("sortDir", sortDir);
        
        return "admin/products/list";
    }

    @GetMapping("/products/new")
    public String newProductForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products/form";
    }

    @PostMapping("/products")
    public String createProduct(
            @Valid @ModelAttribute Product product,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/products/form";
        }

        try {
            productService.createProduct(product);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Product created successfully");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating product: " + e.getMessage());
            return "redirect:/admin/products/new";
        }
    }

    @GetMapping("/products/{id}/edit")
    public String editProductForm(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "admin/products/form";
    }

    @PostMapping("/products/{id}")
    public String updateProduct(
            @PathVariable Long id,
            @Valid @ModelAttribute Product product,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/products/form";
        }

        try {
            productService.updateProduct(id, product);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Product updated successfully");
            return "redirect:/admin/products";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating product: " + e.getMessage());
            return "redirect:/admin/products/" + id + "/edit";
        }
    }

    @PostMapping("/products/{id}/deactivate")
    @ResponseBody
    public ResponseEntity<?> deactivateProduct(@PathVariable Long id) {
        try {
            productService.deactivateProduct(id);
            return ResponseEntity.ok().body("Product deactivated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Category Management
    @GetMapping("/categories")
    public String listCategories(Model model) {
        List<Category> categories = categoryService.getAllCategories();
        model.addAttribute("categories", categories);
        return "admin/categories/list";
    }

    @GetMapping("/categories/new")
    public String newCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("parentCategories", categoryService.getMainCategories());
        return "admin/categories/form";
    }

    @PostMapping("/categories")
    public String createCategory(
            @Valid @ModelAttribute Category category,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "admin/categories/form";
        }

        try {
            categoryService.createCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Category created successfully");
            return "redirect:/admin/categories";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error creating category: " + e.getMessage());
            return "redirect:/admin/categories/new";
        }
    }

    // User Management
    @GetMapping("/users")
    public String listUsers(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Page<User> users = userService.getAllUsers(PageRequest.of(page, size));
        model.addAttribute("users", users.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", users.getTotalPages());
        model.addAttribute("totalItems", users.getTotalElements());
        
        return "admin/users/list";
    }

    @PostMapping("/users/{id}/role")
    @ResponseBody
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestParam String role,
            @RequestParam boolean add) {
        try {
            if (add) {
                userService.addRole(id, role);
            } else {
                userService.removeRole(id, role);
            }
            return ResponseEntity.ok().body("User role updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/users/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateUserStatus(
            @PathVariable Long id,
            @RequestParam boolean active) {
        try {
            if (!active) {
                userService.deactivateUser(id);
            }
            return ResponseEntity.ok().body("User status updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    // Reports
    @GetMapping("/reports/sales")
    public String salesReport(
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            Model model) {
        
        BigDecimal totalRevenue = orderService.getTotalRevenue(startDate, endDate);
        model.addAttribute("totalRevenue", totalRevenue);
        
        List<Object> monthlyStats = orderService.getMonthlyOrderStats(
            LocalDateTime.now().getYear()
        );
        model.addAttribute("monthlyStats", monthlyStats);
        
        return "admin/reports/sales";
    }

    @GetMapping("/reports/inventory")
    public String inventoryReport(Model model) {
        Page<Product> products = productService.getAllProducts(
            PageRequest.of(0, Integer.MAX_VALUE)
        );
        model.addAttribute("products", products.getContent());
        return "admin/reports/inventory";
    }
}
