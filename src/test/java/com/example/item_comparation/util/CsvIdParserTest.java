package com.example.item_comparation.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class CsvIdParserTest {

    private CsvIdParser parser;

    @BeforeEach
    void setUp() {
        parser = new CsvIdParser();
    }

    @Test
    void shouldReturnListOfLongsWhenCsvIsValid() {
        List<Long> ids = parser.parseToLongList("1,2,3");
        assertEquals(3, ids.size());
        assertEquals(List.of(1L, 2L, 3L), ids);
    }

    @Test
    void shouldReturnListOfLongsWhenCsvContainsSpacesAndEmptyTokens() {
        List<Long> ids = parser.parseToLongList(" 1 ,  2,,3 , ");
        assertEquals(List.of(1L, 2L, 3L), ids);
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCsvIsNullOrBlank() {
        assertThrows(IllegalArgumentException.class, () -> parser.parseToLongList(null));
        assertThrows(IllegalArgumentException.class, () -> parser.parseToLongList("\n\t  "));
    }

    @Test
    void shouldThrowIllegalArgumentExceptionWhenCsvContainsNonNumericToken() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> parser.parseToLongList("1,a,3"));
        assertTrue(ex.getMessage().contains("non-numeric") || ex.getMessage().toLowerCase().contains("non"));
    }
}
