package org.example.convertor;

import com.fasterxml.jackson.databind.JsonNode;
import java.io.IOException;
import java.util.*;

public class Convertor {

    public static ConvertorResult convert(JsonNode rootArray) throws IOException {
        if (rootArray == null || !rootArray.isArray()) {
            return new ConvertorResult(new ArrayList<>(), new ArrayList<>());
        }

        List<Map<String, String>> allRecords = new ArrayList<>();

        for (JsonNode record : rootArray) {
            List<Map<String, String>> flattenedRecords = flattenRecord(record);
            allRecords.addAll(flattenedRecords);
        }

        List<String> allPaths = extractAllPaths(allRecords);

        return new ConvertorResult(allPaths, allRecords);
    }

    private static List<Map<String, String>> flattenRecord(JsonNode record) {
        List<Map<String, String>> results = new ArrayList<>();

        Map<String, String> baseMap = new LinkedHashMap<>();
        baseMap.put("id", getValue(record.get("id")));
        baseMap.put("source", getValue(record.get("source")));
        baseMap.put("timestamp", getValue(record.get("timestamp")));

        JsonNode data = record.get("data");
        if (data != null) {
            List<Map<String, String>> expandedRecords = expandArrays(data, baseMap);
            results.addAll(expandedRecords);
        } else {
            results.add(baseMap);
        }

        return results;
    }

    private static List<Map<String, String>> expandArrays(JsonNode node, Map<String, String> baseMap) {
        List<Map<String, String>> results = new ArrayList<>();

        List<String> arrayFields = new ArrayList<>();
        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (field.getValue().isArray()) {
                arrayFields.add(field.getKey());
            }
        }

        if (arrayFields.isEmpty()) {
            // Нет массивов - добавляем все поля
            Map<String, String> recordMap = new LinkedHashMap<>(baseMap);
            addAllFields(recordMap, node);
            results.add(recordMap);
        } else {
            // Есть массивы - разворачиваем первый найденный массив
            String firstArrayField = arrayFields.getFirst();
            JsonNode arrayNode = node.get(firstArrayField);

            for (JsonNode arrayItem : arrayNode) {
                Map<String, String> recordMap = new LinkedHashMap<>(baseMap);
                // Добавляем все поля, кроме массива
                addAllFieldsExcept(recordMap, node, firstArrayField);
                // Добавляем данные из элемента массива
                flattenNode(arrayItem, "data" + "." + firstArrayField, recordMap);
                results.add(recordMap);
            }
        }

        return results;
    }

    private static void addAllFieldsExcept(Map<String, String> result, JsonNode node,
                                           String excludeField) {
        if (node == null) return;

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            if (!field.getKey().equals(excludeField)) {
                String fieldName = "data" + "." + field.getKey();
                JsonNode value = field.getValue();

                if (value.isObject()) {
                    flattenNode(value, fieldName, result);
                } else if (value.isArray()) {
                    // Для других массивов берем первый элемент или оставляем пустым
                    if (!value.isEmpty()) {
                        JsonNode firstItem = value.get(0);
                        if (firstItem.isObject()) {
                            flattenNode(firstItem, fieldName, result);
                        } else if (firstItem.isValueNode()) {
                            result.put(fieldName, getValue(firstItem));
                        }
                    }
                } else {
                    result.put(fieldName, getValue(value));
                }
            }
        }
    }

    private static void addAllFields(Map<String, String> result, JsonNode node) {
        if (node == null) return;

        Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
        while (fields.hasNext()) {
            Map.Entry<String, JsonNode> field = fields.next();
            String fieldName = "data" + "." + field.getKey();
            JsonNode value = field.getValue();

            if (value.isObject()) {
                flattenNode(value, fieldName, result);
            } else if (value.isArray()) {
                // Для массивов берем первый элемент
                if (!value.isEmpty()) {
                    JsonNode firstItem = value.get(0);
                    if (firstItem.isObject()) {
                        flattenNode(firstItem, fieldName, result);
                    } else if (firstItem.isValueNode()) {
                        result.put(fieldName, getValue(firstItem));
                    }
                }
            } else {
                result.put(fieldName, getValue(value));
            }
        }
    }

    private static void flattenNode(JsonNode node, String prefix, Map<String, String> result) {
        if (node == null) return;

        if (node.isObject()) {
            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String fieldName = prefix + "." + field.getKey();
                JsonNode value = field.getValue();

                if (value.isObject()) {
                    flattenNode(value, fieldName, result);
                } else if (value.isArray()) {
                    if (!value.isEmpty()) {
                        JsonNode firstItem = value.get(0);
                        if (firstItem.isObject()) {
                            flattenNode(firstItem, fieldName, result);
                        } else if (firstItem.isValueNode()) {
                            result.put(fieldName, getValue(firstItem));
                        }
                    }
                } else {
                    result.put(fieldName, getValue(value));
                }
            }
        } else if (node.isValueNode()) {
            result.put(prefix, getValue(node));
        }
    }

    private static List<String> extractAllPaths(List<Map<String, String>> records) {
        Set<String> allKeys = new LinkedHashSet<>();
        for (Map<String, String> record : records) {
            allKeys.addAll(record.keySet());
        }
        return new ArrayList<>(allKeys);
    }

    private static String getValue(JsonNode node) {
        if (node == null || node.isNull()) {
            return "";
        }
        if (node.isTextual()) {
            return node.asText();
        }
        if (node.isNumber()) {
            return node.asText();
        }
        if (node.isBoolean()) {
            return String.valueOf(node.asBoolean());
        }
        return node.toString();
    }
}