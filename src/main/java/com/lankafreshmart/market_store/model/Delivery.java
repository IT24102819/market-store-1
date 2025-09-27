package com.lankafreshmart.market_store.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "delivery")
public class Delivery {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @Column(nullable = false)
    private String status;

    private String trackingNumber;

    private LocalDateTime estimatedDeliveryDate;

    @Column(name = "address", length = 500) // Nullable so Pick Up can be null
    private String address;

    // Constructors
    public Delivery() {}
    public Delivery(Order order) {
        this.order = order;
    }

    // Getters and setters (existing ones unchanged)
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getTrackingNumber() { return trackingNumber; }
    public void setTrackingNumber(String trackingNumber) { this.trackingNumber = trackingNumber; }
    public LocalDateTime getEstimatedDeliveryDate() { return estimatedDeliveryDate; }
    public void setEstimatedDeliveryDate(LocalDateTime estimatedDeliveryDate) { this.estimatedDeliveryDate = estimatedDeliveryDate; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
}