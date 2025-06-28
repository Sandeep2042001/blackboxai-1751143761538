package com.blingbag.clone.controller;

import com.blingbag.clone.model.Order;
import com.blingbag.clone.model.User;
import com.blingbag.clone.service.OrderService;
import com.blingbag.clone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/order")
@RequiredArgsConstructor
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    @GetMapping("/{orderNumber}")
    public String viewOrder(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        Order order = orderService.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        // Verify user has access to this order
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        if (!order.getUser().getId().equals(user.getId()) && 
            !userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new IllegalStateException("Unauthorized access to order");
        }
        
        model.addAttribute("order", order);
        return "order/details";
    }

    @GetMapping("/track/{orderNumber}")
    public String trackOrder(
            @PathVariable String orderNumber,
            Model model) {
        
        Order order = orderService.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        model.addAttribute("order", order);
        return "order/track";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/list")
    public String listOrders(
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) Order.PaymentMethod paymentMethod,
            @RequestParam(required = false) Order.PaymentStatus paymentStatus,
            @RequestParam(required = false) LocalDateTime startDate,
            @RequestParam(required = false) LocalDateTime endDate,
            @RequestParam(required = false) Double minTotal,
            @RequestParam(required = false) Double maxTotal,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Model model) {
        
        Page<Order> orders = orderService.searchOrders(
            status,
            paymentMethod,
            paymentStatus,
            startDate,
            endDate,
            minTotal != null ? java.math.BigDecimal.valueOf(minTotal) : null,
            maxTotal != null ? java.math.BigDecimal.valueOf(maxTotal) : null,
            PageRequest.of(page, size)
        );
        
        model.addAttribute("orders", orders.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", orders.getTotalPages());
        model.addAttribute("totalItems", orders.getTotalElements());
        
        // Add filter options to model
        model.addAttribute("orderStatuses", Order.OrderStatus.values());
        model.addAttribute("paymentMethods", Order.PaymentMethod.values());
        model.addAttribute("paymentStatuses", Order.PaymentStatus.values());
        
        return "order/admin/list";
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{id}/status")
    @ResponseBody
    public ResponseEntity<?> updateOrderStatus(
            @PathVariable Long id,
            @RequestParam Order.OrderStatus status) {
        try {
            Order updatedOrder = orderService.updateOrderStatus(id, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order status updated successfully");
            response.put("status", updatedOrder.getStatus());
            response.put("lastModified", updatedOrder.getOrderDate());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{id}/payment-status")
    @ResponseBody
    public ResponseEntity<?> updatePaymentStatus(
            @PathVariable Long id,
            @RequestParam Order.PaymentStatus status) {
        try {
            Order updatedOrder = orderService.updatePaymentStatus(id, status);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Payment status updated successfully");
            response.put("paymentStatus", updatedOrder.getPaymentStatus());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/{id}/tracking")
    @ResponseBody
    public ResponseEntity<?> updateTrackingNumber(
            @PathVariable Long id,
            @RequestParam String trackingNumber) {
        try {
            Order updatedOrder = orderService.updateTrackingNumber(id, trackingNumber);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Tracking number updated successfully");
            response.put("trackingNumber", updatedOrder.getTrackingNumber());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/dashboard")
    public String orderDashboard(Model model) {
        // Get daily order statistics
        Object dailyStats = orderService.getDailyOrderStats();
        model.addAttribute("dailyStats", dailyStats);
        
        // Get monthly order statistics for current year
        List<Object> monthlyStats = orderService.getMonthlyOrderStats(
            LocalDateTime.now().getYear()
        );
        model.addAttribute("monthlyStats", monthlyStats);
        
        // Get unshipped orders
        List<Order> unshippedOrders = orderService.findUnshippedOrders();
        model.addAttribute("unshippedOrders", unshippedOrders);
        
        // Get orders pending delivery
        List<Order> pendingDelivery = orderService.findOrdersPendingDelivery();
        model.addAttribute("pendingDelivery", pendingDelivery);
        
        // Get orders requiring follow-up
        List<Order> followUpOrders = orderService.findOrdersRequiringFollowUp();
        model.addAttribute("followUpOrders", followUpOrders);
        
        return "order/admin/dashboard";
    }

    @GetMapping("/invoice/{orderNumber}")
    public String viewInvoice(
            @PathVariable String orderNumber,
            @AuthenticationPrincipal UserDetails userDetails,
            Model model) {
        
        Order order = orderService.findByOrderNumber(orderNumber)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        
        // Verify user has access to this order
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        if (!order.getUser().getId().equals(user.getId()) && 
            !userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"))) {
            throw new IllegalStateException("Unauthorized access to order");
        }
        
        model.addAttribute("order", order);
        return "order/invoice";
    }
}
