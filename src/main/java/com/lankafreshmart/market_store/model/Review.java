package com.lankafreshmart.market_store.model;

import jakarta.persistence.*;

@Entity
@Table(name = "reviews")
public class Review {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(nullable = false)
    private String comment;

    @Column(nullable = false)
    private int rating; // 1 to 5

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // New direct user relationship

    // Constructors
    public Review() {}
    public Review(Order order, Product product, String comment, int rating, User user) {
        this.order = order;
        this.product = product;
        this.comment = comment;
        this.rating = rating;
        this.user = user;
    }

    // Getters and setters...
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Order getOrder() { return order; }
    public void setOrder(Order order) { this.order = order; }
    public Product getProduct() { return product; }
    public void setProduct(Product product) { this.product = product; }
    public String getComment() { return comment; }
    public void setComment(String comment) { this.comment = comment; }
    public int getRating() { return rating; }
    public void setRating(int rating) { this.rating = rating; }
    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}