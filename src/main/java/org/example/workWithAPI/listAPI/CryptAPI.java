package org.example.workWithAPI.listAPI;

import org.example.workWithAPI.APIConnectException;
import org.example.workWithAPI.ClientAPI;
import org.example.workWithAPI.ResultAPI;

public class CryptAPI implements ClientAPI {
    private static final String nameAPI = "Crypt";

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
