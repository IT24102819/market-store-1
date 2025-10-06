package com.lankafreshmart.market_store.repository;

import com.lankafreshmart.market_store.model.Sale;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface SaleRepository extends JpaRepository<Sale, Long> {
    List<Sale> findBySaleDateBetween(LocalDateTime start, LocalDateTime end);

    @Query("SELECT SUM(s.amount) FROM Sale s WHERE s.saleDate BETWEEN :start AND :end")
    BigDecimal getTotalSales(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT oi.product.name, SUM(oi.quantity) AS sold FROM Order o JOIN o.orderItems oi JOIN Sale s ON s.order.id = o.id WHERE s.saleDate BETWEEN :start AND :end GROUP BY oi.product.name HAVING SUM(oi.quantity) > 30 ORDER BY sold DESC")
    List<Object[]> getFastMovingItemsThreshold(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end);

    @Query("SELECT p.name, COALESCE(SUM(oi.quantity), 0) AS sold FROM Product p LEFT JOIN OrderItem oi ON p.id = oi.product.id LEFT JOIN oi.order o LEFT JOIN Sale s ON s.order.id = o.id WHERE s.saleDate BETWEEN :start AND :end OR s.id IS NULL GROUP BY p.name HAVING COALESCE(SUM(oi.quantity), 0) < :threshold ORDER BY sold ASC")
    List<Object[]> getSlowMovingItemsThreshold(@Param("start") LocalDateTime start, @Param("end") LocalDateTime end, @Param("threshold") int threshold);
}

