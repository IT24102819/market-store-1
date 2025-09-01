package com.lankafreshmart.market_store.service;


import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.repository.ProductRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class ProductService {
    private final ProductRepository productRepository;

    @Value("${app.low-stock-threshold}")
    private int lowStockThreshold;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public Page<Product> getAllProducts(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        return productRepository.findAll(pageable);
    }

    public Optional<Product> getProductById(Long id) {
        return productRepository.findById(id);
    }

    public List<Product> getLowStockProducts() {
        return productRepository.findByStockQuantityLessThanEqual(lowStockThreshold);
    }

    public void createProduct(Product product) {
        if (product.getName() == null || product.getName().isEmpty()) {
            throw new IllegalArgumentException("Product name is required");
        }
        if (product.getPrice() == null || product.getPrice().signum() <= 0) {
            throw new IllegalArgumentException("Price must be positive");
        }
        if (product.getImageUrl() != null && product.getImageUrl().length() > 500) {
            throw new IllegalArgumentException("Image URL must be 500 characters or less");
        }
        if (product.getStockQuantity() < 0) {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        productRepository.save(product);
    }

    public void updateProduct(Long id, Product product) {
        Product existing = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (product.getName() != null && !product.getName().isEmpty()) {
            existing.setName(product.getName());
        }
        if (product.getPrice() != null && product.getPrice().signum() > 0) {
            existing.setPrice(product.getPrice());
        }
        if (product.getDescription() != null) {
            existing.setDescription(product.getDescription());
        }
        if (product.getCategory() != null) {
            existing.setCategory(product.getCategory());
        }
        if (product.getImageUrl() != null) {
            if (product.getImageUrl().length() > 500) {
                throw new IllegalArgumentException("Image URL must be 500 characters or less");
            }
            existing.setImageUrl(product.getImageUrl());
        }
        if (product.getStockQuantity() >= 0) {
            existing.setStockQuantity(product.getStockQuantity());
        } else {
            throw new IllegalArgumentException("Stock quantity cannot be negative");
        }
        productRepository.save(existing);
    }

    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        productRepository.delete(product);
    }
}
