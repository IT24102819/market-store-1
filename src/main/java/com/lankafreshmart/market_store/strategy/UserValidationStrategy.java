package com.lankafreshmart.market_store.strategy;

import com.lankafreshmart.market_store.model.User;

public class UserValidationStrategy implements ValidationStrategy {

    @Override
    public void validate(User user, String secretCode) throws IllegalStateException {
        if (!user.isAgreedToTerms()) {
            throw new IllegalStateException("You must agree to the Privacy Policy to register.");
        }
        if (user.getUsername().matches("\\d+")) {
            throw new IllegalStateException("Username should not contain only numbers.");
        }
    }
}