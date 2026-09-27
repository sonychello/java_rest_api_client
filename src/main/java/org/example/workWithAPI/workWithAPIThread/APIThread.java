package org.example.workWithAPI.workWithAPIThread;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import org.example.Main;
import org.example.convertor.Convertor;
import org.example.convertor.ConvertorResult;
import org.example.workWithAPI.APIConnectException;
import org.example.workWithAPI.ApiRegistry;
import org.example.workWithAPI.ClientAPI;
import org.example.workWithData.ProcessingData;
import org.example.workWithData.SaveData;

import java.io.IOException;

public class APIThread implements Runnable {
    private final ClientAPI api;
    private final Main.Info info;

    public APIThread(ClientAPI api, Main.Info info) {
        this.api = api;
        this.info = info;
    }

    @Override
    public void run() {
        try {

            JsonNode resultAPI = ProcessingData.processingDataToJSON(api.getRequest());

            if (resultAPI == null) {
                System.out.println("[" + api.getName() + "] There isn't answer to request.");
                return;
            }

            switch (info.outType) {
                case "json":
                    SaveData.outputFileJson(resultAPI, info);
                    break;
                case "csv":
                    try {
                        ArrayNode wrapper = new ObjectMapper().createArrayNode();
                        wrapper.add(resultAPI);
                        ConvertorResult convertResult = Convertor.convert(wrapper);
                        if (convertResult.records().isEmpty()) {
                            System.out.println("[" + api.getName() + "] There isn't convert result.");
                            return;
                        }
                        SaveData.outputFileCSV(convertResult, info);
                    } catch (IOException e) {
                        System.out.println("[" + api.getName() + "] Error: " + e.getMessage());
                    }
                    break;
            }
        } catch (JsonProcessingException | APIConnectException e) {
            System.out.println("[" + api.getName() + "] Error: " + e.getMessage());
        } finally {
            ApiRegistry.markApiCompleted(api.getName());

        }
    }
}