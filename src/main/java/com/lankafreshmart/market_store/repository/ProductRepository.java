package com.lankafreshmart.market_store.repository;



import com.lankafreshmart.market_store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStockQuantityLessThanEqual(int stockQuantity);
}
