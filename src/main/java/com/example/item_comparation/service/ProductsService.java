package com.example.item_comparation.service;

import com.example.item_comparation.domain.Product;
import com.example.item_comparation.exception.InvalidProductIdException;
import com.example.item_comparation.exception.ProductNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ProductsService {
    private final Map<Long, Product> repository = new ConcurrentHashMap<>();

    // Pega todos os produtos do json
    public List<Product> getAllProducts() {
        return new ArrayList<>(repository.values());
    }

    // Pega um produto pelo seu ID
    public Product getProductById(Long productId) {
        // Validações e tratamento de erros conforme comentado
        if (productId == null) {
            //TODO [corrigir] lançar exceção customizada para ser tratada pelo handler global
            throw new InvalidProductIdException("O ID do produto não pode ser nulo.");
        }
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

    //criar produto
//    public Product createProduct(Product product) {
//        Product newProduct = new Product(product);
//        repository.save(newProduct);
//        return newProduct;
//    }




    public void compareProducts(Long productId1, Long productId2) {
        // Lógica para comparar dois produtos com base em seus IDs
    }

}
