package com.lankafreshmart.market_store.service;

import com.lankafreshmart.market_store.model.*;
import com.lankafreshmart.market_store.repository.CartItemRepository;
import com.lankafreshmart.market_store.repository.ProductRepository;
import com.lankafreshmart.market_store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final EmailService emailService;

    @Value("${app.low-stock-threshold}")
    private int lowStockThreshold;

    public CartService(CartItemRepository cartItemRepository, UserRepository userRepository,
                       ProductRepository productRepository, EmailService emailService) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.emailService = emailService;
    }

    public List<CartItem> getCartItems() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return cartItemRepository.findByUser(user);
    }

    public BigDecimal getCartTotal() {
        List<CartItem> cartItems = getCartItems();
        return cartItems.stream()
                .map(item -> item.getProduct().getPrice().multiply(BigDecimal.valueOf(item.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addToCart(Long productId, int quantity) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > product.getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity (" + quantity + ") exceeds available stock (" + product.getStockQuantity() + ")");
        }

        CartItem existingItem = cartItemRepository.findByUserAndProductId(user, productId);
        if (existingItem != null) {
            int newQuantity = existingItem.getQuantity() + quantity;
            if (newQuantity > product.getStockQuantity()) {
                throw new IllegalArgumentException("Total quantity (" + newQuantity + ") exceeds available stock (" + product.getStockQuantity() + ")");
            }
            existingItem.setQuantity(newQuantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItem.setUser(user); // Set the user here
            cartItemRepository.save(cartItem);
        }
        // Check for low stock after adding to cart
        if (product.getStockQuantity() - quantity <= lowStockThreshold) {
            try {
                emailService.sendLowStockEmail(product);
            } catch (MessagingException e) {
                System.err.println("Failed to send low stock email for product " + product.getName() + ": " + e.getMessage());
            }
        }
    }

    public void updateCartItem(Long cartItemId, int quantity) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!cartItem.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized: Cannot update another user's cart");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        if (quantity > cartItem.getProduct().getStockQuantity()) {
            throw new IllegalArgumentException("Requested quantity (" + quantity + ") exceeds available stock (" + cartItem.getProduct().getStockQuantity() + ")");
        }
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
        // Check for low stock after updating cart
        if (cartItem.getProduct().getStockQuantity() <= lowStockThreshold) {
            try {
                emailService.sendLowStockEmail(cartItem.getProduct());
            } catch (MessagingException e) {
                System.err.println("Failed to send low stock email for product " + cartItem.getProduct().getName() + ": " + e.getMessage());
            }
        }
    }

    public void removeFromCart(Long cartItemId) {
        CartItem cartItem = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("Cart item not found"));
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        if (!cartItem.getUser().getUsername().equals(username)) {
            throw new IllegalArgumentException("Unauthorized: Cannot remove another user's cart item");
        }
        cartItemRepository.delete(cartItem);
    }
}