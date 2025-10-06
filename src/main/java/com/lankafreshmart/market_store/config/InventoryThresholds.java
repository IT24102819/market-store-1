package com.lankafreshmart.market_store.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "inventory.threshold")
public class InventoryThresholds {
    private int fastMoving;
    private int slowMoving;

    public int getFastMoving() {
        return fastMoving;
    }

    public void setFastMoving(int fastMoving) {
        this.fastMoving = fastMoving;
    }

    public int getSlowMoving() {
        return slowMoving;
    }

    public void setSlowMoving(int slowMoving) {
        this.slowMoving = slowMoving;
    }
}