package com.example.item_comparation.repository;

import com.example.item_comparation.domain.Product;
import com.example.item_comparation.exception.DataLoadException;
import com.example.item_comparation.service.ProductsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

@Component
public class DataLoader implements CommandLineRunner {

    private final ProductsService productsService;

    public DataLoader(ProductsService productsService) {
        this.productsService = productsService;
    }

    @Override
    public void run(String... args) throws Exception {
        ObjectMapper mapper = new ObjectMapper();
        TypeReference<List<Product>> typeReference = new TypeReference<>() {};

        try (InputStream inputStream = openProductsInputStream()) {
            List<Product> products = mapper.readValue(inputStream, typeReference);
            productsService.saveAll(products);
            System.out.println("JSON products loaded!");
        } catch (IOException exception){
            throw new DataLoadException("Unable to load product json file: " + exception.getMessage(), exception);
        }
    }

    protected InputStream openProductsInputStream() throws IOException {
        return new ClassPathResource("products.json").getInputStream();
    }
}