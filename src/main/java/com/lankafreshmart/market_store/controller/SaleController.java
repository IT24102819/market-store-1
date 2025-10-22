package com.lankafreshmart.market_store.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lankafreshmart.market_store.model.Sale;
import com.lankafreshmart.market_store.repository.SaleRepository;
import com.lankafreshmart.market_store.service.SaleService;
import com.opencsv.CSVWriter;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/sales")
public class SaleController {
    private final SaleService saleService;
    private final SaleRepository saleRepository;
    private final ObjectMapper objectMapper;

    public SaleController(SaleService saleService, SaleRepository saleRepository, ObjectMapper objectMapper) {
        this.saleService = saleService;
        this.saleRepository = saleRepository;
        this.objectMapper = objectMapper;
    }

    @GetMapping
    public String getAllSales(Model model) {
        model.addAttribute("sales", saleService.getAllSales());
        return "sales-list";
    }

    @PostMapping
    public String createSale(@ModelAttribute Sale sale, Model model) {
        saleService.createSale(sale);
        return "redirect:/admin/sales";
    }

    @GetMapping("/edit/{id}")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        Sale sale = saleService.getAllSales().stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Sale not found"));
        model.addAttribute("sale", sale);
        return "sales-edit";
    }

    @PostMapping("/update/{id}")
    public String updateSale(@PathVariable Long id, @ModelAttribute Sale saleDetails) {
        saleService.updateSale(id, saleDetails);
        return "redirect:/admin/sales";
    }

    @GetMapping("/delete/{id}")
    public String deleteSale(@PathVariable Long id) {
        saleService.deleteSale(id);
        return "redirect:/admin/sales";
    }

    @GetMapping("/reports")
    public String showReports(
            @RequestParam(required = false) String startDate,
            @RequestParam(required = false) String endDate,
            Model model) {
        LocalDateTime start = startDate != null ? LocalDateTime.parse(startDate) : LocalDateTime.now().minusDays(30);
        LocalDateTime end = endDate != null ? LocalDateTime.parse(endDate) : LocalDateTime.now();

        try {
            // Fetch total sales and convert to double
            BigDecimal totalSalesBigDecimal = saleService.getTotalSales(start, end);
            double totalSales = totalSalesBigDecimal != null ? totalSalesBigDecimal.doubleValue() : 0.0;

            // Fetch chart and table data
            List<Object[]> fastMoving = saleService.getFastMovingItems(start, end);
            List<Object[]> slowMoving = saleService.getSlowMovingItems(start, end);
            List<Object[]> dailySales = saleService.getDailySales(start, end);
            List<Object[]> productPerformance = saleService.getProductPerformance(start, end);
            List<Object[]> monthlySales = saleService.getMonthlySales(start, end);

            // Serialize to JSON
            String dailySalesJson = objectMapper.writeValueAsString(dailySales);
            String productPerformanceJson = objectMapper.writeValueAsString(productPerformance);
            String monthlySalesJson = objectMapper.writeValueAsString(monthlySales);

            // Debug logs
            System.out.println("Total Sales: " + totalSales);
            System.out.println("Fast Moving: " + fastMoving);
            System.out.println("Slow Moving: " + slowMoving);
            System.out.println("Daily Sales JSON: " + dailySalesJson);
            System.out.println("Product Performance JSON: " + productPerformanceJson);
            System.out.println("Monthly Sales JSON: " + monthlySalesJson);

            // Add to model
            model.addAttribute("totalSales", totalSales);
            model.addAttribute("fastMoving", fastMoving);
            model.addAttribute("slowMoving", slowMoving);
            model.addAttribute("dailySalesData", dailySalesJson);
            model.addAttribute("productPerformanceData", productPerformanceJson);
            model.addAttribute("monthlySalesTrend", monthlySalesJson);

        } catch (JsonProcessingException e) {
            System.err.println("Error serializing data: " + e.getMessage());
        }

        return "sales-reports";
    }

    @GetMapping("/reports/export-csv")
    public void exportSalesReportCsv(@RequestParam(defaultValue = "30") int days, HttpServletResponse response) throws IOException {
        LocalDateTime start = LocalDateTime.now().minusDays(days);
        LocalDateTime end = LocalDateTime.now();
        List<Sale> sales = saleService.getSalesInRange(start, end);

        response.setContentType("text/csv");
        response.setHeader("Content-Disposition", "attachment; filename=\"sales-report-" + LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss")) + ".csv\"");

        try (CSVWriter writer = new CSVWriter(response.getWriter())) {
            // Header
            writer.writeNext(new String[]{"ID", "Order ID", "Amount (LKR)", "Sale Date","Most Sold Items"});

            // Data
            if (sales.isEmpty()) {
                writer.writeNext(new String[]{"No sales data available for the selected period."});
            } else {
                for (Sale sale : sales) {
                    writer.writeNext(new String[]{
                            String.valueOf(sale.getId()),
                            String.valueOf(sale.getOrder().getId()),
                            sale.getAmount().setScale(2, java.math.RoundingMode.HALF_UP).toString(),
                            sale.getSaleDate().format(java.time.format.DateTimeFormatter.ISO_LOCAL_DATE_TIME),
                            saleRepository.getFastMovingItemsThreshold(start, end).stream()
                                    .filter(item -> item[0] != null && item[1] != null)
                                    .map(item -> item[0] + " (Sold: " + item[1] + ")")
                                    .reduce((a, b) -> a + "; " + b)
                                    .orElse("N/A")
                    });
                }
            }
        }
    }
}