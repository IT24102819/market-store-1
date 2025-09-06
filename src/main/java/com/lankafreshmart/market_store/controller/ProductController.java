package com.lankafreshmart.market_store.controller;

import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.service.ProductService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/")
    public String showLandingPage(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            HttpServletRequest request,
            Model model) {
        List<Product> products;
        if (search != null && !search.isEmpty()) {
            products = productService.searchProducts(search, null, null, null);
        } else if (category != null && !category.isEmpty()) {
            products = productService.searchProducts("", null, null, category);
        } else {
            // Display a limited number of featured products (e.g., first 6)
            Page<Product> productPage = productService.getAllProducts(0, 6);
            products = productPage.getContent();
        }
        model.addAttribute("products", products);
        model.addAttribute("categories", productService.getAllProducts(0, Integer.MAX_VALUE).getContent().stream()
                .map(Product::getCategory)
                .filter(cat -> cat != null && !cat.isEmpty())
                .distinct()
                .collect(Collectors.toList()));
        model.addAttribute("search", search);
        model.addAttribute("category", category);
        // Add guest cart items to model if exists in session
        @SuppressWarnings("unchecked")
        Map<Long, Integer> guestCart = (Map<Long, Integer>) request.getSession().getAttribute("guestCart");
        if (guestCart == null) {
            guestCart = new HashMap<>();
            request.getSession().setAttribute("guestCart", guestCart);
        }
        model.addAttribute("guestCart", guestCart);
        return "landing";
    }

    @PostMapping("/guest-cart/add")
    public String addToGuestCart(
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            HttpServletRequest request,
            Model model) {

        Product product = productService.getProductById(productId)
                .orElseThrow(() -> new IllegalArgumentException("Product not found"));
        if (quantity > product.getStockQuantity()) {
            model.addAttribute("error", "Insufficient stock");
            return "redirect:/";
        }
        // Store in session for guest
        @SuppressWarnings("unchecked")
        Map<Long, Integer> guestCart = (Map<Long, Integer>) request.getSession().getAttribute("guestCart");
        if (guestCart == null) {
            guestCart = new HashMap<>();
            request.getSession().setAttribute("guestCart", guestCart);
        }
        guestCart.put(productId, guestCart.getOrDefault(productId, 0) + quantity);
        model.addAttribute("message", "Product added to cart! Please login to proceed to checkout.");
        return "redirect:/";
    }

    @GetMapping("/guest-cart/view")
    public String viewGuestCart(HttpServletRequest request, Model model) {
        @SuppressWarnings("unchecked")
        Map<Long, Integer> guestCart = (Map<Long, Integer>) request.getSession().getAttribute("guestCart");
        if (guestCart == null) {
            guestCart = new HashMap<>();
            request.getSession().setAttribute("guestCart", guestCart);
        }
        List<Product> cartProducts = new ArrayList<>();
        for (Map.Entry<Long, Integer> entry : guestCart.entrySet()) {
            Product product = productService.getProductById(entry.getKey())
                    .orElseThrow(() -> new IllegalArgumentException("Product not found"));
            cartProducts.add(product);
        }
        model.addAttribute("cartProducts", cartProducts);
        model.addAttribute("guestCart", guestCart);
        return "guest-cart";
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
        if (search != null && !search.isEmpty() && (minPrice == null && maxPrice == null && (category == null || category.isEmpty()))) {
            products = productService.searchProducts(search, null, null, null);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
        } else if ((search == null || search.isEmpty()) && (minPrice != null || maxPrice != null || (category != null && !category.isEmpty()))) {
            products = productService.searchProducts("", minPrice, maxPrice, category);
            model.addAttribute("currentPage", 0);
            model.addAttribute("totalPages", 1);
        } else {
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