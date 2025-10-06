package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.Delivery;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.repository.UserRepository;
import com.lankafreshmart.market_store.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Restricts access to users with ADMIN role
public class AdminController {

    private final OrderService orderService;
    private final DeliveryService deliveryService;
    private final EmailService emailService;
    private final UserService userService;
    private final UserRepository userRepository;
    private final SaleService saleService;
    private final ProductService productService;

    @Autowired
    public AdminController(OrderService orderService, DeliveryService deliveryService, EmailService emailService, UserService userService, UserRepository userRepository, SaleService saleService,
                           ProductService productService) {
        this.orderService = orderService;
        this.deliveryService = deliveryService;
        this.emailService = emailService;
        this.userService = userService;
        this.userRepository = userRepository;
        this.saleService = saleService;
        this.productService = productService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("newEmailCount", emailService.getNewEmailCount());
        model.addAttribute("users", userService.getAllUsers());
        LocalDateTime start = LocalDateTime.now().minusDays(30);
        LocalDateTime end = LocalDateTime.now();
        model.addAttribute("totalSales", saleService.getTotalSales(start, end));
        model.addAttribute("fastMovingCount", saleService.getFastMovingItems(start, end).size());
        model.addAttribute("slowMovingCount", saleService.getSlowMovingItems(start, end).size());
        model.addAttribute("customerCount", userService.getRegisteredUsersCount());
        model.addAttribute("productCount", productService.getListedProductsCount());
        model.addAttribute("completedDeliveries", deliveryService.getCompletedDeliveriesCount());
        model.addAttribute("pendingDeliveries", deliveryService.getPendingDeliveriesCount());
        model.addAttribute("cancelledDeliveries", deliveryService.getCancelledDeliveriesCount());
        model.addAttribute("shippedDeliveries", deliveryService.getShippedDeliveriesCount());
        model.addAttribute("neverSoldItems", productService.getNeverSoldItemsCount());
        return "admin-dashboard";
    }

    @GetMapping("/reset-email-count")
    public String resetEmailCount() {
        emailService.resetNewEmailCount();
        return "redirect:/admin/dashboard";
    }

    @GetMapping("/orders")
    public String listOrders(Model model, @RequestParam(required = false) String success, @RequestParam(required = false) String error) {
        List<Order> orders = orderService.getAllOrders();
        model.addAttribute("orders", orders);
        if (success != null) {
            model.addAttribute("success", success);
        }
        if (error != null) {
            model.addAttribute("error", error);
        }
        return "admin-orders";
    }

    @GetMapping("/delivery/{deliveryId}")
    public String getDeliveryDetails(@PathVariable Long deliveryId, Model model) {
        Delivery delivery = deliveryService.getDelivery(deliveryId); // Ensure fresh fetch
        model.addAttribute("delivery", delivery);
        return "delivery-details";
    }

    @PostMapping("/delivery/{deliveryId}/update")
    public String updateDeliveryStatus(@PathVariable Long deliveryId,
                                       @RequestParam String newStatus,
                                       Model model) {
        System.out.println("Attempting to update delivery ID: " + deliveryId + " to status: " + newStatus);
        try {
            deliveryService.updateDeliveryStatus(deliveryId, newStatus);
            System.out.println("Successfully updated delivery status for ID: " + deliveryId);
            return "redirect:/admin/delivery/" + deliveryId + "?success=Delivery status updated successfully";
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to update delivery status: " + e.getMessage());
            return "redirect:/admin/delivery/" + deliveryId + "?error=Delivery not found";
        } catch (IllegalStateException e) {
            System.out.println("Unexpected error updating delivery status: " + e.getMessage());
            return "redirect:/admin/delivery/" + deliveryId + "?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Unexpected error updating delivery status: " + e.getMessage());
            return "redirect:/admin/delivery/" + deliveryId + "?error=Unexpected error occurred";
        }
    }

    @PostMapping("/delivery/{deliveryId}/delete")
    public String deleteDelivery(@PathVariable Long deliveryId, Model model) {
        try {
            deliveryService.deleteDelivery(deliveryId);
            System.out.println("Successfully deleted delivery ID: " + deliveryId);
            return "redirect:/admin/orders?success=Delivery deleted successfully";
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
            return "redirect:/admin/orders?error=Delivery not found";
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            return "redirect:/admin/orders?error=Failed to delete delivery: " + e.getMessage();
        }
    }

    @PostMapping("/orders/{orderId}/refund")
    public String refundOrder(@PathVariable Long orderId, Model model) {
        System.out.println("Attempting to refund order ID: " + orderId);
        try {
            orderService.updatePaymentStatusToRefunded(orderId);
            System.out.println("Successfully refunded order ID: " + orderId);
            return "redirect:/admin/orders?success=Order refunded successfully";
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to refund: " + e.getMessage());
            return "redirect:/admin/orders?error=Order not found";
        } catch (IllegalStateException e) {
            System.out.println("Invalid refund state: " + e.getMessage());
            return "redirect:/admin/orders?error=" + URLEncoder.encode(e.getMessage(), StandardCharsets.UTF_8);
        } catch (Exception e) {
            System.out.println("Unexpected error: " + e.getMessage());
            return "redirect:/admin/orders?error=Unexpected error occurred";
        }
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/promote/{userId}")
    public String promoteUserToAdmin(@PathVariable Long userId, Model model) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRole("ADMIN");
        userRepository.save(user);
        model.addAttribute("success", "User promoted to admin successfully!");
        return "redirect:/admin/users";
    }
}