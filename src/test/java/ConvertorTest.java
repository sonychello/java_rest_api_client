import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.convertor.Convertor;
import org.example.convertor.ConvertorResult;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Convertor - конвертация JSON в плоскую структуру для CSV")
public class ConvertorTest {
    private final ObjectMapper mapper = new ObjectMapper();

    // ==================== ТЕСТЫ ДЛЯ ПУСТЫХ ВХОДНЫХ ДАННЫХ ====================

    @Test
    @DisplayName("convert() возвращает пустой результат при null")
    void convert_shouldReturnEmpty_whenInputIsNull() throws IOException {
        // When
        ConvertorResult result = Convertor.convert(null);

        // Then
        assertNotNull(result);
        assertTrue(result.records().isEmpty());
        assertTrue(result.allPaths().isEmpty());
    }

    @Test
    @DisplayName("convert() возвращает пустой результат при пустом массиве")
    void convert_shouldReturnEmpty_whenInputIsEmptyArray() throws IOException {
        // Given
        ArrayNode emptyArray = mapper.createArrayNode();

        // When
        ConvertorResult result = Convertor.convert(emptyArray);

        // Then
        assertNotNull(result);
        assertTrue(result.records().isEmpty());
        assertTrue(result.allPaths().isEmpty());
    }

    @Test
    @DisplayName("convert() возвращает пустой результат если корневой узел не массив")
    void convert_shouldReturnEmpty_whenRootIsNotArray() throws IOException {
        // Given
        ObjectNode objectNode = mapper.createObjectNode();
        objectNode.put("key", "value");

        // When
        ConvertorResult result = Convertor.convert(objectNode);

        // Then
        assertNotNull(result);
        assertTrue(result.records().isEmpty());
        assertTrue(result.allPaths().isEmpty());
    }

    // ==================== ТЕСТЫ ДЛЯ ПРОСТЫХ ОБЪЕКТОВ (БЕЗ МАССИВОВ) ====================

    @Test
    @DisplayName("Конвертация простого объекта без вложенных массивов")
    void convert_shouldFlattenSimpleObject() throws IOException {
        // Given — JSON обёрнут в массив []
        String json = """
                [{
                    "id": 1,
                    "source": "Weather",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "data": {
                        "temperature": 22.5,
                        "humidity": 65,
                        "city": "Moscow"
                    }
                }]
                """;
        ArrayNode rootArray = (ArrayNode) mapper.readTree(json);

        // When
        ConvertorResult result = Convertor.convert(rootArray);

        // Then
        assertNotNull(result);
        assertEquals(1, result.records().size());

        Map<String, String> record = result.records().getFirst();
        assertEquals("1", record.get("id"));
        assertEquals("Weather", record.get("source"));
        assertEquals("2024-01-15T10:30:00Z", record.get("timestamp"));
        assertEquals("22.5", record.get("data.temperature"));
        assertEquals("65", record.get("data.humidity"));
        assertEquals("Moscow", record.get("data.city"));

        // Проверяем, что все пути собраны
        assertTrue(result.allPaths().contains("id"));
        assertTrue(result.allPaths().contains("source"));
        assertTrue(result.allPaths().contains("timestamp"));
        assertTrue(result.allPaths().contains("data.temperature"));
        assertTrue(result.allPaths().contains("data.humidity"));
        assertTrue(result.allPaths().contains("data.city"));
        assertEquals(6, result.allPaths().size());
    }

    // ==================== ТЕСТЫ ДЛЯ ОБЪЕКТОВ С МАССИВАМИ ====================

    @Test
    @DisplayName("Конвертация объекта с массивом — разворачивает массив в несколько строк")
    void convert_shouldExpandArrayIntoSeveralRows() throws IOException {
        // Given — JSON обёрнут в массив []
        String json = """
                [{
                    "id": 1,
                    "source": "Books",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "data": {
                        "author": "Tolkien",
                        "books": [
                            {"title": "The Hobbit", "year": 1937},
                            {"title": "The Lord of the Rings", "year": 1954},
                            {"title": "The Silmarillion", "year": 1977}
                        ]
                    }
                }]
                """;
        ArrayNode rootArray = (ArrayNode) mapper.readTree(json);

        // When
        ConvertorResult result = Convertor.convert(rootArray);

        // Then
        assertNotNull(result);
        assertEquals(3, result.records().size());

        // Проверяем первую строку
        Map<String, String> firstRecord = result.records().getFirst();
        assertEquals("1", firstRecord.get("id"));
        assertEquals("Books", firstRecord.get("source"));
        assertEquals("2024-01-15T10:30:00Z", firstRecord.get("timestamp"));
        assertEquals("Tolkien", firstRecord.get("data.author"));
        assertEquals("The Hobbit", firstRecord.get("data.books.title"));
        assertEquals("1937", firstRecord.get("data.books.year"));

        // Проверяем вторую строку
        Map<String, String> secondRecord = result.records().get(1);
        assertEquals("The Lord of the Rings", secondRecord.get("data.books.title"));
        assertEquals("1954", secondRecord.get("data.books.year"));

        // Проверяем третью строку
        Map<String, String> thirdRecord = result.records().get(2);
        assertEquals("The Silmarillion", thirdRecord.get("data.books.title"));
        assertEquals("1977", thirdRecord.get("data.books.year"));

        // Проверяем, что id, source, timestamp одинаковые во всех строках
        for (Map<String, String> record : result.records()) {
            assertEquals("1", record.get("id"));
            assertEquals("Books", record.get("source"));
            assertEquals("2024-01-15T10:30:00Z", record.get("timestamp"));
            assertEquals("Tolkien", record.get("data.author"));
        }
    }

    @Test
    @DisplayName("Конвертация с несколькими массивами — разворачивает только первый")
    void convert_shouldExpandOnlyFirstArray_whenMultipleArrays() throws IOException {
        // Given — JSON обёрнут в массив []
        String json = """
                [{
                    "id": 1,
                    "source": "Test",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "data": {
                        "tags": ["java", "testing", "json"],
                        "comments": [
                            {"user": "Alice", "text": "Good"},
                            {"user": "Bob", "text": "Nice"}
                        ]
                    }
                }]
                """;
        ArrayNode rootArray = (ArrayNode) mapper.readTree(json);

        // When
        ConvertorResult result = Convertor.convert(rootArray);

        // Then
        assertNotNull(result);
        assertEquals(3, result.records().size());

        // Проверяем первую строку (первый тег)
        Map<String, String> firstRecord = result.records().getFirst();
        assertEquals("java", firstRecord.get("data.tags"));
        assertEquals("Alice", firstRecord.get("data.comments.user"));
        assertEquals("Good", firstRecord.get("data.comments.text"));

        // Проверяем вторую строку (второй тег)
        Map<String, String> secondRecord = result.records().get(1);
        assertEquals("testing", secondRecord.get("data.tags"));
        assertEquals("Alice", secondRecord.get("data.comments.user"));

        // Проверяем третью строку (третий тег)
        Map<String, String> thirdRecord = result.records().get(2);
        assertEquals("json", thirdRecord.get("data.tags"));
    }

    // ==================== ТЕСТЫ ДЛЯ ВЛОЖЕННЫХ ОБЪЕКТОВ ====================

    @Test
    @DisplayName("Конвертация глубоко вложенных объектов (рекурсивное уплощение)")
    void convert_shouldFlattenDeeplyNestedObjects() throws IOException {
        // Given — JSON обёрнут в массив []
        String json = """
                [{
                    "id": 1,
                    "source": "Weather",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "data": {
                        "location": {
                            "city": "Moscow",
                            "coordinates": {
                                "lat": 55.7558,
                                "lon": 37.6173
                            }
                        },
                        "weather": {
                            "main": "Clouds",
                            "description": "scattered clouds"
                        }
                    }
                }]
                """;
        ArrayNode rootArray = (ArrayNode) mapper.readTree(json);

        // When
        ConvertorResult result = Convertor.convert(rootArray);

        // Then
        assertNotNull(result);
        assertEquals(1, result.records().size());

        Map<String, String> record = result.records().getFirst();
        assertEquals("Moscow", record.get("data.location.city"));
        assertEquals("55.7558", record.get("data.location.coordinates.lat"));
        assertEquals("37.6173", record.get("data.location.coordinates.lon"));
        assertEquals("Clouds", record.get("data.weather.main"));
        assertEquals("scattered clouds", record.get("data.weather.description"));

        // Проверяем, что промежуточные ключи не появились
        assertNull(record.get("data.location"));
        assertNull(record.get("data.location.coordinates"));
    }

    // ==================== ТЕСТЫ ДЛЯ РАЗНЫХ ТИПОВ ДАННЫХ ====================

    @Test
    @DisplayName("Корректная обработка разных типов значений")
    void convert_shouldHandleDifferentValueTypes() throws IOException {
        // Given — JSON обёрнут в массив []
        String json = """
                [{
                    "id": 1,
                    "source": "Test",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "data": {
                        "intValue": 42,
                        "doubleValue": 3.14159,
                        "booleanTrue": true,
                        "booleanFalse": false,
                        "nullValue": null,
                        "stringValue": "hello"
                    }
                }]
                """;
        ArrayNode rootArray = (ArrayNode) mapper.readTree(json);

        // When
        ConvertorResult result = Convertor.convert(rootArray);

        // Then
        assertNotNull(result);
        assertEquals(1, result.records().size());

        Map<String, String> record = result.records().getFirst();
        assertEquals("42", record.get("data.intValue"));
        assertEquals("3.14159", record.get("data.doubleValue"));
        assertEquals("true", record.get("data.booleanTrue"));
        assertEquals("false", record.get("data.booleanFalse"));
        assertEquals("", record.get("data.nullValue"));
        assertEquals("hello", record.get("data.stringValue"));
    }

    // ==================== ТЕСТЫ С РЕАЛЬНЫМИ ДАННЫМИ ИЗ ТВОИХ API ====================

    @Test
    @DisplayName("Реальный сценарий: ответ WeatherAPI")
    void convert_shouldHandleRealWeatherApiResponse() throws IOException {
        // Given — JSON обёрнут в массив []
        String json = """
                [{
                    "id": 1,
                    "source": "Weather",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "data": {
                        "main": {
                            "temp": 275.15,
                            "feels_like": 271.15,
                            "humidity": 87
                        },
                        "wind": {
                            "speed": 5.1
                        },
                        "name": "Saint Petersburg"
                    }
                }]
                """;
        ArrayNode rootArray = (ArrayNode) mapper.readTree(json);

        // When
        ConvertorResult result = Convertor.convert(rootArray);

        // Then
        assertNotNull(result);
        assertEquals(1, result.records().size());

        Map<String, String> record = result.records().getFirst();
        assertEquals("Weather", record.get("source"));
        assertEquals("275.15", record.get("data.main.temp"));
        assertEquals("271.15", record.get("data.main.feels_like"));
        assertEquals("87", record.get("data.main.humidity"));
        assertEquals("5.1", record.get("data.wind.speed"));
        assertEquals("Saint Petersburg", record.get("data.name"));
    }

    @Test
    @DisplayName("Реальный сценарий: ответ AnimeAPI с массивом новостей")
    void convert_shouldHandleRealAnimeApiResponse() throws IOException {
        // Given — JSON обёрнут в массив []
        String json = """
                [{
                    "id": 1,
                    "source": "AnimeNews",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "data": {
                        "news": [
                            {"title": "New Anime Announced", "date": "2024-01-10"},
                            {"title": "Studio Closes", "date": "2024-01-12"},
                            {"title": "Movie Release", "date": "2024-01-14"}
                        ]
                    }
                }]
                """;
        ArrayNode rootArray = (ArrayNode) mapper.readTree(json);

        // When
        ConvertorResult result = Convertor.convert(rootArray);

        // Then
        assertNotNull(result);
        assertEquals(3, result.records().size());

        // Проверяем первую новость
        Map<String, String> firstRecord = result.records().getFirst();
        assertEquals("AnimeNews", firstRecord.get("source"));
        assertEquals("New Anime Announced", firstRecord.get("data.news.title"));
        assertEquals("2024-01-10", firstRecord.get("data.news.date"));

        // Проверяем вторую новость
        Map<String, String> secondRecord = result.records().get(1);
        assertEquals("Studio Closes", secondRecord.get("data.news.title"));
        assertEquals("2024-01-12", secondRecord.get("data.news.date"));

        // Проверяем третью новость
        Map<String, String> thirdRecord = result.records().get(2);
        assertEquals("Movie Release", thirdRecord.get("data.news.title"));
        assertEquals("2024-01-14", thirdRecord.get("data.news.date"));

        // Проверяем, что заголовки (пути) собраны правильно
        assertTrue(result.allPaths().contains("id"));
        assertTrue(result.allPaths().contains("source"));
        assertTrue(result.allPaths().contains("timestamp"));
        assertTrue(result.allPaths().contains("data.news.title"));
        assertTrue(result.allPaths().contains("data.news.date"));
    }

    // ==================== ТЕСТЫ ДЛЯ НЕСКОЛЬКИХ ЗАПИСЕЙ В КОРНЕ ====================

    @Test
    @DisplayName("Несколько записей в корневом массиве — обрабатываются все")
    void convert_shouldHandleMultipleRootRecords() throws IOException {
        // Given — этот JSON уже является массивом, ничего менять не нужно
        String json = """
                [
                    {
                        "id": 1,
                        "source": "Weather",
                        "timestamp": "2024-01-15T10:30:00Z",
                        "data": {"temp": 20}
                    },
                    {
                        "id": 2,
                        "source": "Weather",
                        "timestamp": "2024-01-15T10:35:00Z",
                        "data": {"temp": 22}
                    },
                    {
                        "id": 3,
                        "source": "Weather",
                        "timestamp": "2024-01-15T10:40:00Z",
                        "data": {"temp": 21}
                    }
                ]
                """;
        ArrayNode rootArray = (ArrayNode) mapper.readTree(json);

        // When
        ConvertorResult result = Convertor.convert(rootArray);

        // Then
        assertNotNull(result);
        assertEquals(3, result.records().size());

        assertEquals("20", result.records().get(0).get("data.temp"));
        assertEquals("22", result.records().get(1).get("data.temp"));
        assertEquals("21", result.records().get(2).get("data.temp"));

        assertEquals("1", result.records().get(0).get("id"));
        assertEquals("2", result.records().get(1).get("id"));
        assertEquals("3", result.records().get(2).get("id"));

        assertEquals("Weather", result.records().get(0).get("source"));
        assertEquals("2024-01-15T10:30:00Z", result.records().get(0).get("timestamp"));
        assertEquals("2024-01-15T10:35:00Z", result.records().get(1).get("timestamp"));
        assertEquals("2024-01-15T10:40:00Z", result.records().get(2).get("timestamp"));
    }

    // ==================== ТЕСТ ДЛЯ ПУСТОГО DATA ====================

    @Test
    @DisplayName("Если data пустой, запись всё равно создаётся с базовыми полями")
    void convert_shouldCreateRecordWithBaseFields_whenDataIsEmpty() throws IOException {
        // Given — JSON обёрнут в массив []
        String json = """
                [{
                    "id": 1,
                    "source": "Test",
                    "timestamp": "2024-01-15T10:30:00Z",
                    "data": {}
                }]
                """;
        ArrayNode rootArray = (ArrayNode) mapper.readTree(json);

        // When
        ConvertorResult result = Convertor.convert(rootArray);

        // Then
        assertNotNull(result);
        assertEquals(1, result.records().size());

        Map<String, String> record = result.records().getFirst();
        assertEquals("1", record.get("id"));
        assertEquals("Test", record.get("source"));
        assertEquals("2024-01-15T10:30:00Z", record.get("timestamp"));
        assertNull(record.get("data"));
    }

    // ==================== ТЕСТ ДЛЯ ОТСУТСТВУЮЩЕГО DATA ====================

    @Test
    @DisplayName("Если data отсутствует, запись всё равно создаётся с базовыми полями")
    void convert_shouldCreateRecordWithBaseFields_whenDataIsMissing() throws IOException {
        // Given — JSON обёрнут в массив []
        String json = """
                [{
                    "id": 1,
                    "source": "Test",
                    "timestamp": "2024-01-15T10:30:00Z"
                }]
                """;
        ArrayNode rootArray = (ArrayNode) mapper.readTree(json);

        // When
        ConvertorResult result = Convertor.convert(rootArray);

        // Then
        assertNotNull(result);
        assertEquals(1, result.records().size());

        Map<String, String> record = result.records().getFirst();
        assertEquals("1", record.get("id"));
        assertEquals("Test", record.get("source"));
        assertEquals("2024-01-15T10:30:00Z", record.get("timestamp"));
    }
}