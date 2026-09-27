import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.workWithAPI.ResultAPI;
import org.example.workWithData.ProcessingData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.time.OffsetDateTime;

public class ProcessingDataTest {
    private static ResultAPI resultAPI;

    @BeforeEach
    void setUp() {
        resultAPI = new ResultAPI();
    }

    @Test
    @DisplayName("processingDataToJSON() должен вернуть null при пустых данных")
    void processingShouldReturnNull_whenJsonDataIsEmpty() throws JsonProcessingException {
        resultAPI.textResult = new StringBuffer();
        resultAPI.nameAPI = "Weather";
        OffsetDateTime now = OffsetDateTime.now();
        resultAPI.time = now.toString();

        JsonNode result = ProcessingData.processingDataToJSON(resultAPI);
        assertNull(result);
    }

    @Test
    @DisplayName("Корректная обработка данных из API в json.")
    void processingShouldCorrectlyProcessData_whenJsonDataIsNotEmpty() throws JsonProcessingException {
        resultAPI.textResult = new StringBuffer("""
                {
                    "cloud": "yes",
                    "wind": 10,
                    "cold": "yes"
                }""");
        resultAPI.nameAPI = "Weather";
        OffsetDateTime now = OffsetDateTime.now();
        resultAPI.time = now.toString();
        JsonNode result = ProcessingData.processingDataToJSON(resultAPI);
        assertNotNull(result);
        assertEquals("Weather", result.get("source").asText());
        assertEquals(now.toString(), result.get("timestamp").asText());
        assertTrue(result.has("id"));
        assertTrue(result.get("id").isInt());

        JsonNode data = result.get("data");
        assertNotNull(data);
        assertEquals("yes", data.get("cloud").asText());
        assertEquals(10, data.get("wind").asInt());
        assertEquals("yes", data.get("cold").asText());
    }

    @Test
    @DisplayName("Тестирование верного подсчёта ID запроса")
    void processingShouldCorrectlyCountID_whenMethodCallSomeTimes() throws JsonProcessingException {
        resultAPI.textResult = new StringBuffer("""
                {
                    "cloud": "yes",
                    "wind": 10,
                    "cold": "yes"
                }""");
        resultAPI.nameAPI = "Weather";
        OffsetDateTime now = OffsetDateTime.now();
        resultAPI.time = now.toString();
        ProcessingData.resetCounter();

        ProcessingData.processingDataToJSON(resultAPI);
        ProcessingData.processingDataToJSON(resultAPI);
        JsonNode result = ProcessingData.processingDataToJSON(resultAPI);

        assertNotNull(result);
        assertEquals(3, result.get("id").asInt());
    }

}
