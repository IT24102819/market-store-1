package com.lankafreshmart.market_store.repository;


import com.lankafreshmart.market_store.model.CartItem;
import com.lankafreshmart.market_store.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUser(User user);
    CartItem findByUserAndProductId(User user, Long productId);
}
