package com.lankafreshmart.market_store.service;


import com.lankafreshmart.market_store.model.Product;
import org.springframework.beans.factory.annotation.Value;
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
        }
    }
}
