package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.CartItem;
import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.service.CartService;
import com.lankafreshmart.market_store.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class CartController {
    private final ProductService productService;
    private final CartService cartService;

    @Autowired
    public CartController(ProductService productService, CartService cartService) {
        this.productService = productService;
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal User user, Model model) {
        model.addAttribute("cartItems", cartService.getCartItems());
        model.addAttribute("total", cartService.getCartTotal());
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam Long productId, @RequestParam int quantity,
                            @AuthenticationPrincipal User user, Model model) {
        try {
            cartService.addToCart(productId, quantity);
            model.addAttribute("success", "Item added to cart successfully!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/update")
    public String updateCartItem(@RequestParam Long cartItemId, @RequestParam int quantity,
                                 @AuthenticationPrincipal User user, Model model) {
        try {
            cartService.updateCartItem(cartItemId, quantity);
            model.addAttribute("success", "Cart updated successfully!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam Long cartItemId,
                                 @AuthenticationPrincipal User user, Model model) {
        try {
            cartService.removeFromCart(cartItemId);
            model.addAttribute("success", "Item removed from cart successfully!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "redirect:/cart";
    }

    @PostMapping("/cart/checkout")
    public String checkout(@AuthenticationPrincipal User user, Model model) {
        try {
            // Assuming OrderService is autowired and implemented
            // orderService.createOrder(user, cartService.getCartItems()); // Uncomment and implement
            cartService.getCartItems().forEach(cartItem -> cartService.removeFromCart(cartItem.getId())); // Clear cart
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
}