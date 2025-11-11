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
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

@Service
public class ProductsService {
    private final Map<Long, Product> repository = new ConcurrentHashMap<>(); //chave = id do produto, valor = produto
    private final CsvIdParser csvIdParser; //utilitário para parsear CSV de IDs
//    private long nextId = repository.size();
    private final AtomicLong nextId = new AtomicLong(1); //próximo ID disponível para novos produtos

    public ProductsService(CsvIdParser csvIdParser) {
        this.csvIdParser = csvIdParser;

        repository.keySet().stream() //encontra o maior ID existente no repositório
                .max(Long::compare) // compara Ids e pega o maior ID existente
                .ifPresent(max -> nextId.set(max + 1)); //define o próximo ID como maior ID + 1
    }

    public Product save(Product product) {
        if (product == null) {
            throw new IllegalArgumentException("Product must not be null"); // Throws exception for null product
        }
        try {
            if (product.getId() == null) {
                product.setId(nextId.getAndIncrement()); //atribui um ID único ao produto se não tiver
            } else {
                // atualiza o nextId se o ID fornecido for maior ou igual ao próximo ID
                long provided = product.getId(); // ID fornecido no produto
                nextId.updateAndGet(curr -> Math.max(curr, provided + 1)); // atualiza o próximo ID  se necessário
            }
            repository.put(product.getId(), product); //salva o produto no repositório (memória)
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
            return Collections.emptyList(); //retorna lista vazia se a lista de IDs estiver vazia
        }
        try {
            return productIds.stream() // percorre a lista de IDs
                    .map(this::getProductById) // busca o produto pelo ID
                    .collect(Collectors.toList()); // coleta os produtos em uma lista
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
            List<Long> ids = csvIdParser.parseToLongList(productIdsCsv); //parseia o CSV para lista de IDs
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
