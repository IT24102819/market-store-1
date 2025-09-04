package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.service.ReviewService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ReviewController {

    private final ReviewService reviewService;

    @Autowired
    public ReviewController(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    @GetMapping("/review")
    public String showReviewForm(@RequestParam Long orderId, Model model) {
        model.addAttribute("orderId", orderId);
        return "review";
    }

    @PostMapping("/submit-review")
    public String submitReview(@RequestParam Long orderId, @RequestParam String comment, @RequestParam int rating, Model model) {
        try {
            reviewService.submitReview(orderId, comment, rating);
            model.addAttribute("message", "Review submitted successfully!");
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "review";
    }
}
