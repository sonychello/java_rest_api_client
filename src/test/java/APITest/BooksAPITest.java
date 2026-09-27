package APITest;

import org.example.workWithAPI.ClientAPI;
import org.example.workWithAPI.listAPI.BooksAPI;
import org.junit.jupiter.api.DisplayName;

class BooksAPITest extends AbstractApiClientTest {

    private static final String MOCK_RESPONSE = "{\"docs\":[{\"title\":\"The Hobbit\",\"author_name\":[\"Tolkien\"]}]}";

    @Override
    protected ClientAPI createClient() {
        return new BooksAPI();
    }

    @Override
    protected String getExpectedName() {
        return "Books";
    }

    @Override
    protected String getTestUrl() {
        return "https://openlibrary.org/search.json?author=tolkien";
    }

    @Override
    protected String getMockResponseBody() {
        return MOCK_RESPONSE;
    }

    @Override
    protected String getExpectedContent() {
        return "The Hobbit";
    }
}