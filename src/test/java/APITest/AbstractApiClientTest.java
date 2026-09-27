package APITest;

import org.example.workWithAPI.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public abstract class AbstractApiClientTest {

    protected HttpURLConnection mockConnection;
    protected URL mockUrl;

    protected abstract ClientAPI createClient();
    protected abstract String getExpectedName();
    protected abstract String getTestUrl();
    protected abstract String getMockResponseBody();

    @BeforeEach
    void setUp() throws IOException {
        mockConnection = mock(HttpURLConnection.class);
        mockUrl = mock(URL.class);
    }

    @Test
    @DisplayName("Должен вернуть корректное имя API")
    void testGetName() {
        ClientAPI client = createClient();
        assertEquals(getExpectedName(), client.getName());
    }

    @Test
    @DisplayName("Должен успешно выполнить запрос и вернуть результат при статусе 200")
    void testGetRequestSuccess() throws Exception {
        // Настройка моков
        when(mockConnection.getResponseCode()).thenReturn(200);
        when(mockConnection.getInputStream()).thenReturn(
                new ByteArrayInputStream(getMockResponseBody().getBytes())
        );

        // Создаем частичный мок клиента
        ClientAPI client = spy(createClient());

        // Подменяем вызов getInformation
        doReturn(getResultFromMock(mockConnection)).when(client)
                .getInformation(anyString(), anyString());

        ResultAPI result = client.getRequest();

        assertNotNull(result);
        assertEquals(getExpectedName(), result.nameAPI);
        assertNotNull(result.time);
        assertNotNull(result.textResult);
        assertTrue(result.textResult.toString().contains(getExpectedContent()));
    }

    @Test
    @DisplayName("Должен выбросить исключение при ошибке HTTP (статус не 200)")
    void testGetRequestHttpError() throws Exception {
        when(mockConnection.getResponseCode()).thenReturn(404);

        ClientAPI client = spy(createClient());

        doThrow(new APIConnectException("HTTP 404 error"))
                .when(client).getInformation(anyString(), anyString());

        assertThrows(APIConnectException.class, client::getRequest);
    }

    @Test
    @DisplayName("Должен выбросить исключение при таймауте соединения")
    void testGetRequestTimeout() throws Exception {
        ClientAPI client = spy(createClient());

        doThrow(new APIConnectException("Connection timeout"))
                .when(client).getInformation(anyString(), anyString());

        assertThrows(APIConnectException.class, client::getRequest);
    }

    @Test
    @DisplayName("Должен выбросить исключение при ошибке ввода-вывода")
    void testGetRequestIOException() throws Exception {
        ClientAPI client = spy(createClient());

        doThrow(new APIConnectException("IO Error"))
                .when(client).getInformation(anyString(), anyString());

        assertThrows(APIConnectException.class, client::getRequest);
    }

    // Вспомогательные методы
    protected ResultAPI getResultFromMock(HttpURLConnection connection) throws IOException {
        ResultAPI result = new ResultAPI();
        result.nameAPI = getExpectedName();
        result.time = java.time.OffsetDateTime.now().toString();
        result.textResult = new StringBuffer(getMockResponseBody());
        return result;
    }

    protected abstract String getExpectedContent();
}