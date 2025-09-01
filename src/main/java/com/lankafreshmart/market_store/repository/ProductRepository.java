package com.lankafreshmart.market_store.repository;



import com.lankafreshmart.market_store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductRepository extends JpaRepository<Product, Long> {
}
