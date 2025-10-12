package com.lankafreshmart.market_store.config;

import com.lankafreshmart.market_store.strategy.UserValidationStrategy;
import com.lankafreshmart.market_store.strategy.ValidationStrategy;
import com.lankafreshmart.market_store.strategy.AdminValidationStrategy;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public ValidationStrategy userValidationStrategy() {
        return new UserValidationStrategy();
    }

    @Bean
    public ValidationStrategy adminValidationStrategy() {
        return new AdminValidationStrategy();
    }
}