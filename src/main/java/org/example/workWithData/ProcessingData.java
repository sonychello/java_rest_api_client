package org.example.workWithData;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.workWithAPI.ResultAPI;

import java.util.concurrent.atomic.AtomicInteger;

public class ProcessingData {
    static ObjectMapper mapper = new ObjectMapper();
    private static final AtomicInteger requestCounter = new AtomicInteger(0);
    public static JsonNode processingDataToJSON(ResultAPI data) throws JsonProcessingException {
        if (data.textResult.isEmpty()) {
            return null;
        }
        int id = requestCounter.incrementAndGet();

        ObjectNode result = mapper.createObjectNode();

        result.put("id", id);
        result.put("source", data.nameAPI);
        result.put("timestamp", data.time);
        JsonNode apiData = mapper.readTree(data.textResult.toString());
        result.set("data", apiData);
        return result;

    }

    // В классе ProcessingData
    public static void resetCounter() {
        requestCounter.set(0);
    }

}
