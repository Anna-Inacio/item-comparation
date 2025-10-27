package com.example.item_comparation.repository;

import com.example.item_comparation.exception.DataLoadException;
import com.example.item_comparation.service.ProductsService;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.InputStream;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;

class DataLoaderTest {

    @Test
    void ShouldThrowDataLoadExceptionWhenFileIsNotJson() throws Exception {
        ProductsService productsService = mock(ProductsService.class);

        DataLoader loader = new DataLoader(productsService) {
            @Override
            protected InputStream openProductsInputStream() {
                String notJson = "<root>this is xml, not json</root>";
                return new ByteArrayInputStream(notJson.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            }
        };

        assertThrows(DataLoadException.class, () -> loader.run());
    }
}

