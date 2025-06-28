package com.blingbag.clone.controller;

import com.blingbag.clone.model.User;
import com.blingbag.clone.model.Order;
import com.blingbag.clone.service.UserService;
import com.blingbag.clone.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final OrderService orderService;

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        model.addAttribute("user", new User());
        return "user/register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute("user") User user,
            BindingResult result,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "user/register";
        }

        try {
            userService.createUser(user);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Registration successful! Please login.");
            return "redirect:/user/login";
        } catch (Exception e) {
            result.rejectValue("email", "error.user", 
                "An account already exists for this email.");
            return "user/register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "user/login";
    }

    @GetMapping("/account")
    public String viewAccount(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        model.addAttribute("user", user);
        
        // Get recent orders
        List<Order> recentOrders = orderService.getOrdersByUser(user);
        model.addAttribute("recentOrders", recentOrders);
        
        return "user/account";
    }

    @GetMapping("/orders")
    public String viewOrders(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Page<Order> orders = orderService.getAllOrders(PageRequest.of(page, size));
        
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());
        
        return "user/orders";
    }

    @GetMapping("/order/{orderId}")
    public String viewOrderDetails(
            @PathVariable Long orderId,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        Order order = orderService.getOrderById(orderId);
        
        // Verify order belongs to user
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalStateException("Unauthorized access to order");
        }
        
        model.addAttribute("order", order);
        return "user/order-details";
    }

    @GetMapping("/profile")
    public String viewProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        model.addAttribute("user", user);
        return "user/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @Valid @ModelAttribute("user") User updatedUser,
            BindingResult result,
            @AuthenticationPrincipal UserDetails userDetails,
            RedirectAttributes redirectAttributes) {
        
        if (result.hasErrors()) {
            return "user/profile";
        }

        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            
            userService.updateUser(user.getId(), updatedUser);
            redirectAttributes.addFlashAttribute("successMessage", 
                "Profile updated successfully");
            
            return "redirect:/user/profile";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", 
                "Error updating profile: " + e.getMessage());
            return "redirect:/user/profile";
        }
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "user/change-password";
    }

    @PostMapping("/change-password")
    @ResponseBody
    public ResponseEntity<?> changePassword(
            @RequestParam String oldPassword,
            @RequestParam String newPassword,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            
            userService.updatePassword(user.getId(), oldPassword, newPassword);
            
            Map<String, String> response = new HashMap<>();
            response.put("message", "Password updated successfully");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/addresses")
    public String viewAddresses(
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        model.addAttribute("user", user);
        return "user/addresses";
    }

    @PostMapping("/address/shipping")
    @ResponseBody
    public ResponseEntity<?> updateShippingAddress(
            @RequestParam String addressLine1,
            @RequestParam(required = false) String addressLine2,
            @RequestParam String city,
            @RequestParam String state,
            @RequestParam String postalCode,
            @RequestParam String country,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            
            userService.updateShippingAddress(user.getId(), 
                addressLine1, addressLine2, city, state, postalCode, country);
            
            return ResponseEntity.ok().body("Shipping address updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/preferences")
    @ResponseBody
    public ResponseEntity<?> updatePreferences(
            @RequestParam boolean marketingEmails,
            @RequestParam boolean smsNotifications,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            
            userService.updateMarketingPreferences(user.getId(), 
                marketingEmails, smsNotifications);
            
            return ResponseEntity.ok().body("Preferences updated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/deactivate")
    @ResponseBody
    public ResponseEntity<?> deactivateAccount(
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            
            userService.deactivateUser(user.getId());
            return ResponseEntity.ok().body("Account deactivated successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
