import org.example.Main;
import org.example.workWithAPI.workWithAPIThread.StartThreadAPI;
import org.example.workWithData.InOutConsole;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

class MainTest {
    private PrintStream originalOut;
    private ByteArrayOutputStream outContent;
    Main.Info testInfo;

    @BeforeEach
    void setUp() {
        testInfo = new Main.Info();
        testInfo.apis = new ArrayList<>();
        testInfo.nTask = 2;
        testInfo.interval = 100;
        testInfo.sessionTimeout = 1000;
        testInfo.filename = "testOut";
        testInfo.fileMode = "cr";
        testInfo.outAPI = "all";
        testInfo.outType = "json";
        originalOut = System.out;
        outContent = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outContent));
    }

    @AfterEach
    void restoreOutStream() {
        System.setOut(originalOut);
    }

    @Test
    @DisplayName("automaticMode() вызывает методы StartThreadAPI")
    void automaticModeShouldCallMethodsFromStartThreadAPI() {
        testInfo.apis.add("Weather");

        try (MockedStatic<StartThreadAPI> mockedThread = mockStatic(StartThreadAPI.class)) {

            Main.automaticMode(testInfo);

            mockedThread.verify(() -> StartThreadAPI.start(testInfo.nTask), times(1));
            mockedThread.verify(() -> StartThreadAPI.callAPIsInThread(testInfo), times(1));
            mockedThread.verify(StartThreadAPI::stop, times(1));

            assertTrue(outContent.toString().contains("App start automatic mode of work......"));
        }
    }

    @Test
    @DisplayName("interactiveMode() вызывает методы StartThreadAPI и InOutConsole")
    void interactiveModeShouldCallRequiredMethods() {
        testInfo.apis.add("Weather");

        try (MockedStatic<StartThreadAPI> mockedThread = mockStatic(StartThreadAPI.class);
             MockedStatic<InOutConsole> mockedConsole = mockStatic(InOutConsole.class)) {

            mockedConsole.when(InOutConsole::isExit).thenReturn(false, true);
            mockedConsole.when(InOutConsole::requestUser).thenReturn(2);
            mockedConsole.when(InOutConsole::isStartRequests).thenReturn(true);
            mockedConsole.when(() -> InOutConsole.readConsole(any())).thenAnswer(_ -> null);

            Main.interactiveMode(testInfo);

            mockedConsole.verify(() -> InOutConsole.readConsole(testInfo), atLeastOnce());
            mockedConsole.verify(InOutConsole::isExit, atLeastOnce());
            mockedConsole.verify(InOutConsole::isStartRequests, atLeastOnce());
            mockedThread.verify(() -> StartThreadAPI.start(testInfo.nTask), atLeastOnce());
            mockedThread.verify(() -> StartThreadAPI.callAPIsInThread(testInfo), atLeastOnce());
            mockedThread.verify(StartThreadAPI::stop, atLeastOnce());
        }
    }

    @Test
    @DisplayName("interactiveMode() завершается сразу, если isExit() вернул true")
    void interactiveModeShouldExitImmediately_whenIsExitReturnsTrue() {
        try (MockedStatic<StartThreadAPI> mockedThread = mockStatic(StartThreadAPI.class);
             MockedStatic<InOutConsole> mockedConsole = mockStatic(InOutConsole.class)) {

            mockedConsole.when(InOutConsole::isExit).thenReturn(true);

            Main.interactiveMode(testInfo);

            mockedConsole.verify(InOutConsole::isExit, times(1));
            mockedConsole.verify(() -> InOutConsole.readConsole(any()), never());
            mockedConsole.verify(InOutConsole::isStartRequests, never());
            mockedThread.verify(() -> StartThreadAPI.start(anyInt()), never());
        }
    }

    @Test
    @DisplayName("interactiveMode() завершается, если isStartRequests() вернул false")
    void interactiveModeShouldExit_whenIsStartRequestsReturnsFalse() {
        try (MockedStatic<StartThreadAPI> mockedThread = mockStatic(StartThreadAPI.class);
             MockedStatic<InOutConsole> mockedConsole = mockStatic(InOutConsole.class)) {

            mockedConsole.when(InOutConsole::isExit).thenReturn(false);
            mockedConsole.when(InOutConsole::isStartRequests).thenReturn(false);
            mockedConsole.when(() -> InOutConsole.readConsole(any())).thenAnswer(_ -> null);

            Main.interactiveMode(testInfo);

            mockedConsole.verify(InOutConsole::isExit, times(1));
            mockedConsole.verify(() -> InOutConsole.readConsole(testInfo), times(1));
            mockedConsole.verify(InOutConsole::isStartRequests, times(1));
            mockedThread.verify(() -> StartThreadAPI.start(anyInt()), never());
        }
    }

    @Test
    @DisplayName("interactiveMode() обрабатывает команду 1 (перезапуск API)")
    void interactiveModeShouldHandleCommandOne() {
        testInfo.apis.add("Weather");

        try (MockedStatic<StartThreadAPI> mockedThread = mockStatic(StartThreadAPI.class);
             MockedStatic<InOutConsole> mockedConsole = mockStatic(InOutConsole.class)) {

            mockedConsole.when(InOutConsole::isExit).thenReturn(false, true);
            mockedConsole.when(InOutConsole::isStartRequests).thenReturn(true);
            mockedConsole.when(InOutConsole::requestUser).thenReturn(1, 0);
            mockedConsole.when(() -> InOutConsole.readConsole(any())).thenAnswer(_ -> null);

            testInfo.outType = "json";

            Main.interactiveMode(testInfo);

            mockedThread.verify(() -> StartThreadAPI.start(testInfo.nTask), atLeastOnce());
            mockedConsole.verify(() -> InOutConsole.outputJSONToConsole(testInfo), times(2));
        }
    }

    @Test
    @DisplayName("interactiveMode() обрабатывает команду 2 (повторный опрос)")
    void interactiveModeShouldHandleCommandTwo() {
        testInfo.apis.add("Weather");

        try (MockedStatic<StartThreadAPI> mockedThread = mockStatic(StartThreadAPI.class);
             MockedStatic<InOutConsole> mockedConsole = mockStatic(InOutConsole.class)) {

            mockedConsole.when(InOutConsole::isExit).thenReturn(false, false, true);
            mockedConsole.when(InOutConsole::isStartRequests).thenReturn(true);
            mockedConsole.when(InOutConsole::requestUser).thenReturn(2);
            mockedConsole.when(() -> InOutConsole.readConsole(any())).thenAnswer(_ -> null);

            Main.interactiveMode(testInfo);

            mockedConsole.verify(() -> InOutConsole.readConsole(testInfo), times(2));
            mockedThread.verify(() -> StartThreadAPI.start(testInfo.nTask), atLeastOnce());
        }
    }


    //Tests for parseArgs()

    @Test
    @DisplayName("parseArgs() успешно парсит auto режим")
    void parseArgsShouldReturnTrue_whenArgsIsCorrect() {
        String[] args = {"auto", "3", "500", "15000", "Weather", "Books", "json"};

        boolean result = Main.parseArgs(args, testInfo);

        assertTrue(result);
        assertEquals("auto", testInfo.mode);
        assertEquals(3, testInfo.nTask);
        assertEquals(500, testInfo.interval);
        assertEquals(15000, testInfo.sessionTimeout);
        assertEquals(List.of("Weather", "Books"), testInfo.apis);
        assertEquals("json", testInfo.outType);
    }

    @Test
    @DisplayName("parseArgs() успешно парсит inter режим")
    void parseArgsShouldReturnTrue_whenInterMode() {
        String[] args = {"inter"};

        boolean result = Main.parseArgs(args, testInfo);

        assertTrue(result);
        assertEquals("inter", testInfo.mode);
    }

    @Test
    @DisplayName("parseArgs() возвращает false при пустом массиве аргументов")
    void parseArgsShouldReturnFalse_whenArgsIsEmpty() {
        String[] args = {};

        boolean result = Main.parseArgs(args, testInfo);

        assertFalse(result);
        assertTrue(outContent.toString().contains("No arguments provided"));
    }

    @Test
    @DisplayName("parseArgs() возвращает false при неизвестном API")
    void parseArgsShouldReturnFalse_whenApiIsUnknown() {
        String[] args = {"auto", "3", "500", "15000", "UnknownAPI", "json"};

        boolean result = Main.parseArgs(args, testInfo);

        assertFalse(result);
        assertTrue(outContent.toString().contains("Unknown API: UnknownAPI"));
    }

    @Test
    @DisplayName("parseArgs() возвращает false при неверном outType")
    void parseArgsShouldReturnFalse_whenOutTypeIsInvalid() {
        String[] args = {"auto", "3", "500", "15000", "Weather", "xml"};

        boolean result = Main.parseArgs(args, testInfo);

        assertFalse(result);
        assertTrue(outContent.toString().contains("Output type should be 'json' or 'csv'"));
    }

    @Test
    @DisplayName("parseArgs() возвращает false при nTask вне диапазона")
    void parseArgsShouldReturnFalse_whenNTaskOutOfRange() {
        String[] args = {"auto", "6", "500", "15000", "Weather", "json"};

        boolean result = Main.parseArgs(args, testInfo);

        assertFalse(result);
        assertTrue(outContent.toString().contains("Number of tasks should be >0 and <=5"));
    }

    @Test
    @DisplayName("parseArgs() возвращает false при interval вне диапазона")
    void parseArgsShouldReturnFalse_whenIntervalOutOfRange() {
        String[] args = {"auto", "3", "99", "15000", "Weather", "json"};

        boolean result = Main.parseArgs(args, testInfo);

        assertFalse(result);
        assertTrue(outContent.toString().contains("Interval should be >=100 and <=10000"));
    }

    @Test
    @DisplayName("parseArgs() возвращает false при sessionTimeout <= interval")
    void parseArgsShouldReturnFalse_whenSessionTimeoutLessThanInterval() {
        String[] args = {"auto", "3", "500", "400", "Weather", "json"};

        boolean result = Main.parseArgs(args, testInfo);

        assertFalse(result);
        assertTrue(outContent.toString().contains("Session time should be > interval"));
    }

    @Test
    @DisplayName("parseArgs() возвращает false при пустом списке API")
    void parseArgsShouldReturnFalse_whenNoApisProvided() {
        String[] args = {"auto", "3", "500", "15000", "json"};

        boolean result = Main.parseArgs(args, testInfo);

        assertFalse(result);
        assertTrue(outContent.toString().contains("At least one API must be specified"));
    }
}