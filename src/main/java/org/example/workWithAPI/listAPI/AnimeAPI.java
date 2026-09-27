package org.example.workWithAPI.listAPI;

import org.example.workWithAPI.APIConnectException;
import org.example.workWithAPI.ResultAPI;
import org.example.workWithAPI.ClientAPI;


public class AnimeAPI implements ClientAPI {
    private static final String nameAPI = "AnimeNews";

    @Override
    public ResultAPI getRequest() throws APIConnectException {
        String urlString = "https://aninews.vercel.app/api/news?source=all&limit=4";
        return getInformation(urlString, nameAPI);
    }
    @Override
    public String getName() {
        return nameAPI;
    }
}
