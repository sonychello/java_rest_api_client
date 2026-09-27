package org.example;

import org.example.workWithAPI.ApiRegistry;
import org.example.workWithAPI.workWithAPIThread.StartThreadAPI;
import org.example.workWithData.InOutConsole;

import java.util.ArrayList;
import java.util.List;

public class Main {
    public static class Info {
        public List<String> apis;
        public String outType;
        public String outAPI;
        public String filename;
        public String fileMode;
        public int interval;
        public int nTask;
        public String mode;
        public int sessionTimeout;
    }


    public static boolean parseArgs(String[] args, Info info) {
        if (args.length == 0) {
            System.out.println("No arguments provided");
            return false;
        }

        info.mode = args[0];

        switch (info.mode) {
            case "inter":
                return true;

            case "auto":
                if (args.length < 5) {
                    System.out.println("One or more args is not exist.");
                    return false;
                }

                try {
                    info.nTask = Integer.parseInt(args[1]);
                    info.interval = Integer.parseInt(args[2]);
                    info.sessionTimeout = Integer.parseInt(args[3]);
                } catch (NumberFormatException e) {
                    System.out.println(e.getMessage());
                    return false;
                }

                if (info.nTask > 5 || info.nTask < 1) {
                    System.out.println("Number of tasks should be >0 and <=5, got: " + info.nTask);
                    return false;
                }

                if (info.interval < 100 || info.interval > 10000) {
                    System.out.println("Interval should be >=100 and <=10000, got: " + info.interval);
                    return false;
                }

                if (info.sessionTimeout <= info.interval) {
                    System.out.println("Session time should be > interval (" + info.interval + "), got: " + info.sessionTimeout);
                    return false;
                }
                if (info.sessionTimeout > 30000) {
                    System.out.println("Session time should be <=30000, got: " + info.sessionTimeout);
                    return false;
                }

                info.apis = new ArrayList<>();
                for (int i = 4; i < args.length - 1; i++) {
                    String apiName = args[i];
                    if (!ApiRegistry.contains(apiName)) {
                        System.out.println("Unknown API: " + apiName + ". Available APIs: " + ApiRegistry.getApiNames());
                        return false;
                    }
                    info.apis.add(apiName);
                }

                if (info.apis.isEmpty()) {
                    System.out.println("At least one API must be specified");
                    return false;
                }

                info.outType = args[args.length - 1];
                if (!info.outType.equals("json") && !info.outType.equals("csv")) {
                    System.out.println("Output type should be 'json' or 'csv', got: " + info.outType);
                    return false;
                }

                info.fileMode = "cr";
                info.filename = "src/test/testOut";
                info.outAPI = "all";
                return true;

            default:
                System.out.println("Incorrect app mode: " + info.mode + ". Expected 'inter' or 'auto'");
                return false;
        }
    }


    public static void automaticMode(Info info) {
        System.out.println("App start automatic mode of work......");
        StartThreadAPI.start(info.nTask);

        StartThreadAPI.callAPIsInThread(info);

        System.out.println("Waiting " + info.sessionTimeout + "ms for tasks to complete...");

        try {
            Thread.sleep(info.sessionTimeout);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            System.out.println("Tasks interrupted");
        }

        StartThreadAPI.stop();
    }

    public static void interactiveMode(Info info) {
        boolean isWork = false;
        System.out.println("App start interactive mode of work......");

        while (isWork || !InOutConsole.isExit()) {
            if (!isWork) {
                InOutConsole.readConsole(info);
            }

            if (!isWork && !InOutConsole.isStartRequests()) {
                InOutConsole.readWriteLine("Завершение...", false);
                return;
            }
            StartThreadAPI.start(info.nTask);

            StartThreadAPI.callAPIsInThread(info);

            int command;
            command = InOutConsole.requestUser();
            StartThreadAPI.stop();
            isWork = false;
            System.out.println("APIs stoped");
            switch (info.outType) {
                case "json":
                    InOutConsole.outputJSONToConsole(info);
                    break;
                case "csv":
                    InOutConsole.outputCSVToConsole(info);
            }

            switch (command) {
                case 1:
                    if (InOutConsole.isStartRequests()) {
                        isWork = true;
                        continue;
                    }
                    break;
                case 2:
                    continue;
                default:
                    return;
            }
        }
    }

    public static void main(String[] args) {
        Info info = new Info();

        if (!parseArgs(args, info)) {
            return;
        }

        switch (info.mode) {
            case "inter":
                interactiveMode(info);
                break;
            case "auto":
                automaticMode(info);
                break;
        }
    }
}