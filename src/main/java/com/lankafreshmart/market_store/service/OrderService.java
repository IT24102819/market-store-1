package com.lankafreshmart.market_store.service;


import com.lankafreshmart.market_store.model.*;
import com.lankafreshmart.market_store.repository.DeliveryRepository;
import com.lankafreshmart.market_store.repository.OrderRepository;
import com.lankafreshmart.market_store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final EmailService emailService;
    private final DeliveryService deliveryService;
    private final DeliveryRepository deliveryRepository;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        ProductService productService, EmailService emailService, DeliveryService deliveryService, DeliveryRepository deliveryRepository) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.emailService = emailService;
        this.deliveryService = deliveryService;
        this.deliveryRepository = deliveryRepository;
    }

    @Transactional
    public Order createOrder(User user, List<CartItem> cartItems, String paymentMethod, String deliveryMethod) throws IllegalStateException {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // Validate stock and calculate total, update unitsSold
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int requestedQuantity = cartItem.getQuantity();
            if (product.getStockQuantity() < requestedQuantity) {
                throw new IllegalStateException("Insufficient stock for " + product.getName());
            }
            BigDecimal itemTotal = product.getPrice().multiply(BigDecimal.valueOf(requestedQuantity));
            totalAmount = totalAmount.add(itemTotal);
            orderItems.add(new OrderItem(product, requestedQuantity, product.getPrice().doubleValue()));
            product.setStockQuantity(product.getStockQuantity() - requestedQuantity);
            product.setUnitsSold(product.getUnitsSold() != null ? product.getUnitsSold() + requestedQuantity : requestedQuantity); // Increment units sold
            productRepository.save(product); // Save updated stock and units sold
        }

        // Create and save order with payment and delivery details
        Order order = new Order(user, totalAmount.doubleValue(), orderItems);
        order.setStatus("PENDING");
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PENDING".equals(paymentMethod) ? "PENDING" : "PROCESSING");
        order.setDeliveryMethod(deliveryMethod);
        order = orderRepository.save(order);

        // Create delivery
        Delivery delivery = deliveryService.createDelivery(order);
        delivery.setStatus("PENDING");
        delivery.setEstimatedDeliveryDate(LocalDateTime.now().plusDays(3)); // Example: 3 days for delivery
        deliveryRepository.save(delivery);

        order.setDelivery(delivery);
        orderRepository.save(order);

        // Send confirmation email
        try {
            emailService.sendOrderConfirmationEmail(user.getEmail(), order);
        } catch (Exception e) {
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        }

        return order;
    }

    @Transactional
    public void cancelOrder(Long orderId) throws IllegalStateException {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!"PENDING".equals(order.getStatus())) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }
        // Revert stock quantities
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }
        order.setStatus("CANCELLED");
        orderRepository.save(order);
    }

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public Optional<Order> findById(Long orderId) {
        return orderRepository.findById(orderId);
    }
}
