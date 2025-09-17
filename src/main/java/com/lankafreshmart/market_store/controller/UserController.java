package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(@Valid @ModelAttribute User user, BindingResult result, Model model) {
        if (!user.isAgreedToTerms()) {
            model.addAttribute("error", "You must agree to the Privacy Policy to register.");
            return "register";
        }
        if (result.hasErrors()) {
            return "register";
        }
        try {
            userService.register(user);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/login")
    public String showLoginForm() {
        return "login";
    }

    @GetMapping("/privacy")
    public String showPrivacyPolicy() {
        return "privacy";
    }

    @GetMapping("/profile")
    public String showProfile(Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        User user = userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        model.addAttribute("user", user);
        return "profile";
    }

    @PostMapping("/profile")
    public String updateProfile(@ModelAttribute User updatedUser, Model model) {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        try {
            userService.updateProfile(username, updatedUser);
            return "redirect:/profile?success=Profile updated successfully";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("user", updatedUser);
            return "profile";
        }
    }

    @PostMapping("/delete-account")
    public String deleteAccount() {
        String username = SecurityContextHolder.getContext().getAuthentication().getName();
        userService.deleteAccount(username);
        return "redirect:/logout";
    }

    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-dashboard";
    }

    @PostMapping("/admin/delete-user")
    public String deleteUser(@RequestParam("username") String username, Model model) {
        try {
            userService.deleteAccount(username);
            return "redirect:/admin/dashboard?success=User deleted successfully";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("users", userService.getAllUsers());
            return "admin-dashboard";
        }
    }
}