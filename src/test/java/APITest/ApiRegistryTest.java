package APITest;

import org.example.workWithAPI.ApiRegistry;
import org.example.workWithAPI.ClientAPI;
import org.junit.jupiter.api.*;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ApiRegistryTest {

    @BeforeEach
    void setUp() {
        ApiRegistry.clearAll();
    }

    @Test
    @DisplayName("Получение существующего API по имени")
    void testGetApiExisting() {
        ClientAPI weatherApi = ApiRegistry.getApi("Weather");
        assertNotNull(weatherApi);
        assertEquals("Weather", weatherApi.getName());
    }

    @Test
    @DisplayName("Получение несуществующего API возвращает null")
    void testGetApiNonExisting() {
        ClientAPI nonExistingApi = ApiRegistry.getApi("NonExisting");
        assertNull(nonExistingApi);
    }

    @Test
    @DisplayName("Имена API чувствительны к регистру")
    void testGetApiCaseSensitive() {
        ClientAPI lowerCaseApi = ApiRegistry.getApi("weather");
        assertNull(lowerCaseApi);

        ClientAPI correctCaseApi = ApiRegistry.getApi("Weather");
        assertNotNull(correctCaseApi);
    }

    @Test
    @DisplayName("Получение списка всех имен API")
    void testGetApiNames() {
        Set<String> apiNames = ApiRegistry.getApiNames();

        assertNotNull(apiNames);
        assertEquals(5, apiNames.size());
        assertTrue(apiNames.containsAll(Set.of("Weather", "AnimeNews", "Crypt", "Books", "Alliexpress")));
    }

    @Test
    @DisplayName("Список имен API нельзя изменить")
    void testGetApiNamesIsUnmodifiable() {
        Set<String> apiNames = ApiRegistry.getApiNames();

        assertThrows(UnsupportedOperationException.class, () -> apiNames.add("NewAPI"));
    }

    @Test
    @DisplayName("Проверка существования API в реестре")
    void testContains() {
        assertTrue(ApiRegistry.contains("Weather"));
        assertTrue(ApiRegistry.contains("Books"));
        assertFalse(ApiRegistry.contains("NonExisting"));
    }

    @Test
    @DisplayName("canLaunch() возвращает true, если API не запущен и интервал прошел")
    void testCanLaunchReturnsTrue_whenNotRunningAndIntervalPassed() throws InterruptedException {
        String apiName = "Weather";

        assertTrue(ApiRegistry.canLaunch(apiName, 100));

        ApiRegistry.markApiStarted(apiName);
        assertFalse(ApiRegistry.canLaunch(apiName, 100));

        ApiRegistry.markApiCompleted(apiName);
        Thread.sleep(150);
        assertTrue(ApiRegistry.canLaunch(apiName, 100));
    }

    @Test
    @DisplayName("canLaunch() возвращает false, если API запущен")
    void testCanLaunchReturnsFalse_whenApiIsRunning() {
        String apiName = "Weather";

        ApiRegistry.markApiStarted(apiName);
        assertFalse(ApiRegistry.canLaunch(apiName, 100));
    }

    @Test
    @DisplayName("markApiStarted() и markApiCompleted() корректно обновляют состояние")
    void testMarkApiStartedAndCompleted() {
        String apiName = "Weather";

        assertTrue(ApiRegistry.canLaunch(apiName, 100));

        ApiRegistry.markApiStarted(apiName);
        assertFalse(ApiRegistry.canLaunch(apiName, 100));

        ApiRegistry.markApiCompleted(apiName);
        assertTrue(ApiRegistry.canLaunch(apiName, 0));
    }

    @Test
    @DisplayName("clearAll() очищает состояние всех API")
    void testClearAll() {
        String apiName = "Weather";

        ApiRegistry.markApiStarted(apiName);
        ApiRegistry.markApiCompleted(apiName);

        ApiRegistry.clearAll();

        assertTrue(ApiRegistry.canLaunch(apiName, 100));
    }
}