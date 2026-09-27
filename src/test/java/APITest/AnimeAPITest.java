package APITest;


import org.example.workWithAPI.ClientAPI;
import org.example.workWithAPI.listAPI.AnimeAPI;

class AnimeAPITest extends AbstractApiClientTest {

    private static final String MOCK_RESPONSE = "{\"news\":[{\"title\":\"New Anime Released\",\"content\":\"Exciting news about anime\"}]}";

    @Override
    protected ClientAPI createClient() {
        return new AnimeAPI();
    }

    @Override
    protected String getExpectedName() {
        return "AnimeNews";
    }

    @Override
    protected String getTestUrl() {
        return "https://aninews.vercel.app/api/news?source=all&limit=4";
    }

    @Override
    protected String getMockResponseBody() {
        return MOCK_RESPONSE;
    }

    @Override
    protected String getExpectedContent() {
        return "New Anime Released";
    }
}