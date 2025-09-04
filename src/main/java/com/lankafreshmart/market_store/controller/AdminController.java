package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.service.DeliveryService;
import com.lankafreshmart.market_store.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')") // Restricts access to users with ADMIN role
public class AdminController {

    private final OrderService orderService;
    private final DeliveryService deliveryService;

    @Autowired
    public AdminController(OrderService orderService, DeliveryService deliveryService) {
        this.orderService = orderService;
        this.deliveryService = deliveryService;
    }

    @GetMapping("/orders")
    public String listOrders(Model model) {
        List<Order> orders = orderService.getAllOrders(); // Assuming this method exists
        model.addAttribute("orders", orders);
        return "admin-orders";
    }

    @PostMapping("/delivery/update")
    public String updateDeliveryStatus(@RequestParam Long deliveryId, @RequestParam String newStatus) {
        System.out.println("Attempting to update delivery ID: " + deliveryId + " to status: " + newStatus);
        try {
            deliveryService.updateDeliveryStatus(deliveryId, newStatus);
            System.out.println("Successfully updated delivery status for ID: " + deliveryId);
        } catch (IllegalArgumentException e) {
            System.out.println("Failed to update delivery status: " + e.getMessage());
            return "redirect:/admin/orders?error=Delivery not found";
        } catch (Exception e) {
            System.out.println("Unexpected error updating delivery status: " + e.getMessage());
            return "redirect:/admin/orders?error=Unexpected error";
        }
        return "redirect:/admin/orders";
    }
}