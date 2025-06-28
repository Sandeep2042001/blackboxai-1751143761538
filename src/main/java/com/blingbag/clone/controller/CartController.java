package com.blingbag.clone.controller;

import com.blingbag.clone.model.User;
import com.blingbag.clone.model.Order;
import com.blingbag.clone.service.CartService;
import com.blingbag.clone.service.UserService;
import com.blingbag.clone.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpSession;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserService userService;
    private final OrderService orderService;

    @GetMapping
    public String viewCart(Model model, HttpSession session) {
        model.addAttribute("cartItems", cartService.getCartItems(session));
        model.addAttribute("subtotal", cartService.getSubtotal(session));
        model.addAttribute("shippingCost", cartService.getShippingCost(session));
        model.addAttribute("discount", cartService.getDiscount(session));
        model.addAttribute("total", cartService.getTotal(session));
        return "cart/view";
    }

    @PostMapping("/add")
    @ResponseBody
    public ResponseEntity<?> addToCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            @RequestParam(required = false) String color,
            HttpSession session) {
        try {
            cartService.addToCart(session, productId, quantity, color);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Product added to cart successfully");
            response.put("cartItemCount", cartService.getCartItems(session).size());
            response.put("cartTotal", cartService.getTotal(session));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateCartItem(
            @RequestParam Long productId,
            @RequestParam int quantity,
            HttpSession session) {
        try {
            cartService.updateCartItemQuantity(session, productId, quantity);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Cart updated successfully");
            response.put("subtotal", cartService.getSubtotal(session));
            response.put("shipping", cartService.getShippingCost(session));
            response.put("discount", cartService.getDiscount(session));
            response.put("total", cartService.getTotal(session));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/remove")
    @ResponseBody
    public ResponseEntity<?> removeFromCart(
            @RequestParam Long productId,
            HttpSession session) {
        try {
            cartService.removeFromCart(session, productId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Item removed from cart");
            response.put("cartItemCount", cartService.getCartItems(session).size());
            response.put("subtotal", cartService.getSubtotal(session));
            response.put("shipping", cartService.getShippingCost(session));
            response.put("discount", cartService.getDiscount(session));
            response.put("total", cartService.getTotal(session));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/clear")
    @ResponseBody
    public ResponseEntity<?> clearCart(HttpSession session) {
        cartService.clearCart(session);
        return ResponseEntity.ok().body("Cart cleared successfully");
    }

    @PostMapping("/apply-coupon")
    @ResponseBody
    public ResponseEntity<?> applyCoupon(
            @RequestParam String couponCode,
            HttpSession session) {
        try {
            cartService.applyCoupon(session, couponCode);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Coupon applied successfully");
            response.put("discount", cartService.getDiscount(session));
            response.put("total", cartService.getTotal(session));
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/remove-coupon")
    @ResponseBody
    public ResponseEntity<?> removeCoupon(HttpSession session) {
        cartService.removeCoupon(session);
        
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Coupon removed");
        response.put("total", cartService.getTotal(session));
        
        return ResponseEntity.ok(response);
    }

    @GetMapping("/checkout")
    public String checkout(
            Model model,
            HttpSession session,
            @AuthenticationPrincipal UserDetails userDetails) {
        
        // Validate cart
        cartService.validateCart(session);
        
        // Get user details
        User user = userService.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("User not found"));
        
        model.addAttribute("user", user);
        model.addAttribute("cartItems", cartService.getCartItems(session));
        model.addAttribute("subtotal", cartService.getSubtotal(session));
        model.addAttribute("shippingCost", cartService.getShippingCost(session));
        model.addAttribute("discount", cartService.getDiscount(session));
        model.addAttribute("total", cartService.getTotal(session));
        
        return "cart/checkout";
    }

    @PostMapping("/place-order")
    @ResponseBody
    public ResponseEntity<?> placeOrder(
            @RequestParam String shippingAddress,
            @RequestParam Order.PaymentMethod paymentMethod,
            HttpSession session,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User user = userService.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new IllegalStateException("User not found"));
            
            Order order = cartService.checkout(session, user, shippingAddress, paymentMethod);
            
            Map<String, Object> response = new HashMap<>();
            response.put("message", "Order placed successfully");
            response.put("orderId", order.getId());
            response.put("orderNumber", order.getOrderNumber());
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/mini-cart")
    public String getMiniCart(Model model, HttpSession session) {
        model.addAttribute("cartItems", cartService.getCartItems(session));
        model.addAttribute("total", cartService.getTotal(session));
        return "cart/mini-cart :: miniCartContent";
    }
}
