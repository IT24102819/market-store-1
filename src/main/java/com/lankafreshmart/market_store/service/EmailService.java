package com.lankafreshmart.market_store.service;

import com.lankafreshmart.market_store.model.Product;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;

    @Value("${app.admin-email}")
    private String adminEmail;

    public EmailService(JavaMailSender mailSender, TemplateEngine templateEngine) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
    }

    public void sendLowStockEmail(Product product) throws MessagingException {
        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        // Set email details
        helper.setTo(adminEmail);
        helper.setSubject("Low Stock Alert: " + product.getName());
        helper.setFrom("no-reply@lankafreshmart.com");

        // Prepare Thymeleaf context
        Context context = new Context();
        context.setVariable("productName", product.getName());
        context.setVariable("stockQuantity", product.getStockQuantity());
        context.setVariable("threshold", 10); // Matches app.low-stock-threshold

        // Render email template
        String htmlContent = templateEngine.process("email/low-stock-email", context);
        helper.setText(htmlContent, true);

        // Send email
        mailSender.send(message);
    }
}
