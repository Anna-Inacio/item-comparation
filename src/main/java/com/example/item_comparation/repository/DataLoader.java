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

@Component //indica que é um componente gerenciado pelo Spring
public class DataLoader implements CommandLineRunner { // executa alguma coisa quando a aplicaçao inicia

    private final ProductsService productsService;

    public DataLoader(ProductsService productsService) {
        this.productsService = productsService;
    }

    @Override
    public void run(String... args) throws Exception {
        ObjectMapper mapper = new ObjectMapper(); //transforma json em objeto java
        TypeReference<List<Product>> typeReference = new TypeReference<>() {}; //tipo = Lista de produtos

        try (InputStream inputStream = openProductsInputStream()) { //abre o arquivo products.json
            List<Product> products = mapper.readValue(inputStream, typeReference); //le arquivo e transforma em lista de produtos
            productsService.saveAll(products); //salva todos os produtos no repositório
            System.out.println("JSON products loaded!");
        } catch (IOException exception){
            throw new DataLoadException("Unable to load product json file: " + exception.getMessage(), exception);
        }
    }

    protected InputStream openProductsInputStream() throws IOException {
        return new ClassPathResource("products.json").getInputStream();
    }
}