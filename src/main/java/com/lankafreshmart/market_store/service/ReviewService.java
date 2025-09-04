package com.lankafreshmart.market_store.service;

import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.model.Review;
import com.lankafreshmart.market_store.repository.ReviewRepository;
import com.lankafreshmart.market_store.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class ReviewService {
    private final ReviewRepository reviewRepository;
    private final OrderService orderService;
    private final ProductService productService;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository, OrderService orderService, ProductService productService) {
        this.reviewRepository = reviewRepository;
        this.orderService = orderService;
        this.productService = productService;
    }

    @Transactional
    public void submitReview(Long orderId, String comment, int rating) throws IllegalStateException {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!"DELIVERED".equals(order.getDelivery().getStatus())) {
            throw new IllegalStateException("Can only review delivered orders");
        }
        Product product = order.getOrderItems().get(0).getProduct(); // Assuming one product per order for simplicity
        Review review = new Review(order, product, comment, rating);
        reviewRepository.save(review);

        // Update product average rating
        updateProductRating(product);
    }

    private void updateProductRating(Product product) {
        double averageRating = reviewRepository.findByProduct(product)
                .stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        product.setRating(averageRating); // Add rating field to Product if not present
        productService.saveProduct(product);
    }

    public List<Review> getReviewsByProduct(Product product) {
        return reviewRepository.findByProduct(product);
    }

    public Review findById(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new IllegalArgumentException("Review not found"));
    }

    @Transactional
    public void updateReview(Long reviewId, String comment, int rating) {
        Review review = findById(reviewId);
        review.setComment(comment);
        review.setRating(rating);
        reviewRepository.save(review);
        updateProductRating(review.getProduct());
    }

    @Transactional
    public void deleteReview(Long reviewId) {
        Review review = findById(reviewId);
        Product product = review.getProduct();
        reviewRepository.delete(review);
        updateProductRating(product);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }
}
