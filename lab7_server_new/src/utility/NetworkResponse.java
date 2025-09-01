package utility;

import java.io.Serializable;

public class NetworkResponse implements Serializable {
    private static final long serialVersionUID = 1L;

    private int status;
    private String message;
    private Object data;

    public NetworkResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public NetworkResponse(int status, String message, Object data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // Геттеры и сеттеры
    public int getStatus() { return status; }
    public void setStatus(int status) { this.status = status; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
}