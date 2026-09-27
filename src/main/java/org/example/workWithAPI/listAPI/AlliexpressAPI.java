package org.example.workWithAPI.listAPI;

import org.example.workWithAPI.APIConnectException;
import org.example.workWithAPI.ClientAPI;
import org.example.workWithAPI.ResultAPI;

public class AlliexpressAPI implements ClientAPI {
    private static final String nameAPI = "Alliexpress";

    @Override
    public ResultAPI getRequest() throws APIConnectException {
        String urlString = "";
        return getInformation(urlString, nameAPI);
    }

    @Override
    public String getName() {
        return nameAPI;
    }
}
