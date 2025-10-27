package com.example.item_comparation.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CsvIdParser {
    public List<Long> parseToLongList(String csv) {
        if (csv == null || csv.isBlank()) {
            throw new IllegalArgumentException("Empty productIds");
        }
        String[] parts = csv.split(",");
        List<Long> ids = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (trimmed.isEmpty()) continue;
            try {
                ids.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("productIds contain non-numeric value: " + trimmed, ex);
            }
        }
        return ids;
    }
}
