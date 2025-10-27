package com.example.item_comparation.service;

import com.example.item_comparation.domain.Product;
import com.example.item_comparation.exception.ProductNotFoundException;
import com.example.item_comparation.util.CsvIdParser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ProductsServiceTest {

    private ProductsService service;

    @BeforeEach
    void setUp() {
        service = new ProductsService(new CsvIdParser());
    }

    @Test
    void MustReturnProductWithIdAfterSaving() {
        Product product = createProduct("A", 10.0);
        assertNull(product.getId());
        Product saved = service.save(product);
        assertNotNull(saved.getId());
        assertEquals(1L, saved.getId());
    }

    @Test
    void shouldReturnAllProductsWhenSaveAllCalled() {
        Product product1 = createProduct("A", 10.0);
        Product product2 = createProduct("B", 20.0);
        service.saveAll(List.of(product1, product2));
        List<Product> all = service.getAllProducts();
        assertEquals(2, all.size());
    }

    @Test
    void shouldReturnEmptyListWhenGetAllProductsAndRepositoryEmpty() {
        List<Product> allProducts = service.getAllProducts();
        assertNotNull(allProducts);
        assertTrue(allProducts.isEmpty());
    }

    @Test
    void shouldThrowProductNotFoundWhenGetProductByIdNotFound() {
        assertThrows(ProductNotFoundException.class, () -> service.getProductById(999L));
    }

    @Test
    void shouldReturnProductsWhenCompareWithValidIds() {
        createAndSaveProducts(3);
        List<Product> compared = service.compare(List.of(1L, 2L, 3L));
        assertEquals(3, compared.size());
    }

    @Test
    void shouldReturnProductsWhenCompareFromCsvWithValidCsv() {
        createAndSaveProducts(2);
        Product saveProduct1 = service.getProductById(1L);
        Product saveProduct2 = service.getProductById(2L);
        List<Product> compared = service.compareFromCsv(saveProduct1.getId() + "," + saveProduct2.getId());
        assertEquals(2, compared.size());
    }

    @Test
    void shouldThrowIllegalArgumentWhenCompareFromCsvWithInvalidCsv() {
        assertThrows(IllegalArgumentException.class, () -> service.compareFromCsv("1,a,3"));
    }

    private Product createProduct(String name, double price) {
        Product p = new Product();
        p.setName(name);
        p.setPrice(BigDecimal.valueOf(price));
        p.setDescription("desc");
        p.setImageUrl("http://img");
        p.setClassification("cat");
        p.setSpecifications("spec");
        return p;
    }

    private void createAndSaveProducts(int count) {
        for (int i = 1; i <= count; i++) {
            Product product = new Product();
            product.setName("Product " + i);
            product.setPrice(BigDecimal.valueOf(10.0 * i));
            product.setDescription("Description " + i);
            product.setImageUrl("http://image" + i);
            product.setClassification("Category " + i);
            product.setSpecifications("Specifications " + i);
            service.save(product);
        }
    }
}
