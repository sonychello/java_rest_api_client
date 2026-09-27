package org.example.workWithAPI.listAPI;

import org.example.workWithAPI.APIConnectException;
import org.example.workWithAPI.ResultAPI;
import org.example.workWithAPI.ClientAPI;


public class BooksAPI implements ClientAPI {
    private static final String nameAPI = "Books";

    @Override
    public ResultAPI getRequest() throws APIConnectException {
        String urlString = "https://openlibrary.org/search.json?author=tolkien";
        return getInformation(urlString, nameAPI);
    }


    @Override
    public String getName() {
        return nameAPI;
    }
}
