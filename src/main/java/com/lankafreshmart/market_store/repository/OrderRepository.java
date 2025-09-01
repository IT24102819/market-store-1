package com.lankafreshmart.market_store.repository;

import com.lankafreshmart.market_store.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {
}
