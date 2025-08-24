package com.lankafreshmart.market_store.service;

import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(User user){
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
        userRepository.save(user);
    }

    public void updateProfile(String currentUsername,User updatedUser){
        User  user = userRepository.findByUsername(currentUsername)
                .orElseThrow(() -> new IllegalArgumentException("Username not found"));

        // Update fields only if provided
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

    public void deleteAccount(String username){
        User  user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        userRepository.delete(user);
    }
}
