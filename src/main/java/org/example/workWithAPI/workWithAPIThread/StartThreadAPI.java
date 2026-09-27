package org.example.workWithAPI.workWithAPIThread;

import org.example.Main;
import org.example.workWithAPI.ApiRegistry;
import org.example.workWithAPI.ClientAPI;

import java.util.concurrent.*;

public class StartThreadAPI {
    private static volatile boolean isRunning = false;

    private static ExecutorService executor;

    private static ExecutorService schedulerExecutor;

    public static synchronized void start(int nTask) {
        if (isRunning) {
            System.out.println("Scheduler already running");
            return;
        }

        executor = Executors.newFixedThreadPool(nTask);
        schedulerExecutor = Executors.newSingleThreadExecutor(r -> {
            Thread t = new Thread(r, "SchedulerLoop");
            t.setDaemon(true);
            return t;
        });

        isRunning = true;
        Runtime.getRuntime().addShutdownHook(new Thread(StartThreadAPI::stop));
    }

    public static synchronized void stop() {
        if (!isRunning) return;
        isRunning = false;

        if (schedulerExecutor != null) {
            schedulerExecutor.shutdown();
            try {
                if (!schedulerExecutor.awaitTermination(2, TimeUnit.SECONDS)) {
                    schedulerExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                schedulerExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            schedulerExecutor = null;
        }

        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(10, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
                Thread.currentThread().interrupt();
            }
            executor = null;
        }

        ApiRegistry.clearAll();
    }

    public static void callAPIsInThread(Main.Info info) {
        if (!isRunning || executor == null || schedulerExecutor == null) {
            System.out.println("Error: executors not started");
            return;
        }

        schedulerExecutor.submit(() -> {
            try {
                while (isRunning) {
                    for (String apiName : info.apis) {
                        if (!isRunning) break;

                        if (!ApiRegistry.canLaunch(apiName, info.interval)) {
                            continue;
                        }

                        ClientAPI api = ApiRegistry.getApi(apiName);
                        APIThread task = new APIThread(api, info);

                        ApiRegistry.markApiStarted(apiName);
                        executor.submit(task);
                    }
                }
            } catch (Exception e) {
                System.out.println("Scheduler loop error: " + e.getMessage());
            }
        });
    }
}