package com.lankafreshmart.market_store.service;

import com.lankafreshmart.market_store.model.Delivery;
import com.lankafreshmart.market_store.model.Order;
import com.lankafreshmart.market_store.model.Sale;
import com.lankafreshmart.market_store.repository.DeliveryRepository;
import com.lankafreshmart.market_store.repository.OrderRepository;
import jakarta.mail.AuthenticationFailedException;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.mail.MessagingException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class DeliveryService {
    private final DeliveryRepository deliveryRepository;
    private final EmailService emailService;
    private final OrderRepository orderRepository;
    private final SaleService saleService;

    @Autowired
    public DeliveryService(DeliveryRepository deliveryRepository, EmailService emailService,OrderRepository orderRepository, SaleService saleService) {
        this.deliveryRepository = deliveryRepository;
        this.emailService = emailService;
        this.orderRepository = orderRepository;
        this.saleService = saleService;
    }

    private String generateTrackingNumber() {
        String characters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder trackingNumber = new StringBuilder("TRK");
        Random random = new Random();
        for (int i = 0; i < 7; i++) {
            trackingNumber.append(characters.charAt(random.nextInt(characters.length())));
        }
        return trackingNumber.toString();
    }

    @Transactional
    public Delivery createDelivery(Order order, String address) {
        Delivery delivery = new Delivery(order);
        String trackingNumber = generateTrackingNumber();
        delivery.setTrackingNumber(trackingNumber);
        if (address != null && !address.trim().isEmpty()) {
            delivery.setAddress(address);
        }
        return deliveryRepository.save(delivery);
    }

    @Transactional
    public void updateDeliveryStatus(Long deliveryId, String newStatus) throws IllegalStateException {
        System.out.println("Starting update for delivery ID: " + deliveryId + " to status: " + newStatus);
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ID: " + deliveryId));
        System.out.println("Current status before update: " + delivery.getStatus());
        delivery.setStatus(newStatus);
        System.out.println("New status set to: " + delivery.getStatus());

        if ("SHIPPED".equals(newStatus) && delivery.getTrackingNumber() == null) {
            String trackingNumber = generateTrackingNumber();
            delivery.setTrackingNumber(trackingNumber);
            System.out.println("Generated tracking number: " + trackingNumber);
        }

        deliveryRepository.save(delivery);
        deliveryRepository.flush();

        try {
            System.out.println("Attempting to send email for delivery ID: " + deliveryId);
            emailService.sendDeliveryUpdateEmail(delivery.getOrder().getUser().getEmail(), delivery);
            System.out.println("Email sent successfully for delivery ID: " + deliveryId);
        } catch (MessagingException e) {
            e.printStackTrace();
            System.err.println("Email sending failed for delivery ID: " + deliveryId + ": " + e.getMessage());
        }
    }

    @PersistenceContext
    private EntityManager entityManager;

    @Transactional(readOnly = true)
    public Delivery getDelivery(Long deliveryId) {
        return deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ID: " + deliveryId));
    }

    @Transactional
    public void deleteDelivery(Long deliveryId) {
        Delivery delivery = deliveryRepository.findById(deliveryId)
                .orElseThrow(() -> new IllegalArgumentException("Delivery not found with ID: " + deliveryId));
        Order order = delivery.getOrder();
        if (order != null) {
            order.setDelivery(null);
            orderRepository.save(order);
        }
        System.out.println("Deleting delivery with ID: " + deliveryId);
        deliveryRepository.delete(delivery);
        deliveryRepository.flush();
        System.out.println("Deletion attempted for delivery ID: " + deliveryId);
    }

    public long getCompletedDeliveriesCount() {
        return deliveryRepository.countByStatus("DELIVERED");
    }

    public long getPendingDeliveriesCount() {
        return deliveryRepository.countByStatus("PENDING");
    }

    public long getShippedDeliveriesCount() {
        return deliveryRepository.countByStatus("SHIPPED");
    }

    public long getCancelledDeliveriesCount() {
        return deliveryRepository.countByStatus("CANCELLED");
    }
}