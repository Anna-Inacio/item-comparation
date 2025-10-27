package com.example.item_comparation.service;

import com.example.item_comparation.domain.Product;
import com.example.item_comparation.exception.InternalServerErrorException;
import com.example.item_comparation.exception.ProductNotFoundException;
import com.example.item_comparation.util.CsvIdParser;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Collections;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class ProductsService {
    private final Map<Long, Product> repository = new ConcurrentHashMap<>();
    private final CsvIdParser csvIdParser;
    private long nextId = 1;

    public ProductsService(CsvIdParser csvIdParser) {
        this.csvIdParser = csvIdParser;
    }

    public Product save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null"); // Throws exception for null product
        }
        try {
            if (product.getId() == null) {
                product.setId(nextId++);
            }
            repository.put(product.getId(), product);
            return product;
        } catch (Exception exception) {
            throw new InternalServerErrorException("Internal error saving product."); // Throws generic exception for other errors
        }
    }

    public void saveAll(List<Product> products) {
        if (products == null) {
            throw new IllegalArgumentException("Products list must not be null"); // Throws exception for null list
        }
        try {
            products.forEach(this::save);
        } catch (Exception exception) {
            throw new InternalServerErrorException("Internal error saving products"); // Throws generic exception for other errors
        }
    }

    public List<Product> getAllProducts() {
        try {
            return new ArrayList<>(repository.values());
        } catch (Exception exception){
            throw new InternalServerErrorException("Error retrieving products " + exception.getMessage()); // Throws generic exception for other errors
        }
    }

    public Product getProductById(Long productId) {
        if (productId == null) {
            throw new IllegalArgumentException("productId is required"); // Throws exception for null productId
        }
        try {
            Product product = repository.get(productId);
            if (product == null) {
                throw new ProductNotFoundException("Product with ID " + productId + " not found."); // Throws exception if not found
            }
            return product;
        } catch (ProductNotFoundException notFoundException){
            throw notFoundException;
        } catch (Exception exception) {
            throw new InternalServerErrorException("Error retrieving product with ID: " + productId + ": " + exception.getMessage()); // Throws generic exception for other errors
        }
    }

    public List<Product> compare(List<Long> productIds) {
        if (productIds == null) {
            throw new IllegalArgumentException("productIds must not be null"); // Throws exception for null list
        }
        if (productIds.isEmpty()) {
            return Collections.emptyList();
        }
        try {
            return productIds.stream()
                    .map(this::getProductById)
                    .collect(Collectors.toList());
        } catch (ProductNotFoundException notFoundException) {
            throw notFoundException; // Throws exception if not found
        } catch (Exception exception) {
            throw new InternalServerErrorException("Internal error when comparing products " + exception); // Throws generic exception for other errors
        }
    }

    public List<Product> compareFromCsv(String productIdsCsv) {
        if (productIdsCsv == null || productIdsCsv.trim().isEmpty()) {
            throw new IllegalArgumentException("productIdsCsv must not be null or empty"); // Throws exception for null or empty input
        }
        try {
            List<Long> ids = csvIdParser.parseToLongList(productIdsCsv);
            return compare(ids);
        } catch (IllegalArgumentException parseEx) {
            throw new IllegalArgumentException("Invalid productIdsCsv" + parseEx.getMessage(), parseEx); // Throws exception for parsing errors
        } catch (ProductNotFoundException notFoundException) {
            throw notFoundException; // Throws exception if not found
        } catch (Exception exception) {
            throw new InternalServerErrorException("Internal error when comparing products from CSV " + exception); // Throws generic exception for other errors
        }
    }
}
