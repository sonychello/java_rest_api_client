package org.example.workWithData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVPrinter;
import org.apache.commons.csv.CSVRecord;
import org.example.Main;
import org.example.convertor.ConvertorResult;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;

public class SaveData {
    private static final ReentrantLock fileLock = new ReentrantLock();
    private static boolean isCreated = false;


    public static void outputFileJson(JsonNode json, Main.Info info) {
        ObjectMapper mapper = new ObjectMapper();
        mapper.enable(SerializationFeature.INDENT_OUTPUT);

        fileLock.lock();
        try {
            File file = new File(info.filename + ".json");

            if (!isCreated && info.fileMode.equals("cr")) {
                isCreated = true;
                writeNewJsonFile(mapper, json, file);
                return;
            } else if (!file.exists() && info.fileMode.equals("rw")) {
                System.out.println("File " + info.filename + " is not founded. Create new.");
                isCreated = true;
                writeNewJsonFile(mapper, json, file);
                return;
            }

            JsonNode existingJson = readExistingJson(file, mapper);
            ArrayNode jsonNodes = mergeJsonNodes(existingJson, json, mapper);
            rewriteJsonFile(file, jsonNodes, mapper);

        } catch (JsonProcessingException e) {
            System.out.println("Error of parsing Json: " + e.getMessage());
        } catch (IOException e) {
            System.out.println("Error of write to file: " + e.getMessage());
        } finally {
            fileLock.unlock();
        }
    }

    private static void writeNewJsonFile(ObjectMapper mapper, JsonNode json, File file) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            mapper.writeValue(fos, json);
            fos.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    private static JsonNode readExistingJson(File file, ObjectMapper mapper) throws IOException {
        return mapper.readTree(file);
    }

    private static ArrayNode mergeJsonNodes(JsonNode existingJson, JsonNode newJson, ObjectMapper mapper) {
        ArrayNode jsonNodes;

        if (existingJson.isArray()) {
            jsonNodes = (ArrayNode) existingJson;
            jsonNodes.add(newJson);
        } else {
            jsonNodes = mapper.createArrayNode();
            jsonNodes.add(existingJson);
            jsonNodes.add(newJson);
        }

        return jsonNodes;
    }

    private static void rewriteJsonFile(File file, ArrayNode jsonNodes, ObjectMapper mapper) throws IOException {
        try (FileOutputStream fos = new FileOutputStream(file)) {
            mapper.writeValue(fos, jsonNodes);
            fos.flush();
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }



    public static void outputFileCSV(ConvertorResult convertResult, Main.Info info) throws IOException {

        fileLock.lock();
        try {
            File file = new File(info.filename + ".csv");

            if (!isCreated && info.fileMode.equals("cr")) {
                isCreated = true;
                writeNewCSVFile(convertResult, file);
                return;
            } else if (!file.exists() && info.fileMode.equals("rw")) {
                System.out.println("File " + info.filename + " is not founded. Create new.");
            }

            Set<String> existingHeaders = new LinkedHashSet<>();
            List<Map<String, String>> existingRecords = new ArrayList<>();

            if (file.exists()) {
                existingHeaders = readExistingHeaders(file.toPath());
                existingRecords = readAllRecords(file.toPath(), existingHeaders);
            }

            Set<String> newHeaders = new LinkedHashSet<>(convertResult.allPaths());
            Set<String> allHeaders = new LinkedHashSet<>();
            allHeaders.addAll(existingHeaders);
            allHeaders.addAll(newHeaders);

            List<Map<String, String>> allRecords = new ArrayList<>();
            allRecords.addAll(existingRecords);
            allRecords.addAll(convertResult.records());

            rewriteCSVFile(file.toPath(), allRecords, new ArrayList<>(allHeaders));
        } catch (IOException e) {
            System.out.println("Error writing CSV: " + e.getMessage());
        } finally {
            fileLock.unlock();
        }
    }

    private static void writeNewCSVFile(ConvertorResult convertResult, File file) throws IOException {
        List<String> headers = convertResult.allPaths();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(headers.toArray(String[]::new))
                .get();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file));
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            for (Map<String, String> record : convertResult.records()) {
                for (String header : headers) {
                    String value = record.getOrDefault(header, "");
                    printer.print(value);
                }
                printer.println();
            }
        }
    }

    private static void rewriteCSVFile(Path filePath, List<Map<String, String>> records,
                                       List<String> headers) throws IOException {
        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader(headers.toArray(String[]::new))
                .get();  // вместо .build()

        try (BufferedWriter writer = Files.newBufferedWriter(filePath);
             CSVPrinter printer = new CSVPrinter(writer, format)) {

            for (Map<String, String> record : records) {
                for (String header : headers) {
                    String value = record.getOrDefault(header, "");
                    printer.print(value);
                }
                printer.println();
            }
        }
    }

    private static Set<String> readExistingHeaders(Path filePath){
        Set<String> headers = new LinkedHashSet<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        try (Reader reader = Files.newBufferedReader(filePath);
             CSVParser parser = CSVParser.builder()
                     .setReader(reader)
                     .setFormat(format)
                     .get() ){
            headers.addAll(parser.getHeaderNames());
        } catch (IOException e) {
            System.err.println("Error reading CSV headers: " + e.getMessage());
        }

        return headers;
    }

    private static List<Map<String, String>> readAllRecords(Path filePath, Set<String> headers) throws IOException {
        List<Map<String, String>> records = new ArrayList<>();

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        try (Reader reader = Files.newBufferedReader(filePath);
             CSVParser parser = CSVParser.builder()
                     .setReader(reader)
                     .setFormat(format)
                     .get()) {

            for (CSVRecord csvRecord : parser) {
                Map<String, String> record = new LinkedHashMap<>();
                for (String header : headers) {
                    String value = csvRecord.isSet(header) ? csvRecord.get(header) : "";
                    record.put(header, value);
                }
                records.add(record);
            }
        }

        return records;
    }
}