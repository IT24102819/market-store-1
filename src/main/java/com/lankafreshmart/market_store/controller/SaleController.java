package com.lankafreshmart.market_store.controller;

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

    public SaleController(SaleService saleService, SaleRepository saleRepository) {
        this.saleService = saleService;
        this.saleRepository = saleRepository;
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
    public String showReports(Model model) {
        LocalDateTime start = LocalDateTime.now().minusDays(30); // Last 30 days
        LocalDateTime end = LocalDateTime.now();
        model.addAttribute("totalSales", saleService.getTotalSales(start, end));
        model.addAttribute("fastMoving", saleService.getFastMovingItems(start, end));
        model.addAttribute("slowMoving", saleService.getSlowMovingItems(start, end));
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
                            sale.getAmount().setScale(2, BigDecimal.ROUND_HALF_UP).toString(),
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