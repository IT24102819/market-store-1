package com.lankafreshmart.market_store.service;


import com.lankafreshmart.market_store.model.Delivery;
import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailAuthenticationException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;
import org.thymeleaf.exceptions.TemplateInputException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.admin-email:admin@lankafreshmart.com}")
    private String adminEmail;

    @Value("${app.base-url:http://localhost:8080/market-store}")
    private String baseUrl;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendLowStockEmail(Product product) throws MessagingException {
        if (adminEmail == null || adminEmail.isEmpty()) {
            System.err.println("Admin email not configured, skipping email for product: " + product.getName());
            return;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(adminEmail);
        helper.setSubject("Low Stock Alert: " + product.getName());
        helper.setFrom("no-reply@lankafreshmart.com");

        Context context = new Context();
        context.setVariable("productName", product.getName());
        context.setVariable("stockQuantity", product.getStockQuantity());
        context.setVariable("threshold", 10);
        context.setVariable("adminUrl", baseUrl + "/admin/products");

        try {
            String htmlContent = templateEngine.process("email/low-stock-email", context);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (TemplateInputException e) {
            System.err.println("Failed to process email template for product " + product.getName() + ": " + e.getMessage());
            helper.setText("Low Stock Alert: Product " + product.getName() + " has " + product.getStockQuantity() + " units left.", false);
            mailSender.send(message);
        } catch (MailAuthenticationException e) {
            System.err.println("Failed to authenticate with SMTP server for product " + product.getName() + ": " + e.getMessage());
            throw e;
        }
    }

    public void sendOrderConfirmationEmail(String userEmail, Order order) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(userEmail);
        helper.setSubject("Order Confirmation - Order #" + order.getId());
        helper.setFrom("no-reply@lankafreshmart.com");

        Context context = new Context();
        context.setVariable("orderId", order.getId());
        context.setVariable("orderDate", order.getOrderDate());
        context.setVariable("totalAmount", order.getTotalAmount());
        context.setVariable("orderItems", order.getOrderItems());
        context.setVariable("baseUrl", baseUrl);

        try {
            String htmlContent = templateEngine.process("email/order-confirmation", context);
            helper.setText(htmlContent, true);
            mailSender.send(message);
        } catch (TemplateInputException e) {
            System.err.println("Failed to process order confirmation email for order #" + order.getId() + ": " + e.getMessage());
            helper.setText("Order Confirmation: Order #" + order.getId() + " placed on " + order.getOrderDate() + " for LKR " + order.getTotalAmount() + ".", false);
            mailSender.send(message);
        } catch (MailAuthenticationException e) {
            System.err.println("Failed to authenticate with SMTP server for order #" + order.getId() + ": " + e.getMessage());
            throw e;
        }
    }

    public void sendDeliveryUpdateEmail(String to, Delivery delivery) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true);
        helper.setTo(to);
        helper.setSubject("Delivery Update - Lanka Fresh Mart");
        String statusMessage = switch (delivery.getStatus()) {
            case "SHIPPED" -> "Your order has been shipped! Tracking number: " + (delivery.getTrackingNumber() != null ? delivery.getTrackingNumber() : "Not available yet") +
                    ". Estimated delivery: " + delivery.getEstimatedDeliveryDate();
            case "OUT_FOR_DELIVERY" -> "Your order is out for delivery! Tracking number: " + (delivery.getTrackingNumber() != null ? delivery.getTrackingNumber() : "Not available yet");
            case "DELIVERED" -> "Your order has been delivered! Thank you for shopping with us!";
            case "CANCELLED" -> "Your order has been cancelled. Contact support if you have questions.";
            default -> "Your order is " + delivery.getStatus() + ". Check back for updates.";
        };
        helper.setText("<h1>Delivery Update</h1>" +
                "<p>Order ID: " + delivery.getOrder().getId() + "</p>" +
                "<p>" + statusMessage + "</p>", true);
        mailSender.send(message);
    }
}
