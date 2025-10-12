package com.lankafreshmart.market_store.strategy;

import com.lankafreshmart.market_store.model.User;

public interface ValidationStrategy {
    void validate(User user, String secretCode) throws IllegalStateException;
}