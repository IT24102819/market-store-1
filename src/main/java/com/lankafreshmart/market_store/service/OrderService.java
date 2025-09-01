package com.lankafreshmart.market_store.service;



import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.OrderItem;
import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.repository.OrderRepository;
import com.lankafreshmart.market_store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    public Order createOrder(User user, List<CartItem> cartItems) throws IllegalStateException {
        double totalAmount = 0.0;
        List<OrderItem> orderItems = new ArrayList<>();

        // Validate stock and calculate total
        for (CartItem cartItem : cartItems) {
            Product product = cartItem.getProduct();
            int requestedQuantity = cartItem.getQuantity();
            if (product.getStockQuantity() < requestedQuantity) {
                throw new IllegalStateException("Insufficient stock for " + product.getName());
            }
            totalAmount += product.getPrice() * requestedQuantity;
            orderItems.add(new OrderItem(product, requestedQuantity, product.getPrice()));
            // Update stock
            product.setStockQuantity(product.getStockQuantity() - requestedQuantity);
            productRepository.save(product);
        }

        // Create and save order
        Order order = new Order(user, totalAmount, orderItems);
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
