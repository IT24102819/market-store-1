package com.lankafreshmart.market_store.service;


import com.lankafreshmart.market_store.model.*;
import com.lankafreshmart.market_store.repository.OrderRepository;
import com.lankafreshmart.market_store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    private final OrderRepository orderRepository;
    private final ProductRepository productRepository;
    private final ProductService productService;
    private final EmailService emailService;

    @Autowired
    public OrderService(OrderRepository orderRepository, ProductRepository productRepository,
                        ProductService productService, EmailService emailService) {
        this.orderRepository = orderRepository;
        this.productRepository = productRepository;
        this.productService = productService;
        this.emailService = emailService;
    }

    @Transactional
    public Order createOrder(User user, List<CartItem> cartItems, String paymentMethod) throws IllegalStateException {
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderItem> orderItems = new ArrayList<>();

        // Validate stock and calculate total
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
            productRepository.save(product);
        }

        // Create and save order with payment details
        Order order = new Order(user, totalAmount.doubleValue(), orderItems);
        order.setStatus("PENDING");
        order.setPaymentMethod(paymentMethod);
        order.setPaymentStatus("PENDING");
        order = orderRepository.save(order);

        // Send confirmation email
        try {
            emailService.sendOrderConfirmationEmail(user.getEmail(), order);
        } catch (Exception e) {
            System.err.println("Failed to send order confirmation email: " + e.getMessage());
        }

        return order;
    }
}
