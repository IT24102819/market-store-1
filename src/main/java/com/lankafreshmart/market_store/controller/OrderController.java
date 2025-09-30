package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.CartItem;
import com.lankafreshmart.market_store.model.Delivery;
import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.repository.OrderRepository;
import com.lankafreshmart.market_store.service.CartService;
import com.lankafreshmart.market_store.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class OrderController {
    private final OrderService orderService;
    private final OrderRepository orderRepository;
    private final CartService cartService;

    @Autowired
    public OrderController(OrderService orderService, OrderRepository orderRepository, CartService cartService) {
        this.orderService = orderService;
        this.orderRepository = orderRepository;
        this.cartService = cartService;
    }

    @PostMapping("/order/place")
    public String placeOrder(@AuthenticationPrincipal User user, Model model,
                             @RequestParam String deliveryMethod,
                             @RequestParam(required = false) String paymentMethod,
                             @RequestParam(required = false) String address) {
        System.out.println("Received address in OrderController: " + address); // Debug line
        try {
            List<CartItem> cartItems = cartService.getCartItems();
            if (cartItems == null || cartItems.isEmpty()) {
                model.addAttribute("error", "Your cart is empty.");
                return "checkout";
            }

            if ("DELIVERY".equals(deliveryMethod) && (address == null || address.trim().isEmpty())) {
                model.addAttribute("error", "Please enter a delivery address.");
                model.addAttribute("cartItems", cartItems);
                model.addAttribute("total", cartService.getCartTotal());
                return "checkout";
            }

            paymentMethod = paymentMethod != null ? paymentMethod : "CASH_ON_DELIVERY";
            if ("CARD".equals(paymentMethod) && !validateCardDetails(model)) {
                return "checkout";
            }

            Order order = orderService.createOrder(user, cartItems, paymentMethod, deliveryMethod, address);
            cartItems.forEach(item -> cartService.removeFromCart(item.getId()));

            model.addAttribute("success", "Order placed successfully! Check your email for confirmation.");
            model.addAttribute("orderId", order.getId());
            return "order-success";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "checkout";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to process order: " + e.getMessage());
            return "checkout";
        }
    }

    @GetMapping("/order/history")
    public String orderHistory(@AuthenticationPrincipal User user, Model model) {
        List<Order> orders = orderRepository.findByUser(user);
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

    @PostMapping("/order/cancel")
    public String cancelOrder(@RequestParam Long orderId, @AuthenticationPrincipal User user, Model model) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
            if (!order.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Unauthorized access to order");
            }
            orderService.cancelOrder(orderId);
            model.addAttribute("success", "Order cancelled successfully. Stock has been reverted.");
            return "redirect:/order/history";
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/order/history";
        } catch (Exception e) {
            model.addAttribute("error", "Failed to cancel order: " + e.getMessage());
            return "redirect:/order/history";
        }
    }

    @GetMapping("/order/edit")
    public String showEditOrderForm(@RequestParam Long orderId, @AuthenticationPrincipal User user, Model model) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!order.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("Unauthorized access to order");
        }
        if (!"PLACED".equals(order.getStatus())) {
            model.addAttribute("error", "Only PENDING orders can be updated.");
            return "redirect:/order/details?orderId=" + orderId;
        }
        model.addAttribute("order", order);
        return "order-edit";
    }

    @PostMapping("/order/update")
    public String updateOrder(@RequestParam Long orderId, @AuthenticationPrincipal User user,
                              @RequestParam String deliveryMethod, @RequestParam String address,
                              Model model) {
        try {
            Order order = orderRepository.findById(orderId)
                    .orElseThrow(() -> new IllegalArgumentException("Order not found"));
            if (!order.getUser().getId().equals(user.getId())) {
                throw new IllegalArgumentException("Unauthorized access to order");
            }
            orderService.updateOrder(orderId, deliveryMethod, address);
            model.addAttribute("success", "Order updated successfully.");
            return "redirect:/order/details?orderId=" + orderId;
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "redirect:/order/edit?orderId=" + orderId;
        } catch (Exception e) {
            model.addAttribute("error", "Failed to update order: " + e.getMessage());
            return "redirect:/order/edit?orderId=" + orderId;
        }
    }

    private boolean validateCardDetails(Model model) {
        String cardNumber = model.getAttribute("cardNumber") != null ? model.getAttribute("cardNumber").toString() : "";
        if (cardNumber.isEmpty() || cardNumber.length() < 16) {
            model.addAttribute("error", "Invalid card details. Please enter a valid card number.");
            return false;
        }
        return true;
    }
}

