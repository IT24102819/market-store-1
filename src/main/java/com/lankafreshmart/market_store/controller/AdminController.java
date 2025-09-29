package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.Delivery;
import com.lankafreshmart.market_store.service.DeliveryService;
import com.lankafreshmart.market_store.service.EmailService;
import com.lankafreshmart.market_store.service.OrderService;
import com.lankafreshmart.market_store.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Restricts access to users with ADMIN role
public class AdminController {

    private final OrderService orderService;
    private final DeliveryService deliveryService;
    private final EmailService emailService;
    private final UserService userService;

    @Autowired
    public AdminController(OrderService orderService, DeliveryService deliveryService, EmailService emailService, UserService userService) {
        this.orderService = orderService;
        this.deliveryService = deliveryService;
        this.emailService = emailService;
        this.userService = userService;
    }

    @GetMapping("/dashboard")
    public String showDashboard(Model model) {
        model.addAttribute("newEmailCount", emailService.getNewEmailCount());
        model.addAttribute("users", userService.getAllUsers());
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
}