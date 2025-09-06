package com.lankafreshmart.market_store.service;

import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.model.Review;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.repository.ReviewRepository;
import com.lankafreshmart.market_store.repository.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
    public void submitReview(Long orderId, String comment, int rating, User user) throws IllegalStateException {
        Order order = orderService.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Order not found"));
        if (!"DELIVERED".equals(order.getDelivery().getStatus())) {
            throw new IllegalStateException("Can only review delivered orders");
        }
        Product product = order.getOrderItems().get(0).getProduct();
        Review review = new Review(order, product, comment, rating, user); // Pass user
        reviewRepository.save(review);
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
    public void deleteReview(Long reviewId, User user) {
        Review review = findById(reviewId);
        if (review.getUser() == null) {
            throw new IllegalArgumentException("Invalid review data: User not found");
        }
        if (!review.getUser().equals(user)) {
            throw new IllegalArgumentException("You can only delete your own review");
        }
        Product product = review.getProduct();
        if (product == null) {
            throw new IllegalArgumentException("Invalid review data: Product not found");
        }
        reviewRepository.delete(review);
        updateProductRating(product);
    }

    @Transactional
    public void deleteReviewByAdmin(Long reviewId) {
        Review review = findById(reviewId);
        if (review.getUser() == null) {
            throw new IllegalArgumentException("Invalid review data: User not found");
        }
        Product product = review.getProduct();
        if (product == null) {
            throw new IllegalArgumentException("Invalid review data: Product not found");
        }
        reviewRepository.delete(review);
        updateProductRating(product);
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAll();
    }

    public List<Review> getReviewsByUser(User user) {
        System.out.println("Fetching reviews for user ID: " + user.getId());
        List<Review> allReviews = reviewRepository.findAll();
        System.out.println("Total reviews in DB: " + allReviews.size());
        List<Review> userReviews = allReviews.stream()
                .filter(review -> review != null && review.getUser() != null && review.getUser().getId() != null && review.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
        System.out.println("User reviews found: " + userReviews.size());
        return userReviews;
    }
}
