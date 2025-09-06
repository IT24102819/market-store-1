package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.model.Review;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.service.ProductService;
import com.lankafreshmart.market_store.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ReviewController {

    private final ReviewService reviewService;
    private final ProductService productService;

    @Autowired
    public ReviewController(ReviewService reviewService, ProductService productService) {
        this.reviewService = reviewService;
        this.productService = productService;
    }

    @GetMapping("/review")
    public String showReviewForm(@RequestParam Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "review";
    }

    @PostMapping("/submit-review")
    public String submitReview(@RequestParam Long orderId, @RequestParam String comment, @RequestParam int rating, @AuthenticationPrincipal User user, Model model) {
        try {
            reviewService.submitReview(orderId, comment, rating, user); // Pass user
            model.addAttribute("message", "Review submitted successfully!");
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "review";
    }

    @GetMapping("/product-reviews")
    public String showProductReviews(@RequestParam Long productId, Model model) {
        Product product = productService.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        List<Review> reviews = reviewService.getReviewsByProduct(product);
        double averageRating = reviews.stream()
                .mapToInt(Review::getRating)
                .average()
                .orElse(0.0);
        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("averageRating", averageRating);
        return "product-reviews";
    }

    @PostMapping("/review/update")
    public String updateReview(@RequestParam Long reviewId, @RequestParam String comment, @RequestParam int rating, @AuthenticationPrincipal User user, Model model) {
        Review review = reviewService.findById(reviewId); // Should throw IllegalArgumentException if not found
        if (review.getUser() == null) {
            throw new IllegalArgumentException("Invalid review data: User not found");
        }
        System.out.println("Review User ID: " + (review.getUser() != null ? review.getUser().getId() : "null"));
        System.out.println("Logged-in User ID: " + (user != null ? user.getId() : "null"));
        if (review.getUser().getId() == null || !review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only update your own review");
        }
        if (review.getProduct() == null || review.getProduct().getId() == null) {
            throw new IllegalArgumentException("Invalid review data: Product not found");
        }
        try {
            reviewService.updateReview(reviewId, comment, rating, user);
            model.addAttribute("message", "Review updated successfully!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return "edit-review"; // Return to form with error
        }
        return "redirect:/product-reviews?productId=" + review.getProduct().getId();
    }

    @GetMapping("/review/delete")
    public String deleteReview(@RequestParam Long reviewId, @AuthenticationPrincipal User user, Model model) {
        Review review = reviewService.findById(reviewId);
        if (review.getUser() == null) {
            throw new IllegalArgumentException("Invalid review data: User not found");
        }
        System.out.println("Review User ID: " + (review.getUser() != null ? review.getUser().getId() : "null"));
        System.out.println("Logged-in User ID: " + (user != null ? user.getId() : "null"));
        if (review.getUser().getId() == null || !review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only delete your own review");
        }
        if (review.getProduct() == null || review.getProduct().getId() == null) {
            throw new IllegalArgumentException("Invalid review data: Product not found");
        }
        Long productId = review.getProduct().getId();
        reviewService.deleteReview(reviewId, user); // Pass the user parameter
        return "redirect:/product-reviews?productId=" + productId;
    }

    @GetMapping("/your-reviews")
    public String yourReviews(@AuthenticationPrincipal User user, Model model) {
        List<Review> reviews = reviewService.getReviewsByUser(user);
        model.addAttribute("reviews", reviews);
        return "your-reviews";
    }

    @GetMapping("/review/edit")
    public String showEditReviewForm(@RequestParam Long reviewId, @AuthenticationPrincipal User user, Model model) {
        Review review = reviewService.findById(reviewId);
        System.out.println("Review User ID: " + (review.getUser() != null ? review.getUser().getId() : "null"));
        System.out.println("Logged-in User ID: " + (user != null ? user.getId() : "null"));
        if (review.getUser() == null) {
            throw new IllegalArgumentException("Invalid review data: User not found");
        }
        if (review.getUser().getId() == null || !review.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("You can only edit your own review");
        }
        model.addAttribute("review", review);
        return "edit-review";
    }
}
