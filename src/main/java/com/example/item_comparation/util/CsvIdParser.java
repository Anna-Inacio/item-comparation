package com.example.item_comparation.util;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class CsvIdParser {
    //TODO arrumar
    public List<Long> parseToLongList(String csv) {
        if (csv == null || csv.isBlank()) {
            throw new IllegalArgumentException("productIds vazio");
        }
        String[] parts = csv.split(",");
        List<Long> ids = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim();
            if (trimmed.isEmpty()) continue;
            try {
                ids.add(Long.parseLong(trimmed));
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("productIds contém valor não numérico: " + trimmed, ex);
            }
        }
        if (ids.isEmpty()) {
            throw new IllegalArgumentException("nenhum id válido fornecido");
        }
        return ids;
    }
}
