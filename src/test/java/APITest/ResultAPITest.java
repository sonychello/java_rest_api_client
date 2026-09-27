package APITest;

import org.example.workWithAPI.ResultAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class ResultAPITest {

    @Test
    @DisplayName("Создание пустого объекта ResultAPI")
    void testResultAPICreation() {
        ResultAPI result = new ResultAPI();

        assertNotNull(result);
        assertNull(result.textResult);
        assertNull(result.time);
        assertNull(result.nameAPI);
    }

    @Test
    @DisplayName("Установка текстового результата")
    void testSetTextResult() {
        ResultAPI result = new ResultAPI();
        StringBuffer expectedText = new StringBuffer("{\"key\":\"value\"}");
        result.textResult = expectedText;

        assertEquals(expectedText, result.textResult);
    }

    @Test
    @DisplayName("Установка времени запроса")
    void testSetTime() {
        ResultAPI result = new ResultAPI();
        String time = "2024-01-15T10:30:00Z";
        result.time = time;

        assertEquals(time, result.time);
    }

    @Test
    @DisplayName("Установка имени API")
    void testSetNameAPI() {
        ResultAPI result = new ResultAPI();
        String name = "WeatherAPI";
        result.nameAPI = name;

        assertEquals(name, result.nameAPI);
    }

    @Test
    @DisplayName("Установка всех полей одновременно")
    void testAllFieldsSet() {
        ResultAPI result = new ResultAPI();
        StringBuffer text = new StringBuffer("{\"data\":\"test\"}");
        String time = "2024-01-15T10:30:00Z";
        String name = "TestAPI";

        result.textResult = text;
        result.time = time;
        result.nameAPI = name;

        assertEquals(text, result.textResult);
        assertEquals(time, result.time);
        assertEquals(name, result.nameAPI);
    }
}