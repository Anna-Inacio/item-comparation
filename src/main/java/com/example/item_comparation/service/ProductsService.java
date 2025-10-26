package com.example.item_comparation.service;

import com.example.item_comparation.domain.Product;
import com.example.item_comparation.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductsService {
    private final Map<Long, Product> repository = new ConcurrentHashMap<>();

    // Get all products in json
    public List<Product> getAllProducts() {
        return new ArrayList<>(repository.values());
    }

    // Get a product by ID
    public Product getProductById(Long productId) {
        Product product = repository.get(productId);
        if (product == null) {
            throw new ProductNotFoundException("Product with ID " + productId + " not found.");
        }
        return product;
    }

    // Contador para gerar IDs
    private long nextId = 1;

    public Product save(Product product) {
        if (product.getId() == null) {
            product.setId(nextId++);
        }
        repository.put(product.getId(), product);
        return product;
    }

    public void saveAll(List<Product> products) {
        products.forEach(this::save);
    }
}
