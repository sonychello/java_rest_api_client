package org.example.convertor;

import java.util.*;

public record ConvertorResult(List<String> allPaths, List<Map<String, String>> records) {
    public ConvertorResult(List<String> allPaths, List<Map<String, String>> records) {
        this.allPaths = new ArrayList<>(allPaths);
        this.records = new ArrayList<>(records);
    }

    @Override
    public List<String> allPaths() {
        return Collections.unmodifiableList(allPaths);
    }

    @Override
    public List<Map<String, String>> records() {
        return Collections.unmodifiableList(records);
    }

    @Override
    public String toString() {
        return "ConvertorResult{" +
                "allPaths=" + allPaths +
                ", recordsCount=" + records.size() +
                '}';
    }
}