package com.lankafreshmart.market_store.strategy;

import com.lankafreshmart.market_store.model.User;
import org.springframework.beans.factory.annotation.Value;


public class AdminValidationStrategy implements ValidationStrategy {
    @Value("${admin.secret.code:ADMIN2025}")
    private String adminSecretCode;

    @Override
    public void validate(User user, String secretCode) throws IllegalStateException {
        if (!user.isAgreedToTerms()) {
            throw new IllegalStateException("You must agree to the Privacy Policy to register.");
        }
        if (user.getUsername().matches("\\d+")) {
            throw new IllegalStateException("Username should not contain only numbers.");
        }
        if (secretCode == null || secretCode.trim().isEmpty()) {
            throw new IllegalStateException("Admin secret code is required.");
        }
        if (!adminSecretCode.equals(secretCode)) {
            throw new IllegalStateException("Invalid admin secret code! Cannot Register as Admin.");
        }
    }
}