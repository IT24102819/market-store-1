package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
public class UserController {
    private final UserService userService;

    @Value("${admin.secret.code:ADMIN2025}")
    private String adminSecretCode;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "register";
    }

    @PostMapping("/register")
    public String registerUser(
            @Valid @ModelAttribute User user,
            BindingResult result,
            Model model) {
        if (!user.isAgreedToTerms()) {
            model.addAttribute("error", "You must agree to the Privacy Policy to register.");
            return "register";
        }
        if (result.hasErrors()) {
            return "register";
        }

        user.setRole("USER");
        try {
            userService.register(user, null);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "register";
        }
    }

    @GetMapping("/admin-register")
    public String showAdminRegisterForm(Model model) {
        model.addAttribute("user", new User());
        return "admin-register";
    }

    @PostMapping("/admin-register")
    public String registerAdmin(@Valid @ModelAttribute User user, BindingResult result, @RequestParam String secretCode,
            Model model) {
        if (!user.isAgreedToTerms()) {
            model.addAttribute("error", "You must agree to the Privacy Policy to register.");
            return "admin-register";
        }
        if (result.hasErrors()) {
            return "admin-register";
        }
        if (secretCode == null || secretCode.trim().isEmpty()) {
            model.addAttribute("error", "Admin secret code is required.");
            return "admin-register";
        }
        if (!adminSecretCode.equals(secretCode)) {
            model.addAttribute("error", "Invalid admin secret code!Cannot Register as Admin.");
            return "admin-register";
        }

        user.setRole("ADMIN");
        try {
            userService.register(user, secretCode);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", "Registration failed: " + e.getMessage());
            return "admin-register";
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