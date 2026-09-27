package APITest;

import org.example.workWithAPI.ClientAPI;
import org.example.workWithAPI.APIConnectException;
import org.example.workWithAPI.listAPI.AlliexpressAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AlliexpressAPITest extends AbstractApiClientTest {

    @Override
    protected ClientAPI createClient() {
        return new AlliexpressAPI();
    }

    @Override
    protected String getExpectedName() {
        return "Alliexpress";
    }

    @Override
    protected String getTestUrl() {
        return ""; // URL пустой
    }

    @Override
    protected String getMockResponseBody() {
        return "{}";
    }

    @Override
    protected String getExpectedContent() {
        return "";
    }

    @Test
    @DisplayName("Alliexpress API должен выбрасывать исключение при пустом URL")
    void testAlliexpressAPIWithEmptyUrl() {
        AlliexpressAPI alliexpressAPI = new AlliexpressAPI();

        assertThrows(APIConnectException.class, alliexpressAPI::getRequest);
    }
}