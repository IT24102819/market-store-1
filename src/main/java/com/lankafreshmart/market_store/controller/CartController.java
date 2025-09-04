package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.CartItem;
import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.repository.OrderRepository;
import com.lankafreshmart.market_store.service.CartService;
import com.lankafreshmart.market_store.service.OrderService;
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
    private final OrderService orderService;
    private final OrderRepository orderRepository;

    @Autowired
    public CartController(ProductService productService, CartService cartService, OrderService orderService, OrderRepository orderRepository) {
        this.productService = productService;
        this.cartService = cartService;
        this.orderService = orderService;
        this.orderRepository = orderRepository;
    }

    @GetMapping("/cart")
    public String viewCart(@AuthenticationPrincipal User user, Model model) {
        List<CartItem> cartItems = cartService.getCartItems();
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("total", cartService.getCartTotal());

        // Enhance with product stock status using productService
        if (!cartItems.isEmpty()) {
            for (CartItem item : cartItems) {
                Product product = productService.getProductById(item.getProduct().getId())
                        .orElseThrow(() -> new IllegalArgumentException("Product not found"));
                item.getProduct().setStockQuantity(product.getStockQuantity()); // Update with latest stock
            }
            model.addAttribute("lowStockWarning", cartItems.stream()
                    .anyMatch(item -> item.getProduct().getStockQuantity() <= 5)); // Example threshold
        }
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
    public String checkout(@AuthenticationPrincipal User user, Model model, @RequestParam String paymentMethod) {
        try {
            List<CartItem> cartItems = cartService.getCartItems();
            if (cartItems == null || cartItems.isEmpty()) {
                model.addAttribute("error", "Your cart is empty.");
                return "cart";
            }

            // Create order
            Order order = orderService.createOrder(user, cartItems, paymentMethod);

            // Clear cart
            cartItems.forEach(item -> cartService.removeFromCart(item.getId()));

            model.addAttribute("success", "Order placed successfully! Check your email for confirmation.");
            model.addAttribute("orderId", order.getId());
            return "order-success";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "cart";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process order: " + e.getMessage());
            return "cart";
        }
    }

    @GetMapping("/order/history")
    public String orderHistory(@AuthenticationPrincipal User user, Model model) {
        List<Order> orders = orderRepository.findByUser(user); // Assume OrderRepository is autowired
        model.addAttribute("orders", orders);
        return "order-history";
    }

    @GetMapping("/order/details")
    public String orderDetails(@RequestParam Long orderId, @AuthenticationPrincipal User user, Model model) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access to order");
        }
        model.addAttribute("order", order);
        return "order-details";
    }
}