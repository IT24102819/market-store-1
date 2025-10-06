package com.lankafreshmart.market_store.service;

import com.lankafreshmart.market_store.model.RoleRequest;
import com.lankafreshmart.market_store.model.User;
import com.lankafreshmart.market_store.repository.RoleRequestRepository;
import com.lankafreshmart.market_store.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public void register(User user) {
        if (!user.isAgreedToTerms()) {
            throw new IllegalStateException("User must agree to terms.");
        }
        // Custom uniqueness check before saving
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            throw new IllegalStateException("Username already taken.");
        }
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new IllegalStateException("Email already registered.");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole("USER");
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

    @Autowired
    private RoleRequestRepository roleRequestRepository;

    public void submitRoleRequest(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        if ("ADMIN".equals(user.getRole())) {
            throw new IllegalArgumentException("Admin users cannot request role change");
        }
        RoleRequest existingRequest = roleRequestRepository.findByUser(user)
                .stream().filter(r -> "PENDING".equals(r.getStatus()))
                .findFirst().orElse(null);
        if (existingRequest != null) {
            throw new IllegalArgumentException("You already have a pending request");
        }
        RoleRequest request = new RoleRequest();
        request.setUser(user);
        roleRequestRepository.save(request);
    }

    @Transactional
    @PreAuthorize("hasRole('ADMIN')")
    public void processRoleRequest(Long requestId, String action) {
        RoleRequest request = roleRequestRepository.findById(requestId)
                .orElseThrow(() -> new IllegalArgumentException("Request not found"));
        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Request is not pending");
        }
        if ("APPROVE".equals(action)) {
            request.getUser().setRole("ADMIN");
            userRepository.save(request.getUser());
            request.setStatus("APPROVED");
        } else if ("DENY".equals(action)) {
            request.setStatus("DENIED");
        } else {
            throw new IllegalArgumentException("Invalid action");
        }
        roleRequestRepository.save(request);
    }

    public List<RoleRequest> getPendingRequests() {
        List<RoleRequest> requests = roleRequestRepository.findByStatus("PENDING");
        System.out.println("Pending requests: " + requests.stream().map(r -> r.getId()).collect(Collectors.toList()));
        return requests;
    }

    public long getRegisteredUsersCount() {
        return userRepository.count();
    }
}