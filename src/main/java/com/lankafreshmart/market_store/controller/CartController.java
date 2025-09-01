package com.lankafreshmart.market_store.controller;


import com.lankafreshmart.market_store.model.CartItem;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class CartController {
    private final OrderService orderService;

    @Autowired
    public CartController(OrderService orderService) {
        this.orderService = orderService;
    }

    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal User user, Model model) {
        // Assuming cart items are stored in session or database
        List<CartItem> cartItems = getCartItemsFromSession(user); // Implement this method
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", calculateTotal(cartItems)); // Implement this method
        return "cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(@AuthenticationPrincipal User user, Model model) {
        try {
            // Assuming cart items are retrieved from session or database
            List<CartItem> cartItems = getCartItemsFromSession(user); // Implement this method
            if (cartItems == null || cartItems.isEmpty()) {
                model.addAttribute("error", "Your cart is empty.");
                return "cart";
            }

            orderService.createOrder(user, cartItems);
            // Clear cart after successful checkout (implement clearCart method)
            clearCartFromSession(user); // Implement this method
            model.addAttribute("success", "Order placed successfully! Check your email for confirmation.");
            return "redirect:/cart";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "cart";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process order: " + e.getMessage());
            return "cart";
        }
    }

    // Placeholder methods - implement based on your cart storage (e.g., session or database)
    private List<CartItem> getCartItemsFromSession(User user) {
        // Implement to retrieve cart items (e.g., from HttpSession or a Cart entity)
        return null; // Replace with actual logic
    }

    private double calculateTotal(List<CartItem> cartItems) {
        // Implement to calculate total price
        return 0.0; // Replace with actual logic
    }

    private void clearCartFromSession(User user) {
        // Implement to clear cart after checkout
    }
}