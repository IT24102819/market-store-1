package com.lankafreshmart.market_store.service;

import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    @Value("${admin.secret.code:ADMIN2025}")
    private String adminSecretCode;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(User user, String secretCode) {
        if (!user.isAgreedToTerms()) {
            throw new IllegalStateException("User must agree to terms.");
        }

        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already taken.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered.");
        }
        if (user.getUsername().matches("\\d+")) {
            throw new IllegalArgumentException("Username should not contain only numbers");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));

        String role = "USER";

        if (adminSecretCode.equals(secretCode)) {
            role = "ADMIN";
        }
        user.setRole(role);
        userRepository.save(user);
    }

    public void updateProfile(String currentUsername, User updatedUser) {
        User user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Username not found"));

        if (updatedUser.getUsername() != null && !updatedUser.getUsername().isEmpty()) {
            if (!updatedUser.getUsername().equals(currentUsername) &&
                    userRepository.findByUsername(updatedUser.getUsername()).isPresent()) {
                throw new IllegalArgumentException("Username already taken");
            }
            user.setUsername(updatedUser.getUsername());
        }
        if (updatedUser.getEmail() != null && !updatedUser.getEmail().isEmpty()) {
            user.setEmail(updatedUser.getEmail());
        }
        if (updatedUser.getPassword() != null && !updatedUser.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(updatedUser.getPassword()));
        }
        userRepository.save(user);
    }

    public void deleteAccount(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
    }

    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public long getRegisteredUsersCount() {
        return userRepository.count();
    }
}