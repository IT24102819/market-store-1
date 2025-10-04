package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.service.UserService;
import jakarta.validation.Valid;
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

    /*
    @GetMapping("/admin/dashboard")
    public String showAdminDashboard(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        return "admin-dashboard";
    }

     */

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

    @PostMapping("/profile/request-role")
    public String requestRole(@AuthenticationPrincipal User user, Model model) {
        try {
            userService.submitRoleRequest(user.getId());
            model.addAttribute("success", "Role request submitted successfully!");
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("user", user);
        return "profile";
    }

    @GetMapping("/admin/requests")
    @PreAuthorize("hasRole('ADMIN')")
    public String showRequests(Model model) {
        model.addAttribute("requests", userService.getPendingRequests());
        return "admin-requests";
    }

    @PostMapping("/admin/process-request/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public String processRequest(@PathVariable Long id, @RequestParam String action, Model model) {
        System.out.println("Processing request for id: " + id + " with action: " + action);
        try {
            userService.processRoleRequest(id, action);
            model.addAttribute("success", "Request " + action.toLowerCase() + "d successfully!");
        } catch (IllegalArgumentException | IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
        }
        model.addAttribute("requests", userService.getPendingRequests());
        return "admin-requests";
    }
}