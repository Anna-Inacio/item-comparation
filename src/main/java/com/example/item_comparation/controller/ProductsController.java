package com.example.item_comparation.controller;

import com.example.item_comparation.domain.Product;
import com.example.item_comparation.service.ProductsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductsController {

    @Autowired
    private ProductsService productsService;

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        var products = productsService.getAllProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/{productId}")
    public ResponseEntity<Product> getProductById(@PathVariable Long productId) {
        var product = productsService.getProductById(productId);
        return ResponseEntity.ok(product);
    }

    @GetMapping("/compare/{productIds}")
    public ResponseEntity<List<Product>> compareProductsNoParams(@PathVariable("productIds") String productIds) {
        var products = productsService.compareFromCsv(productIds);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    @GetMapping("/compare")
    public ResponseEntity<List<Product>> compareProducts(@RequestBody List<Long> productIds) {
        var products = productsService.compare(productIds);
        if (products.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(products);
    }

    @PostMapping
    public ResponseEntity<Product> createProduct(@RequestBody Product product) {
        var savedProduct = productsService.save(product);
        return ResponseEntity.ok(savedProduct);
    }
}
