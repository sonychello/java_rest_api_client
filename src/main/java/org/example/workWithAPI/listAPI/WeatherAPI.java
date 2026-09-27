package org.example.workWithAPI.listAPI;

import org.example.config.AppConfig;
import org.example.workWithAPI.APIConnectException;
import org.example.workWithAPI.ResultAPI;
import org.example.workWithAPI.ClientAPI;

public class WeatherAPI implements ClientAPI {
    static final String API_KEY = AppConfig.getWeatherApiKey();
    private static final String nameAPI = "Weather";

    @Override
    public ResultAPI getRequest() throws APIConnectException {
            String urlString = "https://api.openweathermap.org/data/2.5/weather?lat=60.021856&lon=30.388511&appid="+API_KEY;
            return getInformation(urlString, nameAPI);
    }


    @Override
    public String getName() {
        return nameAPI;
    }
}
