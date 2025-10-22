package com.lankafreshmart.market_store.service;

import com.lankafreshmart.market_store.config.InventoryThresholds;
import com.lankafreshmart.market_store.model.Sale;
import com.lankafreshmart.market_store.repository.SaleRepository;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class SaleService {
    private final SaleRepository saleRepository;
    private final InventoryThresholds thresholds;

    public SaleService(SaleRepository saleRepository, InventoryThresholds thresholds) {
        this.saleRepository = saleRepository;
        this.thresholds = thresholds;
    }

    public Sale createSale(Sale sale) {
        return saleRepository.save(sale);
    }

    public List<Sale> getAllSales() {
        return saleRepository.findAll();
    }

    public Sale updateSale(Long id, Sale saleDetails) {
        Sale sale = saleRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));
        sale.setAmount(saleDetails.getAmount());
        sale.setSaleDate(saleDetails.getSaleDate());
        return saleRepository.save(sale);
    }

    public void deleteSale(Long id) {
        saleRepository.deleteById(id);
    }

    public BigDecimal getTotalSales(LocalDateTime start, LocalDateTime end) {
        BigDecimal total = saleRepository.getTotalSales(start, end);
        System.out.println("Total Sales: " + total);
        return total != null ? total : BigDecimal.ZERO;
    }

    public List<Object[]> getFastMovingItems(LocalDateTime start, LocalDateTime end) {
        List<Object[]> fastMoving = saleRepository.getFastMovingItemsThreshold(start, end);
        System.out.println("Fast Moving: " + fastMoving);
        return fastMoving;
    }

    public List<Object[]> getSlowMovingItems(LocalDateTime start, LocalDateTime end) {
        List<Object[]> slowMoving = saleRepository.getSlowMovingItemsThreshold(start, end, 30);
        System.out.println("Slow Moving: " + slowMoving);
        return slowMoving;
    }

    public List<Sale> getSalesInRange(LocalDateTime start, LocalDateTime end) {
        return saleRepository.findBySaleDateBetween(start, end);
    }

    public List<Object[]> getDailySales(LocalDateTime start, LocalDateTime end) {
        List<Object[]> dailySales = saleRepository.findDailySales(start, end);
        System.out.println("Daily Sales: " + dailySales);
        return dailySales;
    }

    public List<Object[]> getProductPerformance(LocalDateTime start, LocalDateTime end) {
        List<Object[]> productPerformance = saleRepository.findProductPerformance(start, end);
        System.out.println("Product Performance: " + productPerformance);
        return productPerformance;
    }

    public List<Object[]> getMonthlySales(LocalDateTime start, LocalDateTime end) {
        List<Object[]> monthlySales = saleRepository.findMonthlySales(start, end);
        System.out.println("Monthly Sales: " + monthlySales);
        return monthlySales;
    }
}