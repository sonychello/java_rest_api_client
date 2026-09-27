import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.Main;
import org.example.convertor.ConvertorResult;
import org.example.workWithData.SaveData;
import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SaveDataTest {
    private static final ObjectMapper mapper = new ObjectMapper();

    @BeforeEach
    void resetSaveDataState() throws Exception {
        java.lang.reflect.Field field = SaveData.class.getDeclaredField("isCreated");
        field.setAccessible(true);
        field.set(null, false);
    }

    @TempDir
    Path tempDir;


    //Tests for outputFileJson()
    @Test
    @DisplayName("Успешное сохранение корректного json объекта в новый файл")
    void outputFileJsonShouldCorrectlySaveCorrectJson() throws IOException {
        Path jsonFile = tempDir.resolve("output.json");
        String filename = jsonFile.getParent().resolve("output").toString();
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.fileMode = "cr";
        String jsonString = """
                {
                    "id": 1,
                    "source": "Weather",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "data": {
                        "temperature": 22.5,
                        "humidity": 65,
                        "city": "Moscow"
                    }
                }
                """;
        JsonNode json = mapper.readTree(jsonString);

        SaveData.outputFileJson(json, testInfo);

        assertTrue(Files.exists(jsonFile));
        JsonNode savedJson = mapper.readTree(jsonFile.toFile());
        assertEquals(json, savedJson);
    }

    @Test
    @DisplayName("Сохранение json в существующий файл (сохранённые до этого в файле данные должны обновиться)")
    void outputFileJsonShouldRewriteToExistingFile_whenCreate() throws IOException {
        // GIVEN
        Path jsonFile = tempDir.resolve("output.json");
        String filename = jsonFile.getParent().resolve("output").toString();
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.fileMode = "cr";
        JsonNode firstJson = mapper.readTree("""
            {"id": 1, "source": "Weather", "data": {"temp": 20}}
            """);
        JsonNode secondJson = mapper.readTree("""
            {"id": 2, "source": "Weather", "data": {"temp": 22}}
            """);

        SaveData.outputFileJson(firstJson, testInfo);
        SaveData.outputFileJson(secondJson, testInfo);

        JsonNode savedJson = mapper.readTree(jsonFile.toFile());

        assertTrue(savedJson.isArray());
        assertEquals(2, savedJson.size());
        assertEquals(1, savedJson.get(0).get("id").asInt());
        assertEquals(2, savedJson.get(1).get("id").asInt());
    }

    @Test
    @DisplayName("Сохранение json в существующий файл (сохранённые до этого в файле данные должны обновиться)")
    void outputFileJsonShouldAppendToExistingFile_whenRewrite() throws IOException {
        Path jsonFile = tempDir.resolve("output.json");
        String filename = jsonFile.getParent().resolve("output").toString();
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.fileMode = "rw";
        JsonNode firstJson = mapper.readTree("""
            {"id": 1, "source": "Weather", "data": {"temp": 20}}
            """);
        JsonNode secondJson = mapper.readTree("""
            {"id": 2, "source": "Weather", "data": {"temp": 22}}
            """);

        SaveData.outputFileJson(firstJson, testInfo);
        SaveData.outputFileJson(secondJson, testInfo);

        JsonNode savedJson = mapper.readTree(jsonFile.toFile());

        assertTrue(savedJson.isArray());
        assertEquals(2, savedJson.size());
        assertEquals(1, savedJson.get(0).get("id").asInt());
        assertEquals(2, savedJson.get(1).get("id").asInt());
    }

    //Tests for writeNewJsonFile()

//    @Test
//    @DisplayName("Тестирование записи writeNewJsonFile json в файл без ошибок")


    //Tests for outputFileCSV()

    @Test
    @DisplayName("Успешное сохранение корректного CSV файла (режим cr)")
    void outputFileCsvShouldCorrectlySaveCorrectCsv_whenCreateMode() throws IOException {
        // GIVEN
        Path csvFile = tempDir.resolve("output.csv");
        String filename = csvFile.getParent().resolve("output").toString();
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.fileMode = "cr";

        // Создаём тестовые данные для конвертора
        List<Map<String, String>> records = new ArrayList<>();
        Map<String, String> record = new LinkedHashMap<>();
        record.put("id", "1");
        record.put("source", "Weather");
        record.put("data.temp", "22.5");
        record.put("data.humidity", "65");
        records.add(record);

        List<String> allPaths = List.of("id", "source", "data.temp", "data.humidity");
        ConvertorResult convertResult = new ConvertorResult(allPaths, records);

        SaveData.outputFileCSV(convertResult, testInfo);

        assertTrue(Files.exists(csvFile));
        String content = Files.readString(csvFile);
        assertTrue(content.contains("id,source,data.temp,data.humidity"));
        assertTrue(content.contains("1,Weather,22.5,65"));
    }

    @Test
    @DisplayName("Сохранение CSV в существующий файл (режим cr с добавлением)")
    void outputFileCsvShouldAppendToExistingFile_whenCreateMode() throws IOException {
        // GIVEN
        Path csvFile = tempDir.resolve("output.csv");
        String filename = csvFile.getParent().resolve("output").toString();
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.fileMode = "cr";

        // Первая запись
        List<Map<String, String>> firstRecords = new ArrayList<>();
        Map<String, String> firstRecord = new LinkedHashMap<>();
        firstRecord.put("id", "1");
        firstRecord.put("source", "Weather");
        firstRecord.put("data.temp", "20");
        firstRecords.add(firstRecord);
        ConvertorResult firstResult = new ConvertorResult(List.of("id", "source", "data.temp"), firstRecords);

        // Вторая запись
        List<Map<String, String>> secondRecords = new ArrayList<>();
        Map<String, String> secondRecord = new LinkedHashMap<>();
        secondRecord.put("id", "2");
        secondRecord.put("source", "Weather");
        secondRecord.put("data.temp", "22");
        secondRecords.add(secondRecord);
        ConvertorResult secondResult = new ConvertorResult(List.of("id", "source", "data.temp"), secondRecords);

        // WHEN
        SaveData.outputFileCSV(firstResult, testInfo);
        SaveData.outputFileCSV(secondResult, testInfo);

        // THEN
        String content = Files.readString(csvFile);
        assertTrue(content.contains("1,Weather,20"));
        assertTrue(content.contains("2,Weather,22"));
    }

    @Test
    @DisplayName("Сохранение CSV в существующий файл (режим rw)")
    void outputFileCsvShouldAppendToExistingFile_whenRewriteMode() throws IOException {
        // GIVEN
        Path csvFile = tempDir.resolve("output.csv");
        String filename = csvFile.getParent().resolve("output").toString();
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.fileMode = "rw";

        // Первая запись
        List<Map<String, String>> firstRecords = new ArrayList<>();
        Map<String, String> firstRecord = new LinkedHashMap<>();
        firstRecord.put("id", "1");
        firstRecord.put("source", "Weather");
        firstRecord.put("data.temp", "20");
        firstRecords.add(firstRecord);
        ConvertorResult firstResult = new ConvertorResult(List.of("id", "source", "data.temp"), firstRecords);

        // Вторая запись
        List<Map<String, String>> secondRecords = new ArrayList<>();
        Map<String, String> secondRecord = new LinkedHashMap<>();
        secondRecord.put("id", "2");
        secondRecord.put("source", "Weather");
        secondRecord.put("data.temp", "22");
        secondRecords.add(secondRecord);
        ConvertorResult secondResult = new ConvertorResult(List.of("id", "source", "data.temp"), secondRecords);

        // WHEN
        SaveData.outputFileCSV(firstResult, testInfo);
        SaveData.outputFileCSV(secondResult, testInfo);

        // THEN
        String content = Files.readString(csvFile);
        assertTrue(content.contains("1,Weather,20"));
        assertTrue(content.contains("2,Weather,22"));
    }

    @Test
    @DisplayName("Сохранение CSV с разными заголовками — объединение колонок")
    void outputFileCsvShouldMergeHeaders_whenDifferentColumns() throws IOException {
        // GIVEN
        Path csvFile = tempDir.resolve("output.csv");
        String filename = csvFile.getParent().resolve("output").toString();
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.fileMode = "cr";

        List<Map<String, String>> firstRecords = new ArrayList<>();
        Map<String, String> firstRecord = new LinkedHashMap<>();
        firstRecord.put("id", "1");
        firstRecord.put("source", "Weather");
        firstRecord.put("data.temp", "20");
        firstRecord.put("data.humidity", "65");
        firstRecords.add(firstRecord);
        ConvertorResult firstResult = new ConvertorResult(
                List.of("id", "source", "data.temp", "data.humidity"),
                firstRecords
        );

        List<Map<String, String>> secondRecords = new ArrayList<>();
        Map<String, String> secondRecord = new LinkedHashMap<>();
        secondRecord.put("id", "2");
        secondRecord.put("source", "Weather");
        secondRecord.put("data.temp", "22");
        secondRecord.put("data.wind", "5.1");
        secondRecords.add(secondRecord);
        ConvertorResult secondResult = new ConvertorResult(
                List.of("id", "source", "data.temp", "data.wind"),
                secondRecords
        );

        SaveData.outputFileCSV(firstResult, testInfo);
        SaveData.outputFileCSV(secondResult, testInfo);

        String content = Files.readString(csvFile);
        assertTrue(content.contains("id,source,data.temp,data.humidity,data.wind"));
        assertTrue(content.contains("1,Weather,20,65,"));
        assertTrue(content.contains("2,Weather,22,,5.1"));
    }
}
