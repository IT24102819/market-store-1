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

import java.util.List;

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
        List<CartItem> cartItems = cartService.getCartItems();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", cartService.getCartTotal());
        if (!cartItems.isEmpty()) {
            for (CartItem item : cartItems) {
                Product product = productService.getProductById(item.getProduct().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found"));
                item.getProduct().setStockQuantity(product.getStockQuantity());
            }
            model.addAttribute("lowStockWarning", cartItems.stream()
                    .anyMatch(item -> item.getProduct().getStockQuantity() <= 5));
        }
        return "cart";
    }

    @PostMapping("/cart/add")
    public String addToCart(@AuthenticationPrincipal User user,
                            @RequestParam Long productId,
                            @RequestParam(defaultValue = "1") int quantity,
                            Model model) {
        try {
            cartService.addToCart(productId, quantity);
            model.addAttribute("success", "Product added to cart successfully!");
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
    public String initiateCheckout(@AuthenticationPrincipal User user, Model model) {
        List<CartItem> cartItems = cartService.getCartItems();
        if (cartItems == null || cartItems.isEmpty()) {
            model.addAttribute("error", "Your cart is empty.");
            return "cart";
        }
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", cartService.getCartTotal());
        return "checkout";
    }
}
