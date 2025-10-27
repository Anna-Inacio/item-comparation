package com.example.item_comparation.controller;

import com.example.item_comparation.domain.Product;
import com.example.item_comparation.exception.GlobalHandlerController;
import com.example.item_comparation.exception.ProductNotFoundException;
import com.example.item_comparation.service.ProductsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductsControllerMvcTest {

    private MockMvc mockMvc;

    @Mock
    private ProductsService productsService;

    @InjectMocks
    private ProductsController productsController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productsController)
                .setControllerAdvice(new GlobalHandlerController())
                .build();
    }

    @Test
    void shouldReturn200AndEmptyArrayWhenGetAllProducts() throws Exception {
        when(productsService.getAllProducts()).thenReturn(List.of());

        mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void shouldReturn200AndArrayWhenGetAllProducts() throws Exception {
        Product p = sampleProduct();
        when(productsService.getAllProducts()).thenReturn(List.of(p));

        mockMvc.perform(get("/products").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(p.getId()))
                .andExpect(jsonPath("$[0].name").value(p.getName()));
    }

    @Test
    void shouldReturn404AndErrorResponseWhenGetProductByIdNotFound() throws Exception {
        when(productsService.getProductById(anyLong())).thenThrow(new ProductNotFoundException("not found"));

        mockMvc.perform(get("/products/999").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.path").value("/products/999"));
    }

    @Test
    void shouldReturn200AndProductWhenGetProductByIdFound() throws Exception {
        Product p = sampleProduct();
        when(productsService.getProductById(1L)).thenReturn(p);

        mockMvc.perform(get("/products/1").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(p.getId()))
                .andExpect(jsonPath("$.name").value(p.getName()));
    }

    @Test
    void shouldReturn200AndListWhenCompareFromCsvValid() throws Exception {
        Product p1 = sampleProduct();
        Product p2 = sampleProduct();
        p2.setId(2L);
        when(productsService.compareFromCsv("1,2")).thenReturn(List.of(p1, p2));

        mockMvc.perform(get("/products/compare/1,2").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[1].id").value(2));
    }

    @Test
    void shouldReturn204NoContentWhenCompareFromCsvEmpty() throws Exception {
        when(productsService.compareFromCsv("3,4")).thenReturn(List.of());

        mockMvc.perform(get("/products/compare/3,4").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNoContent());
    }

    private Product sampleProduct() {
        Product p = new Product();
        p.setId(1L);
        p.setName("Prod A");
        p.setPrice(BigDecimal.valueOf(9.99));
        p.setDescription("desc");
        p.setImageUrl("http://img");
        p.setClassification("cat");
        p.setSpecifications("specs");
        return p;
    }
}
