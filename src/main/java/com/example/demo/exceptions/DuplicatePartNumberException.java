package com.example.demo.exceptions;

import com.example.demo.Product;

import java.util.List;
import java.util.stream.Collectors;

public class DuplicatePartNumberException extends Exception {
    public DuplicatePartNumberException(List<Product> duplicates) {
        super("Duplicate partNumberNR found for the following products: " + formatDuplicates(duplicates));
    }

    private static String formatDuplicates(List<Product> duplicates) {
        return duplicates.stream()
                .map(product -> String.format("[id=%d, name=%s, partNumberNR=%s]", product.getId(), product.getName(), product.getPartNumberNR()))
                .collect(Collectors.joining(", "));
    }
}
