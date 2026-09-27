package APITest;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.example.Main;
import org.example.convertor.Convertor;
import org.example.convertor.ConvertorResult;
import org.example.workWithAPI.APIConnectException;
import org.example.workWithAPI.ApiRegistry;
import org.example.workWithAPI.ClientAPI;
import org.example.workWithAPI.ResultAPI;
import org.example.workWithAPI.workWithAPIThread.APIThread;
import org.example.workWithData.ProcessingData;
import org.example.workWithData.SaveData;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.List;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class APIThreadTest {

    @Mock
    private ClientAPI mockApi;

    @Mock
    private ResultAPI mockResult;

    private APIThread apiThread;
    private PrintStream originalOut;
    private ByteArrayOutputStream outputStream;
    private Main.Info realInfo;

    @BeforeEach
    void setUp() {
        realInfo = new Main.Info();
        apiThread = new APIThread(mockApi, realInfo);

        originalOut = System.out;
        outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));
    }

    @AfterEach
    void tearDown() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("Успешное выполнение запроса с выводом в JSON формате")
    void runShouldSaveJsonFile_whenOutTypeIsJsonAndRequestSuccess() throws Exception {
        realInfo.outType = "json";
        lenient().when(mockApi.getName()).thenReturn("TestAPI");
        when(mockApi.getRequest()).thenReturn(mockResult);

        ObjectNode mockJsonNode = new ObjectMapper().createObjectNode();
        mockJsonNode.put("test", "data");

        try (MockedStatic<ProcessingData> processingDataMock = mockStatic(ProcessingData.class);
             MockedStatic<SaveData> saveDataMock = mockStatic(SaveData.class);
             MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {

            processingDataMock.when(() -> ProcessingData.processingDataToJSON(mockResult))
                    .thenReturn(mockJsonNode);

            apiThread.run();

            processingDataMock.verify(() -> ProcessingData.processingDataToJSON(mockResult));
            saveDataMock.verify(() -> SaveData.outputFileJson(eq(mockJsonNode), eq(realInfo)));
            apiRegistryMock.verify(() -> ApiRegistry.markApiCompleted("TestAPI"), times(1));
        }
    }

    @Test
    @DisplayName("Успешное выполнение запроса с выводом в CSV формате")
    void runShouldSaveCsvFile_whenOutTypeIsCsvAndRequestSuccess() throws Exception {
        realInfo.outType = "csv";
        lenient().when(mockApi.getName()).thenReturn("TestAPI");
        when(mockApi.getRequest()).thenReturn(mockResult);

        ArrayNode mockJsonArray = new ObjectMapper().createArrayNode();
        ObjectNode record = mockJsonArray.addObject();
        record.put("id", "1");
        record.put("source", "TestAPI");
        record.put("timestamp", "2024-01-01T00:00:00Z");
        ObjectNode data = record.putObject("data");
        data.put("temp", "20");
        data.put("city", "Moscow");

        try (MockedStatic<ProcessingData> processingDataMock = mockStatic(ProcessingData.class);
             MockedStatic<Convertor> convertorMock = mockStatic(Convertor.class);
             MockedStatic<SaveData> saveDataMock = mockStatic(SaveData.class);
             MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {

            processingDataMock.when(() -> ProcessingData.processingDataToJSON(mockResult))
                    .thenReturn(mockJsonArray);

            List<String> allPaths = List.of("id", "source", "timestamp", "data.temp", "data.city");
            List<Map<String, String>> records = List.of(
                    Map.of("id", "1", "source", "TestAPI", "timestamp", "2024-01-01T00:00:00Z",
                            "data.temp", "20", "data.city", "Moscow")
            );
            ConvertorResult convertResult = new ConvertorResult(allPaths, records);

            convertorMock.when(() -> Convertor.convert(any(ArrayNode.class)))
                    .thenReturn(convertResult);

            apiThread.run();

            processingDataMock.verify(() -> ProcessingData.processingDataToJSON(mockResult));
            convertorMock.verify(() -> Convertor.convert(any(ArrayNode.class)));
            saveDataMock.verify(() -> SaveData.outputFileCSV(any(ConvertorResult.class), eq(realInfo)));
            apiRegistryMock.verify(() -> ApiRegistry.markApiCompleted("TestAPI"), times(1));
        }
    }

    @Test
    @DisplayName("Обработка ошибки при преобразовании данных в JSON")
    void runShouldPrintErrorMessage_whenProcessingDataThrowsJsonException() throws Exception {
        realInfo.outType = "json";
        lenient().when(mockApi.getName()).thenReturn("TestAPI");
        when(mockApi.getRequest()).thenReturn(mockResult);

        try (MockedStatic<ProcessingData> processingDataMock = mockStatic(ProcessingData.class);
             MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {

            processingDataMock.when(() -> ProcessingData.processingDataToJSON(mockResult))
                    .thenThrow(new com.fasterxml.jackson.core.JsonProcessingException("JSON error") {});

            apiThread.run();

            processingDataMock.verify(() -> ProcessingData.processingDataToJSON(mockResult));

            String output = outputStream.toString();
            assertTrue(output.contains("[TestAPI] Error: JSON error"));
            apiRegistryMock.verify(() -> ApiRegistry.markApiCompleted("TestAPI"), times(1));
        }
    }

    @Test
    @DisplayName("Обработка ошибки подключения к API")
    void runShouldPrintApiConnectErrorMessage_whenGetRequestThrowsException() throws Exception {
        realInfo.outType = "json";
        when(mockApi.getName()).thenReturn("TestAPI");
        when(mockApi.getRequest()).thenThrow(new APIConnectException("API connection error"));
        outputStream.reset();

        try (MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {
            apiThread.run();

            String output = outputStream.toString();
            assertTrue(output.contains("[TestAPI] Error: APIConnectException [API connection error]"));
            apiRegistryMock.verify(() -> ApiRegistry.markApiCompleted("TestAPI"), times(1));
        }
    }

    @Test
    @DisplayName("Обработка случая когда результат запроса равен null")
    void runShouldPrintNoAnswerMessage_whenProcessingDataReturnsNull() throws Exception {
        realInfo.outType = "json";
        lenient().when(mockApi.getName()).thenReturn("TestAPI");
        when(mockApi.getRequest()).thenReturn(mockResult);

        try (MockedStatic<ProcessingData> processingDataMock = mockStatic(ProcessingData.class);
             MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {

            processingDataMock.when(() -> ProcessingData.processingDataToJSON(mockResult))
                    .thenReturn(null);

            apiThread.run();

            processingDataMock.verify(() -> ProcessingData.processingDataToJSON(mockResult));

            String output = outputStream.toString();
            assertTrue(output.contains("There isn't answer to request"));
            apiRegistryMock.verify(() -> ApiRegistry.markApiCompleted("TestAPI"), times(1));
        }
    }

    @Test
    @DisplayName("Обработка ошибки при конвертации JSON в CSV")
    void runShouldPrintConversionErrorMessage_whenConvertorThrowsIOException() throws Exception {
        realInfo.outType = "csv";
        lenient().when(mockApi.getName()).thenReturn("TestAPI");
        when(mockApi.getRequest()).thenReturn(mockResult);

        ArrayNode mockJsonArray = new ObjectMapper().createArrayNode();

        try (MockedStatic<ProcessingData> processingDataMock = mockStatic(ProcessingData.class);
             MockedStatic<Convertor> convertorMock = mockStatic(Convertor.class);
             MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {

            processingDataMock.when(() -> ProcessingData.processingDataToJSON(mockResult))
                    .thenReturn(mockJsonArray);
            convertorMock.when(() -> Convertor.convert(any(ArrayNode.class)))
                    .thenThrow(new IOException("Conversion error"));

            apiThread.run();

            processingDataMock.verify(() -> ProcessingData.processingDataToJSON(mockResult));
            convertorMock.verify(() -> Convertor.convert(any(ArrayNode.class)));

            String output = outputStream.toString();
            assertTrue(output.contains("[TestAPI] Error: Conversion error"));
            apiRegistryMock.verify(() -> ApiRegistry.markApiCompleted("TestAPI"), times(1));
        }
    }

    @Test
    @DisplayName("Обработка пустого результата конвертации CSV")
    void runShouldPrintNoConvertResultMessage_whenConvertResultIsEmpty() throws Exception {
        realInfo.outType = "csv";
        lenient().when(mockApi.getName()).thenReturn("TestAPI");
        when(mockApi.getRequest()).thenReturn(mockResult);

        ArrayNode mockJsonArray = new ObjectMapper().createArrayNode();
        ConvertorResult emptyConvertResult = new ConvertorResult(List.of(), List.of());

        try (MockedStatic<ProcessingData> processingDataMock = mockStatic(ProcessingData.class);
             MockedStatic<Convertor> convertorMock = mockStatic(Convertor.class);
             MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {

            processingDataMock.when(() -> ProcessingData.processingDataToJSON(mockResult))
                    .thenReturn(mockJsonArray);
            convertorMock.when(() -> Convertor.convert(any(ArrayNode.class)))
                    .thenReturn(emptyConvertResult);

            apiThread.run();

            processingDataMock.verify(() -> ProcessingData.processingDataToJSON(mockResult));
            convertorMock.verify(() -> Convertor.convert(any(ArrayNode.class)));

            String output = outputStream.toString();
            assertTrue(output.contains("There isn't convert result"));
            apiRegistryMock.verify(() -> ApiRegistry.markApiCompleted("TestAPI"), times(1));
        }
    }

    @Test
    @DisplayName("Проверка работы с разными именами API (Weather)")
    void runShouldWorkWithWeatherApiName_whenApiNameIsWeather() throws Exception {
        realInfo.outType = "json";
        lenient().when(mockApi.getName()).thenReturn("Weather");
        when(mockApi.getRequest()).thenReturn(mockResult);

        ObjectNode mockJsonNode = new ObjectMapper().createObjectNode();
        mockJsonNode.put("weather", "data");

        try (MockedStatic<ProcessingData> processingDataMock = mockStatic(ProcessingData.class);
             MockedStatic<SaveData> saveDataMock = mockStatic(SaveData.class);
             MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {

            processingDataMock.when(() -> ProcessingData.processingDataToJSON(mockResult))
                    .thenReturn(mockJsonNode);

            apiThread.run();

            processingDataMock.verify(() -> ProcessingData.processingDataToJSON(mockResult));
            saveDataMock.verify(() -> SaveData.outputFileJson(eq(mockJsonNode), eq(realInfo)));
            apiRegistryMock.verify(() -> ApiRegistry.markApiCompleted("Weather"), times(1));
        }
    }
}