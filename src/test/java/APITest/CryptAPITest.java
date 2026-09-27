package APITest;

import org.example.workWithAPI.ClientAPI;
import org.example.workWithAPI.APIConnectException;
import org.example.workWithAPI.listAPI.CryptAPI;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CryptAPITest extends AbstractApiClientTest {

    @Override
    protected ClientAPI createClient() {
        return new CryptAPI();
    }

    @Override
    protected String getExpectedName() {
        return "Crypt";
    }

    @Override
    protected String getTestUrl() {
        return ""; // URL пустой, что вызовет ошибку
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
    @DisplayName("Crypt API должен выбрасывать исключение при пустом URL")
    void testCryptAPIWithEmptyUrl() {
        CryptAPI cryptAPI = new CryptAPI();

        assertThrows(APIConnectException.class, cryptAPI::getRequest);
    }
}