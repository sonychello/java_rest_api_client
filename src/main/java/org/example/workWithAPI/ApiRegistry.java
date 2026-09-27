package org.example.workWithAPI;

import org.example.workWithAPI.listAPI.*;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public class ApiRegistry {
    private static final ConcurrentMap<String, ClientAPI> API_MAP = new ConcurrentHashMap<>();
    private static final Set<String> API_NAMES;

    private static final ConcurrentMap<String, Long> LAST_COMPLETION_TIME = new ConcurrentHashMap<>();

    private static final ConcurrentMap<String, Boolean> IS_RUNNING = new ConcurrentHashMap<>();

    static {
        API_MAP.put("Weather", new WeatherAPI());
        API_MAP.put("AnimeNews", new AnimeAPI());
        API_MAP.put("Crypt", new CryptAPI());
        API_MAP.put("Books", new BooksAPI());
        API_MAP.put("Alliexpress", new AlliexpressAPI());
        API_NAMES = Collections.unmodifiableSet(API_MAP.keySet());
    }

    public static ClientAPI getApi(String name) { return API_MAP.get(name); }
    public static Set<String> getApiNames() { return API_NAMES; }
    public static boolean contains(String name) { return API_MAP.containsKey(name); }

    public static boolean canLaunch(String apiName, long intervalMs) {
        if (Boolean.TRUE.equals(IS_RUNNING.get(apiName))) {
            return false;
        }

        Long lastCompletion = LAST_COMPLETION_TIME.get(apiName);
        if (lastCompletion != null) {
            long elapsed = System.currentTimeMillis() - lastCompletion;
            return elapsed >= intervalMs;
        }

        return true;
    }

    public static void markApiStarted(String apiName) {
        IS_RUNNING.put(apiName, true);
    }

    public static void markApiCompleted(String apiName) {
        LAST_COMPLETION_TIME.put(apiName, System.currentTimeMillis());
        IS_RUNNING.put(apiName, false);
    }

    public static void clearAll() {
        LAST_COMPLETION_TIME.clear();
        IS_RUNNING.clear();
    }
}