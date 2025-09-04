package com.lankafreshmart.market_store.repository;

import com.lankafreshmart.market_store.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
