package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.Review;
import com.lankafreshmart.market_store.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
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
    public String deleteReviewByAdmin(@RequestParam Long reviewId, Model model) {
        Review review = reviewService.findById(reviewId);
        Long productId = review.getProduct().getId();
        reviewService.deleteReview(reviewId);
        return "redirect:/admin/reviews";
    }
}