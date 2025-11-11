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
        String[] parts = csv.split(","); //divide a string em partes usando vírgula como separador
        List<Long> ids = new ArrayList<>();
        for (String p : parts) {
            String trimmed = p.trim(); //remove espaços em branco
            if (trimmed.isEmpty()) continue; //pula valores vazios
            try {
                ids.add(Long.parseLong(trimmed)); //converte para Long e adiciona na lista
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("productIds contain non-numeric value: " + trimmed, ex);
            }
        }
        return ids;
    }
}
