package com.lankafreshmart.market_store.repository;

import com.lankafreshmart.market_store.model.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findByStockQuantityLessThanEqual(int stockQuantity);
    List<Product> findByNameContainingIgnoreCase(String name);
    List<Product> findByCategoryIgnoreCase(String category);
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    List<Product> findByNameContainingIgnoreCaseAndPriceBetweenAndCategoryIgnoreCase(
            String name, BigDecimal minPrice, BigDecimal maxPrice, String category);
}