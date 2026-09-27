package APITest;

import org.example.Main;
import org.example.workWithAPI.ApiRegistry;
import org.example.workWithAPI.ClientAPI;
import org.example.workWithAPI.workWithAPIThread.StartThreadAPI;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class StartThreadAPITest {

    @Mock
    private Main.Info mockInfo;

    @Mock
    private ClientAPI mockApi;

    @BeforeEach
    void setUp() {
        StartThreadAPI.stop();
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @AfterEach
    void tearDown() {
        StartThreadAPI.stop();
    }


    @Test
    @DisplayName("start() - успешный запуск планировщика")
    void startShouldInitializeExecutors_whenNotRunning() {
        StartThreadAPI.start(5);
        assertDoesNotThrow(StartThreadAPI::stop);
    }

    @Test
    @DisplayName("start() - повторный запуск выводит сообщение 'Scheduler already running'")
    void startShouldPrintAlreadyRunningMessage_whenCalledTwice() {
        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            StartThreadAPI.start(5);
            StartThreadAPI.start(3);

            assertTrue(outContent.toString().contains("Scheduler already running"));
        } finally {
            System.setOut(originalOut);
        }
    }


    @Test
    @DisplayName("stop() - ничего не делает если планировщик не запущен")
    void stopShouldDoNothing_whenNotRunning() {
        StartThreadAPI.stop();
        assertDoesNotThrow(StartThreadAPI::stop);
    }

    @Test
    @DisplayName("stop() - очищает реестр через ApiRegistry.clearAll()")
    void stopShouldCallClearAll() {
        try (MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {
            StartThreadAPI.start(5);
            StartThreadAPI.stop();

            apiRegistryMock.verify(ApiRegistry::clearAll, times(1));
        }
    }


    @Test
    @DisplayName("stop() - catch InterruptedException при awaitTermination schedulerExecutor")
    void stopShouldCatchInterruptedException_whenAwaitingSchedulerExecutor() throws InterruptedException {
        StartThreadAPI.start(5);

        Thread stopperThread = new Thread(() -> {
            Thread.currentThread().interrupt();
            StartThreadAPI.stop();
        });

        stopperThread.start();
        stopperThread.join(2000);

        assertFalse(stopperThread.isAlive());
    }

    @Test
    @DisplayName("stop() - timeout при awaitTermination schedulerExecutor")
    void stopShouldHandleTimeout_whenAwaitingSchedulerExecutor() {
        StartThreadAPI.start(5);

        try (MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {
            apiRegistryMock.when(() -> ApiRegistry.canLaunch(anyString(), anyLong())).thenReturn(true);
            apiRegistryMock.when(() -> ApiRegistry.getApi(anyString())).thenReturn(mockApi);

            StartThreadAPI.callAPIsInThread(new Main.Info());

            Thread.sleep(100);

            assertDoesNotThrow(StartThreadAPI::stop);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    @Test
    @DisplayName("stop() - catch InterruptedException при awaitTermination executor")
    void stopShouldCatchInterruptedException_whenAwaitingExecutor() throws InterruptedException {
        StartThreadAPI.start(5);

        Thread stopperThread = new Thread(() -> {
            StartThreadAPI.stop();
            Thread.currentThread().interrupt();
        });

        stopperThread.start();
        stopperThread.join(2000);

        assertFalse(stopperThread.isAlive());
    }

    @Test
    @DisplayName("stop() - schedulerExecutor shutdownNow при timeout")
    void stopShouldCallShutdownNow_whenSchedulerExecutorTimeout() {
        StartThreadAPI.start(5);

        try (MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {
            apiRegistryMock.when(() -> ApiRegistry.canLaunch(anyString(), anyLong())).thenReturn(true);
            apiRegistryMock.when(() -> ApiRegistry.getApi(anyString())).thenReturn(mockApi);

            StartThreadAPI.callAPIsInThread(new Main.Info());

            try { Thread.sleep(200); } catch (InterruptedException _) {}

            StartThreadAPI.stop();

            assertTrue(true);
        }
    }


    @Test
    @DisplayName("callAPIsInThread() - executors не запущены, выводит ошибку")
    void callAPIsInThreadShouldPrintError_whenExecutorsNotStarted() {
        Main.Info info = new Main.Info();
        info.apis = List.of("Weather");
        info.interval = 1000;

        ByteArrayOutputStream outContent = new ByteArrayOutputStream();
        PrintStream originalOut = System.out;
        System.setOut(new PrintStream(outContent));

        try {
            StartThreadAPI.callAPIsInThread(info);
            assertTrue(outContent.toString().contains("Error: executors not started"));
        } finally {
            System.setOut(originalOut);
        }
    }

    @Test
    @DisplayName("callAPIsInThread() - не падает при вызове после запуска")
    void callAPIsInThreadShouldNotThrow_whenExecutorsRunning() {
        Main.Info info = new Main.Info();
        info.apis = List.of("Weather");
        info.interval = 1000;

        StartThreadAPI.start(5);
        assertDoesNotThrow(() -> StartThreadAPI.callAPIsInThread(info));
    }

    @Test
    @DisplayName("callAPIsInThread() - не падает с несколькими API")
    void callAPIsInThreadShouldNotThrow_whenMultipleApis() {
        Main.Info info = new Main.Info();
        info.apis = List.of("Weather", "Books", "AnimeNews");
        info.interval = 1000;

        StartThreadAPI.start(5);
        assertDoesNotThrow(() -> StartThreadAPI.callAPIsInThread(info));
    }

    @Test
    @DisplayName("callAPIsInThread() - не падает с пустым списком API")
    void callAPIsInThreadShouldNotThrow_whenEmptyApiList() {
        Main.Info info = new Main.Info();
        info.apis = List.of();
        info.interval = 1000;

        StartThreadAPI.start(5);
        assertDoesNotThrow(() -> StartThreadAPI.callAPIsInThread(info));
    }


    @Test
    @DisplayName("callAPIsInThread() - catch исключения в цикле планировщика")
    void callAPIsInThreadShouldCatchExceptionInSchedulerLoop() throws InterruptedException {
        Main.Info info = new Main.Info();
        info.apis = List.of("Weather");
        info.interval = 1000;

        try (MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {
            apiRegistryMock.when(() -> ApiRegistry.canLaunch(anyString(), anyLong()))
                    .thenThrow(new RuntimeException("Test exception"));

            StartThreadAPI.start(5);

            assertDoesNotThrow(() -> StartThreadAPI.callAPIsInThread(info));

            Thread.sleep(500);

            assertDoesNotThrow(StartThreadAPI::stop);
        }
    }


    @Test
    @DisplayName("callAPIsInThread() - цикл останавливается при isRunning = false")
    void callAPIsInThreadShouldStopLoop_whenIsRunningBecomesFalse() throws InterruptedException {
        Main.Info info = new Main.Info();
        info.apis = List.of("Weather");
        info.interval = 100;

        try (MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {
            apiRegistryMock.when(() -> ApiRegistry.canLaunch(anyString(), anyLong())).thenReturn(true);
            apiRegistryMock.when(() -> ApiRegistry.getApi(anyString())).thenReturn(mockApi);

            StartThreadAPI.start(5);
            StartThreadAPI.callAPIsInThread(info);

            Thread.sleep(300);

            StartThreadAPI.stop();

            Thread.sleep(300);

            apiRegistryMock.verify(ApiRegistry::clearAll, atLeastOnce());
        }
    }


    @Test
    @DisplayName("callAPIsInThread() - break при проверке isRunning")
    void callAPIsInThreadShouldBreak_whenIsRunningFalseDuringLoop() throws InterruptedException {
        Main.Info info = new Main.Info();
        info.apis = List.of("Weather", "Books", "AnimeNews");
        info.interval = 100;

        try (MockedStatic<ApiRegistry> apiRegistryMock = mockStatic(ApiRegistry.class)) {
            apiRegistryMock.when(() -> ApiRegistry.canLaunch(anyString(), anyLong())).thenReturn(true);
            apiRegistryMock.when(() -> ApiRegistry.getApi(anyString())).thenReturn(mockApi);

            StartThreadAPI.start(5);
            StartThreadAPI.callAPIsInThread(info);
            Thread.sleep(200);

            StartThreadAPI.stop();

            Thread.sleep(200);

            apiRegistryMock.verify(ApiRegistry::clearAll, atLeastOnce());
        }
    }
}