package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.service.CartService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class CartController {
    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping("/cart")
    public String showCart(Model model) {
        try {
            model.addAttribute("cartItems", cartService.getCartItems());
            model.addAttribute("cartTotal", cartService.getCartTotal());
            return "cart";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "error";
        }
    }

    @PostMapping("/cart/add")
    public String addToCart(@RequestParam("productId") Long productId,
                            @RequestParam("quantity") int quantity, Model model) {
        try {
            cartService.addToCart(productId, quantity);
            return "redirect:/cart?success=Product added to cart";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cartItems", cartService.getCartItems());
            model.addAttribute("cartTotal", cartService.getCartTotal());
            return "cart";
        }
    }

    @PostMapping("/cart/update")
    public String updateCart(@RequestParam("cartItemId") Long cartItemId,
                             @RequestParam("quantity") int quantity, Model model) {
        try {
            cartService.updateCartItem(cartItemId, quantity);
            return "redirect:/cart?success=Cart updated successfully";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cartItems", cartService.getCartItems());
            model.addAttribute("cartTotal", cartService.getCartTotal());
            return "cart";
        }
    }

    @PostMapping("/cart/remove")
    public String removeFromCart(@RequestParam("cartItemId") Long cartItemId, Model model) {
        try {
            cartService.removeFromCart(cartItemId);
            return "redirect:/cart?success=Product removed from cart";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("cartItems", cartService.getCartItems());
            model.addAttribute("cartTotal", cartService.getCartTotal());
            return "cart";
        }
    }
}