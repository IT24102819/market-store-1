package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.repository.OrderRepository;
import com.lankafreshmart.market_store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/chatbot")
public class ChatbotApiController {

    private final OrderRepository orderRepository;
    private final UserRepository userRepository;

    @Value("${app.n8n.api-key}")
    private String n8nApiKey;

    public ChatbotApiController(OrderRepository orderRepository, UserRepository userRepository) {
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
    }

    @GetMapping("/orders")
    public ResponseEntity<?> getUserOrders(
            @RequestParam String email,
            @RequestHeader(value = "X-API-KEY") String apiKey) {

        // --- DEBUG LOGS START ---
        System.out.println("--- CHATBOT API HIT ---");
        System.out.println("Received Username/Email: " + email);
        System.out.println("Received API Key: " + apiKey);
        // ------------------------

        if (n8nApiKey == null || !n8nApiKey.equals(apiKey)) {
            System.out.println("ERROR: Invalid API Key. Expected: " + n8nApiKey);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid API Key");
        }

        // Try finding by username first (since frontend sends username)
        User user = userRepository.findByUsername(email).orElse(null);

        if (user == null) {
            System.out.println("User not found by username. Trying email...");
            user = userRepository.findByEmail(email).orElse(null);
        }

        if (user == null) {
            System.out.println("ERROR: User not found in database!");
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
        }

        System.out.println("User Found: " + user.getUsername() + " (ID: " + user.getId() + ")");

        List<Order> orders = orderRepository.findByUser(user);
        System.out.println("Orders found: " + orders.size());

        List<Map<String, Object>> responseData = orders.stream().map(order -> {
            String itemList = order.getOrderItems().stream()
                    .map(item -> item.getQuantity() + "x " + item.getProduct().getName())
                    .collect(Collectors.joining(", "));

            String deliveryAddress = (order.getDelivery() != null && order.getDelivery().getAddress() != null)
                    ? order.getDelivery().getAddress()
                    : "Pickup / No Address";

            return Map.<String, Object>of(
                    "orderId", order.getId(),
                    "totalAmount", order.getTotalAmount(),
                    "status", order.getStatus(),
                    "date", order.getOrderDate().toString(),
                    "items", itemList,
                    "address", deliveryAddress,
                    "deliveryStatus", (order.getDelivery() != null ? order.getDelivery().getStatus() : "Not Scheduled"),
                    "trackingNumber", (order.getDelivery() != null && order.getDelivery().getTrackingNumber() != null
                            ? order.getDelivery().getTrackingNumber() : "Pending")
            );
        }).collect(Collectors.toList());

        return ResponseEntity.ok(responseData);
    }
}