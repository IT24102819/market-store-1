package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Controller
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public String showProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) String category,
            Model model) {
        List<Product> products;
        if (search != null || minPrice != null || maxPrice != null || category != null) {
            // Apply filters
            products = productService.searchProducts(search, minPrice, maxPrice, category);
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", 1); // Filters return a list, so no pagination for now
        } else {
            // Default: paginated list of all products
            Page<Product> productPage = productService.getAllProducts(page, size);
            products = productPage.getContent();
            model.addAttribute("currentPage", page);
            model.addAttribute("totalPages", productPage.getTotalPages());
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllProducts(0, Integer.MAX_VALUE).getContent().stream()
                .map(Product::getCategory)
                .filter(cat -> cat != null && !cat.isEmpty())
                .distinct()
                .collect(Collectors.toList()));
        model.addAttribute("search", search);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("category", category);
        return "products";
    }

    @GetMapping("/admin/products")
    public String adminProducts(@RequestParam(defaultValue = "0") int page,
                                @RequestParam(defaultValue = "10") int size, Model model) {
        Page<Product> productPage = productService.getAllProducts(page, size);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        model.addAttribute("product", new Product());
        model.addAttribute("lowStockProducts", productService.getLowStockProducts());
        return "admin-products";
    }

    @PostMapping("/admin/products/create")
    public String createProduct(@ModelAttribute Product product, Model model) {
        try {
            productService.createProduct(product);
            return "redirect:/admin/products?success=Product created successfully";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("product", product);
            Page<Product> productPage = productService.getAllProducts(0, 10);
            model.addAttribute("products", productPage.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", productPage.getTotalPages());
            model.addAttribute("lowStockProducts", productService.getLowStockProducts());
            return "admin-products";
        }
    }

    @PostMapping("/admin/products/update")
    public String updateProduct(@RequestParam("id") Long id, @ModelAttribute Product product, Model model) {
        try {
            productService.updateProduct(id, product);
            return "redirect:/admin/products?success=Product updated successfully";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            Page<Product> productPage = productService.getAllProducts(0, 10);
            model.addAttribute("products", productPage.getContent());
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", productPage.getTotalPages());
            model.addAttribute("product", new Product());
            model.addAttribute("lowStockProducts", productService.getLowStockProducts());
            return "admin-products";
        }
    }

    @PostMapping("/admin/products/delete")
    public String deleteProduct(@RequestParam("id") Long id) {
        productService.deleteProduct(id);
        return "redirect:/admin/products?success=Product deleted successfully";
    }
}