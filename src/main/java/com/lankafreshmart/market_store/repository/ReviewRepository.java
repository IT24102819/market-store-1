package com.lankafreshmart.market_store.repository;

import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findByProduct(Product product);
}
