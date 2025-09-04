package com.lankafreshmart.market_store.service;

import com.lankafreshmart.market_store.model.Delivery;
import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.repository.DeliveryRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;
import java.time.LocalDateTime;

@Service
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final EmailService emailService;

    @Autowired
    public DeliveryService(DeliveryRepository deliveryRepository, EmailService emailService) {
        this.deliveryRepository = deliveryRepository;
        this.emailService = emailService;
    }

    @Transactional
    public Delivery createDelivery(Order order) {
        Delivery delivery = new Delivery(order);
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public void updateDeliveryStatus(Long deliveryId, String newStatus) throws IllegalStateException {
        System.out.println("Updating delivery ID: " + deliveryId + " to status: " + newStatus);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ID: " + deliveryId));
        delivery.setStatus(newStatus);
        deliveryRepository.save(delivery);

        // Send email notification
        try {
            emailService.sendDeliveryUpdateEmail(delivery.getOrder().getUser().getEmail(), delivery);
            System.out.println("Email sent for delivery update ID: " + deliveryId);
        } catch (MessagingException e) {
            System.err.println("Failed to send delivery update email: " + e.getMessage());
        }
    }
}
