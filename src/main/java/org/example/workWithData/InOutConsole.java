package org.example.workWithData;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.example.Main;
import org.example.workWithAPI.ApiRegistry;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.*;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

public class InOutConsole {
    private static Scanner scanner = new Scanner(System.in);
    private static final ReentrantLock consoleLock = new ReentrantLock();

    public static void resetScanner() {
        consoleLock.lock();
        try {
            scanner.close();
            scanner = new Scanner(System.in);
        } finally {
            consoleLock.unlock();
        }
    }


    public static boolean isExit() {
        System.out.println("Enter \"0\" to complete the work, and \"1\" to continue: ");
        while (true) {
            String answer = scanner.nextLine().trim();
            if (answer.equals("0") || answer.equals("1")) return answer.equals("0");
            System.out.println("Incorrect answer. Enter answer again:");
        }

    }


    public static void readWriteLine(String prompt, boolean isQuestion) {
        consoleLock.lock();
        try {
            if (prompt != null && !prompt.isEmpty()) {
                System.out.println(prompt);
            }
            if (isQuestion) {
                scanner.nextLine();
            }
        } finally {
            consoleLock.unlock();
        }
    }


    public static void readConsole(Main.Info info) {
        System.out.println("For work before start ask the questions:\n");

        info.nTask = readNTask();
        info.interval = readInterval();
        info.apis = readApis();
        info.outType = readOutType();
        readFileMode(info);
        readOutAPI(info);
    }


    private static int readNTask() {
        System.out.println("Enter number of task (should be >0 and <=5):");
        while (true) {
            try {
                int nTask = Integer.parseInt(scanner.nextLine());
                if (nTask > 0 && nTask <= 5) return nTask;
                System.out.println("Incorrect enter. Number of tasks should be >0 and <=5.");
            } catch (NumberFormatException e) {
                System.out.println("Incorrect enter. Number of tasks should be >0 and <=5.");
            }
        }
    }


    private static int readInterval() {
        System.out.println("Enter interval between task (should be >100 and <=10000):");
        while (true) {
            try {
                int interval = Integer.parseInt(scanner.nextLine());
                if (interval >= 100 && interval <= 10000) {
                    return interval;
                }
                System.out.println("Incorrect enter. Interval should be >100 and <=10000.");
            } catch (NumberFormatException e) {
                System.out.println("Incorrect enter. Interval should be >100 and <=10000.");
            }
        }
    }


    private static List<String> readApis() {
        System.out.println("""
                Choose API for request:
                \tWeather
                \tAlliexpress
                \tAnimeNews
                \tBooks
                \tCrypt""");

        List<String> apis = Arrays.stream(scanner.nextLine().split(" "))
                .collect(Collectors.toList());

        while (!ApiRegistry.getApiNames().containsAll(apis)) {
            System.out.println("Incorrect answer. Enter answer again:");
            apis = Arrays.stream(scanner.nextLine().split(" "))
                    .collect(Collectors.toList());
        }
        return apis;
    }


    private static String readOutType() {
        System.out.println("""
                Choose type of output file:
                \tjson
                \tcsv""");
        while (true) {
            String outType = scanner.nextLine().trim();
            if (outType.equals("json") || outType.equals("csv")) {
                return outType;
            }
            System.out.println("Incorrect answer. Enter answer again:");
        }
    }


    private static void readFileMode(Main.Info info) {
        System.out.println("""
                Choose mode of work with output file:
                \tRewrite
                \tCreate new""");

        while (true) {
            String answer = scanner.nextLine().trim();
            if (answer.equals("Rewrite")) {
                System.out.println("Enter path to file without format of file:");
                info.filename = scanner.nextLine();
                info.fileMode = "rw";
                break;
            }
            if (answer.equals("Create new")) {
                info.filename = "src/test/testOut";
                info.fileMode = "cr";
                break;
            }
            System.out.println("Incorrect answer. Enter answer again:");
        }
    }


    private static void readOutAPI(Main.Info info) {
        System.out.println("""
                Choose output:
                \tAll API
                \tOne API""");

        while (true) {
            String answer = scanner.nextLine().trim();
            if (answer.equals("One API")) {
                System.out.println("Choose API for output:\n" + info.apis.toString());
                String selectedApi = scanner.nextLine().trim();
                while (!info.apis.contains(selectedApi)) {
                    System.out.println("Incorrect answer. Enter answer again:");
                    selectedApi = scanner.nextLine().trim();
                }
                info.outAPI = selectedApi;
                break;
            }
            if (answer.equals("All API")) {
                info.outAPI = "all";
                break;
            }
            System.out.println("Incorrect answer. Enter answer again:");
        }
    }


    public static boolean isStartRequests() {
        consoleLock.lock();
        try {
            System.out.println("Do you want to start all APIs?");
            while (true) {
               switch (scanner.nextLine().trim()) {
                    case "yes", "Yes":
                        System.out.println("Start work APIs");
                        return true;
                    case "no", "No":
                        return false;
                    default:
                        System.out.println("Incorrect answer.");
                }
            }
        } finally {
            consoleLock.unlock();
        }
    }


    public static void outputJSONToConsole(Main.Info info) {
        ObjectMapper mapper = new ObjectMapper();
        File jsonFile = new File(info.filename + ".json");

        try {
            JsonNode root = mapper.readTree(jsonFile);
            if (info.outAPI.equals("all")) {
                System.out.println(root.toString());
                return;
            }
            String secondFieldName = "source";
            for (JsonNode record : root) {
                JsonNode secondValue2 = record.get(secondFieldName);
                if (secondValue2.asText().equals(info.outAPI)) {
                    System.out.println(record);
                }
            }
        } catch (IOException e) {
            System.out.println("Error in reading JSON file: " + e.getMessage());
        }
    }


    public static void outputCSVToConsole(Main.Info info) {

        File file = new File(info.filename + ".csv");
        if (!file.exists()) {
            System.out.println("File is not founded");
            return;
        }

        CSVFormat format = CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .get();

        try (Reader reader = new FileReader(file);
             CSVParser parser = CSVParser.parse(reader, format)) {

            List<String> headers = new ArrayList<>(parser.getHeaderNames());
            String secondHeader = headers.get(1);
            System.out.println(String.join(",", headers));

            if (info.outAPI.equals("all")) {
                for (CSVRecord record : parser) {
                    StringJoiner joiner = new StringJoiner(",");
                    for (int i = 0; i < record.size(); i++) {
                        joiner.add(record.get(i));
                    }
                    System.out.println(joiner);
                }
                return;
            }

            for (CSVRecord record : parser) {
                String secondValue = record.get(secondHeader);
                if (secondValue.equals(info.outAPI)) {
                    StringJoiner joiner = new StringJoiner(",");
                    for (int i = 0; i < record.size(); i++) {
                        joiner.add(record.get(i));
                    }
                    System.out.println(joiner);
                }
            }
        } catch (IOException | IndexOutOfBoundsException e) {
            System.err.println("Error in reading CSV file: " + e.getMessage());
        }
    }


    public static Integer requestUser() {
        consoleLock.lock();
        try {
            System.out.println("Enter: 1 - stop APIs; 2 - again answer the questions; another - end app.");
            return Integer.parseInt(scanner.nextLine());
        } catch (NumberFormatException e) {
            System.out.println("Incorrect enter");
            return -1;
        } finally {
            consoleLock.unlock();
        }
    }
}
