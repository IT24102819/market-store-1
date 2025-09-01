package com.lankafreshmart.market_store.service;


import com.lankafreshmart.market_store.model.CartItem;
import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.repository.CartItemRepository;
import com.lankafreshmart.market_store.repository.ProductRepository;
import com.lankafreshmart.market_store.repository.UserRepository;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service
public class CartService {
    private final CartItemRepository cartItemRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, UserRepository userRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    public List<CartItem> getCartItems() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        return cartItemRepository.findByUser(user);
    }

    // New: Calculate total price of cart items
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

        CartItem existingItem = cartItemRepository.findByUserAndProductId(user, productId);
        if (existingItem != null) {
            existingItem.setQuantity(existingItem.getQuantity() + quantity);
            cartItemRepository.save(existingItem);
        } else {
            CartItem cartItem = new CartItem();
            cartItem.setUser(user);
            cartItem.setProduct(product);
            cartItem.setQuantity(quantity);
            cartItemRepository.save(cartItem);
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
        cartItem.setQuantity(quantity);
        cartItemRepository.save(cartItem);
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
