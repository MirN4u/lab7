package utility;

/**
 * Класс для вывода успешности выполнения команд
 *
 * @author Miroslav
 * @version 1.0
 */

public class ExecutionResponse {
    private boolean exitCode;
    private String massage;
    private String responseObj;

    public ExecutionResponse(boolean code, String s) {
        exitCode = code;
        massage = s;
    }

    public ExecutionResponse(String s) {
        this(true, s);
    }

    public boolean getExitCode() {
        return exitCode;
    }

    public String getMassage() {
        return massage;
    }

    public String toString() {
        return String.valueOf(exitCode) + ";" + massage + ";" +
                (responseObj == null ? "null" : responseObj.toString());
    }
}