package APITest;

import org.example.workWithAPI.APIConnectException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class APIConnectExceptionTest {

    @Test
    @DisplayName("Создание исключения с сообщением об ошибке")
    void testExceptionCreation() {
        String errorMessage = "Connection timeout";
        APIConnectException exception = new APIConnectException(errorMessage);

        assertNotNull(exception);
        assertInstanceOf(Exception.class, exception);
    }

    @Test
    @DisplayName("Получение форматированного сообщения об ошибке")
    void testGetMessage() {
        String errorMessage = "API key invalid";
        APIConnectException exception = new APIConnectException(errorMessage);

        String expected = "APIConnectException [" + errorMessage + "]";
        assertEquals(expected, exception.getMessage());
    }

    @Test
    @DisplayName("Создание исключения с пустым сообщением")
    void testEmptyErrorMessage() {
        APIConnectException exception = new APIConnectException("");

        assertEquals("APIConnectException []", exception.getMessage());
    }

    @Test
    @DisplayName("Создание исключения с null сообщением")
    void testNullErrorMessage() {
        APIConnectException exception = new APIConnectException(null);

        assertEquals("APIConnectException [null]", exception.getMessage());
    }
}