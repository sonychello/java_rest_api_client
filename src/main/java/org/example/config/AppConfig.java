package org.example.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class AppConfig {
    private static final Properties props = new Properties();
    private static boolean loaded = false;

    private static void load() {
        if (loaded) return;

        try (InputStream input = AppConfig.class.getClassLoader()
                .getResourceAsStream("config.properties")) {

            if (input == null) {
                System.err.println("Warning: config.properties not found, using defaults");
                loaded = true;
                return;
            }

            props.load(input);
            loaded = true;

        } catch (IOException e) {
            System.err.println("Error loading config: " + e.getMessage());
        }
    }

    public static String getWeatherApiKey() {
        load();
        return props.getProperty("weather.api.key", "");
    }
}
