package com.lankafreshmart.market_store.controller;


import com.lankafreshmart.market_store.model.Product;
import com.lankafreshmart.market_store.service.ProductService;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ProductController {
    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Updated: Public product listing with pagination
    @GetMapping("/products")
    public String showProducts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "10") int size, Model model) {
        Page<Product> productPage = productService.getAllProducts(page, size);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "products";
    }

    // Admin product management (add pagination if needed)
    @GetMapping("/admin/products")
    public String showAdminProducts(@RequestParam(defaultValue = "0") int page,
                                    @RequestParam(defaultValue = "10") int size, Model model) {
        Page<Product> productPage = productService.getAllProducts(page, size);
        model.addAttribute("products", productPage.getContent());
        model.addAttribute("product", new Product());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", productPage.getTotalPages());
        return "admin-products";
    }

    @PostMapping("/admin/products/create")
    public String createProduct(@ModelAttribute Product product, Model model) {
        try {
            productService.createProduct(product);
            return "redirect:/admin/products?success=Product created successfully";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("products", productService.getAllProducts(0, 10).getContent());
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
            model.addAttribute("products", productService.getAllProducts(0, 10).getContent());
            return "admin-products";
        }
    }

    @PostMapping("/admin/products/delete")
    public String deleteProduct(@RequestParam("id") Long id, Model model) {
        try {
            productService.deleteProduct(id);
            return "redirect:/admin/products?success=Product deleted successfully";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("products", productService.getAllProducts(0, 10).getContent());
            return "admin-products";
        }
    }
}
