package com.lankafreshmart.market_store.repository;

import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByUser(User user);
}
