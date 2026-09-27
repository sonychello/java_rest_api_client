import org.example.Main;
import org.example.workWithData.InOutConsole;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.io.TempDir;

import static org.junit.jupiter.api.Assertions.*;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

class InOutConsoleTest {
    private InputStream originalIn;
    private PrintStream originalOut;
    private ByteArrayOutputStream outContent;

    @BeforeEach
    void saveSystemStreams() {
        originalIn = System.in;
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
        InOutConsole.resetScanner();
    }

    @AfterEach
    void restoreSystemStreams() {
        System.setIn(originalIn);
        System.setOut(originalOut);
    }


    //Tests for isStartRequests()

    @Test
    @DisplayName("isStartRequests(): Успешное считывание yes")
    void isStartRequestsShouldReturnTrue_when_yes() {
        String input = "yes\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        boolean result = InOutConsole.isStartRequests();
        assertTrue(result);
        assertTrue(outContent.toString().contains("Start work APIs"));
    }

    @Test
    @DisplayName("isStartRequests(): Успешное считывание No")
    void isStartRequestsShouldReturnFalse_when_No() {
        String input = "No\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        boolean result = InOutConsole.isStartRequests();
        assertFalse(result);
    }

    @Test
    @DisplayName("isStartRequests(): Некорректный ввод, затем Yes")
    void isStartRequestsShouldRetryOnIncorrectInput() {
        String input = "invalid\nyes\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        boolean result = InOutConsole.isStartRequests();
        assertTrue(result);
        assertTrue(outContent.toString().contains("Incorrect answer."));
        assertTrue(outContent.toString().contains("Start work APIs"));
    }


    //Tests for readConsole()

    @Test
    @DisplayName("readConsole() должен корректно считать ответы пользователя")
    void readConsoleShouldCorrectlyReadDataFromUser() {
        String input = "1\n100\nWeather Books Crypt\njson\nRewrite\ntest\nOne API\nCrypt\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        Main.Info testInfo = new Main.Info();
        InOutConsole.readConsole(testInfo);

        assertEquals(1, testInfo.nTask);
        assertEquals(100, testInfo.interval);
        assertEquals(List.of("Weather", "Books", "Crypt"), testInfo.apis);
        assertEquals("json", testInfo.outType);
        assertEquals("rw", testInfo.fileMode);
        assertEquals("test", testInfo.filename);
        assertEquals("Crypt", testInfo.outAPI);
    }

    @Test
    @DisplayName("readConsole() - проверка режима Create new")
    void readConsoleShouldHandleCreateNewMode() {
        String input = "2\n500\nWeather\ncsv\nCreate new\nAll API\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        Main.Info testInfo = new Main.Info();
        InOutConsole.readConsole(testInfo);

        assertEquals(2, testInfo.nTask);
        assertEquals(500, testInfo.interval);
        assertEquals(List.of("Weather"), testInfo.apis);
        assertEquals("csv", testInfo.outType);
        assertEquals("cr", testInfo.fileMode);
        assertEquals("src/test/testOut", testInfo.filename);
        assertEquals("all", testInfo.outAPI);
    }

    @Test
    @DisplayName("readConsole() - проверка выбора All API")
    void readConsoleShouldHandleAllApiOutput() {
        String input = "3\n1000\nWeather Books\njson\nRewrite\ntest\nAll API\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        Main.Info testInfo = new Main.Info();
        InOutConsole.readConsole(testInfo);

        assertEquals(3, testInfo.nTask);
        assertEquals(1000, testInfo.interval);
        assertEquals(List.of("Weather", "Books"), testInfo.apis);
        assertEquals("json", testInfo.outType);
        assertEquals("rw", testInfo.fileMode);
        assertEquals("test", testInfo.filename);
        assertEquals("all", testInfo.outAPI);
    }


    //Tests for readWriteLine()

    @Test
    @DisplayName("readWriteLine() выводит сообщение и ждёт ввода при isQuestion = true")
    void readWriteLineShouldPrintPromptAndWait() {
        String input = "test input\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        String prompt = "Enter something:";

        InOutConsole.readWriteLine(prompt, true);

        assertTrue(outContent.toString().contains(prompt));
    }

    @Test
    @DisplayName("readWriteLine() выводит сообщение без ожидания ввода при isQuestion = false")
    void readWriteLineShouldPrintPromptOnly() {
        String prompt = "Just a message";

        InOutConsole.readWriteLine(prompt, false);

        assertTrue(outContent.toString().contains(prompt));
    }


    //Tests for isExit()

    @Test
    @DisplayName("isExit() возвращает true при вводе 0")
    void isExitShouldReturnTrue_whenUserEnter0() {
        String input = "0\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        boolean result = InOutConsole.isExit();
        assertTrue(result);
    }

    @Test
    @DisplayName("isExit() возвращает false при вводе 1")
    void isExitShouldReturnFalse_whenUserEnter1() {
        String input = "1\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        boolean result = InOutConsole.isExit();
        assertFalse(result);
    }

    @Test
    @DisplayName("isExit() повторяет запрос при некорректном вводе")
    void isExitShouldRetryOnInvalidInput() {
        String input = "abc\n-5\n0\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        boolean result = InOutConsole.isExit();
        assertTrue(result);

        String output = outContent.toString();
        assertTrue(output.contains("Incorrect answer. Enter answer again:"));
        long occurrences = output.split("Incorrect answer\\. Enter answer again:", -1).length - 1;
        assertEquals(2, occurrences);
    }


    //Tests for outputJSONToConsole()

    @TempDir
    Path tempDir;

    @Test
    @DisplayName("outputJSONToConsole() выводит весь JSON при outAPI = all")
    void outputJSONToConsoleShouldPrintAll_whenOutApiIsAll() throws IOException {
        Path jsonFile = tempDir.resolve("output.json");
        String filename = jsonFile.toString().replace(".json", "");
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.outAPI = "all";

        String jsonContent = """
                [
                    {"id": 1, "source": "Weather", "data": {"temp": 20}},
                    {"id": 2, "source": "Books", "data": {"title": "Hobbit"}}
                ]
                """;
        Files.writeString(jsonFile, jsonContent);

        InOutConsole.outputJSONToConsole(testInfo);

        String output = outContent.toString();
        assertTrue(output.contains("Weather"));
        assertTrue(output.contains("Books"));
        assertTrue(output.contains("Hobbit"));
    }

    @Test
    @DisplayName("outputJSONToConsole() выводит только выбранный API")
    void outputJSONToConsoleShouldPrintOnlySelected() throws IOException {
        Path jsonFile = tempDir.resolve("output.json");
        String filename = jsonFile.toString().replace(".json", "");
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.outAPI = "Weather";

        String jsonContent = """
                [
                    {"id": 1, "source": "Weather", "data": {"temp": 20}},
                    {"id": 2, "source": "Books", "data": {"title": "Hobbit"}}
                ]
                """;
        Files.writeString(jsonFile, jsonContent);

        InOutConsole.outputJSONToConsole(testInfo);

        String output = outContent.toString();
        assertTrue(output.contains("Weather"));
        assertFalse(output.contains("Books"));
    }

    @Test
    @DisplayName("outputJSONToConsole() выводит сообщение об ошибке при отсутствии файла")
    void outputJSONToConsoleShouldPrintError_whenFileNotFound() {
        Main.Info testInfo = new Main.Info();
        testInfo.filename = "nonexistent";
        testInfo.outAPI = "all";

        InOutConsole.outputJSONToConsole(testInfo);

        assertTrue(outContent.toString().contains("Error in reading JSON file"));
    }


    //Tests for outputCSVToConsole()

    @Test
    @DisplayName("outputCSVToConsole() выводит весь CSV при outAPI = all")
    void outputCSVToConsoleShouldPrintAll_whenOutApiIsAll() throws IOException {
        Path csvFile = tempDir.resolve("output.csv");
        String filename = csvFile.toString().replace(".csv", "");
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.outAPI = "all";

        String csvContent = """
                id,source,data.temp
                1,Weather,20
                2,Books,22
                """;
        Files.writeString(csvFile, csvContent);

        InOutConsole.outputCSVToConsole(testInfo);

        String output = outContent.toString();
        assertTrue(output.contains("id,source,data.temp"));
        assertTrue(output.contains("1,Weather,20"));
        assertTrue(output.contains("2,Books,22"));
    }

    @Test
    @DisplayName("outputCSVToConsole() выводит сообщение при отсутствии файла")
    void outputCSVToConsoleShouldPrintMessage_whenFileNotFound() {
        Main.Info testInfo = new Main.Info();
        testInfo.filename = "nonexistent";
        testInfo.outAPI = "all";

        InOutConsole.outputCSVToConsole(testInfo);

        assertTrue(outContent.toString().contains("File is not founded"));
    }

    @Test
    @DisplayName("outputCSVToConsole() выводит только записи выбранного API")
    void outputCSVToConsoleShouldPrintOnlySelected() throws IOException {
        Path csvFile = tempDir.resolve("output.csv");
        String filename = csvFile.toString().replace(".csv", "");
        Main.Info testInfo = new Main.Info();
        testInfo.filename = filename;
        testInfo.outAPI = "Weather";

        String csvContent = """
                id,source,data.temp
                1,Weather,20
                2,Weather,22
                3,Books,25
                4,AnimeNews,18
                """;
        Files.writeString(csvFile, csvContent);

        InOutConsole.outputCSVToConsole(testInfo);

        String output = outContent.toString();
        assertTrue(output.contains("id,source,data.temp"));
        assertTrue(output.contains("1,Weather,20"));
        assertTrue(output.contains("2,Weather,22"));
        assertFalse(output.contains("3,Books,25"));
        assertFalse(output.contains("4,AnimeNews,18"));
    }


    //Tests for requestUser()

    @Test
    @DisplayName("requestUser() возвращает число при корректном вводе")
    void requestUserShouldReturnNumber() {
        String input = "5\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        Integer result = InOutConsole.requestUser();
        assertEquals(5, result);
    }

    @Test
    @DisplayName("requestUser() возвращает -1 при некорректном вводе")
    void requestUserShouldReturnMinusOneOnInvalidInput() {
        String input = "abc\n";
        ByteArrayInputStream in = new ByteArrayInputStream(input.getBytes());
        System.setIn(in);
        InOutConsole.resetScanner();

        Integer result = InOutConsole.requestUser();
        assertEquals(-1, result);
        assertTrue(outContent.toString().contains("Incorrect enter"));
    }
}