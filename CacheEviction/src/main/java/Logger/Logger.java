package Logger;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;

public class Logger {
    private static final String LOG_FILE = "logger.txt";
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    private static void log(String level, String message, Throwable throwable) {
        String timestamp = DATE_FORMAT.format(new Date());
        StackTraceElement caller = Thread.currentThread().getStackTrace()[3]; // Captura a classe e método que chamou o log
        String logMessage = String.format("[%s] [%s] [%s.%s] %s", timestamp, level, caller.getClassName(), caller.getMethodName(), message);

        System.out.println(logMessage);

        try (PrintWriter out = new PrintWriter(new FileWriter(LOG_FILE, false))) {
            out.println(logMessage);
            if (throwable != null) {
                throwable.printStackTrace(out);
            }
        } catch (IOException e) {
            System.err.println("Erro ao escrever no arquivo de log: " + e.getMessage());
        }
    }

    public static void info(String message) {
        log("INFO", message, null);
    }

    public static void warning(String message) {
        log("WARNING", message, null);
    }

    public static void error(String message, Throwable throwable) {
        log("ERROR", message, throwable);
    }

    public static void request(String clientIp, String action, Object... params) {
        String paramString = params.length > 0 ? " Parâmetros: " + Arrays.toString(params) : "";
        log("REQUEST", "Cliente " + clientIp + " realizou ação: " + action + paramString, null);
    }

    public static void database(String action, Object... params) {
        String paramString = params.length > 0 ? " Parâmetros: " + Arrays.toString(params) : "";
        log("DATABASE", "Banco de dados - " + action + paramString, null);
    }

    public static void cache(String action, Object... params) {
        String paramString = params.length > 0 ? " Parâmetros: " + Arrays.toString(params) : "";
        log("CACHE", "Cache - " + action + paramString, null);
    }
}