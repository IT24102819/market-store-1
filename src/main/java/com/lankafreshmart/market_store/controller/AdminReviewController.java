package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.Review;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class AdminReviewController {

    private final ReviewService reviewService;

    @Autowired
    public AdminReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/admin/reviews")
    @PreAuthorize("hasRole('ADMIN')")
    public String listReviews(Model model) {
        List<Review> reviews = reviewService.getAllReviews();
        model.addAttribute("reviews", reviews);
        return "admin-reviews";
    }

    @GetMapping("/admin/review/delete")
    @PreAuthorize("hasRole('ADMIN')")
    public String deleteReviewByAdmin(@RequestParam Long reviewId, @AuthenticationPrincipal User user, Model model) {
        Review review = reviewService.findById(reviewId);
        if (review == null) {
            throw new IllegalArgumentException("Review not found");
        }
        Long productId = review.getProduct() != null && review.getProduct().getId() != null ? review.getProduct().getId() : null;
        if (productId == null) {
            throw new IllegalArgumentException("Invalid review data: Product not found");
        }
        reviewService.deleteReviewByAdmin(reviewId); // Use the new admin method
        return "redirect:/admin/reviews?productId=" + productId;
    }
}