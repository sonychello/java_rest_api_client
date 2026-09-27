package APITest;


import org.example.workWithAPI.ClientAPI;
import org.example.workWithAPI.listAPI.WeatherAPI;
import org.junit.jupiter.api.DisplayName;

class WeatherAPITest extends AbstractApiClientTest {

    private static final String MOCK_RESPONSE = "{\"weather\":[{\"description\":\"clear sky\"}],\"main\":{\"temp\":293.15}}";

    @Override
    protected ClientAPI createClient() {
        return new WeatherAPI();
    }

    @Override
    protected String getExpectedName() {
        return "Weather";
    }

    @Override
    protected String getTestUrl() {
        return "https://api.openweathermap.org/data/2.5/weather?lat=60.021856&lon=30.388511&appid=test_key";
    }

    @Override
    protected String getMockResponseBody() {
        return MOCK_RESPONSE;
    }

    @Override
    protected String getExpectedContent() {
        return "clear sky";
    }
}