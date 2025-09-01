package client;

import utility.NetworkResponse;

public class ResponseHandler {
    public static void handleResponse(NetworkResponse response) {
        switch (response.getStatus()) {
            case 200:
                System.out.println("Успех: " + response.getMessage());
                break;
            case 201:
                System.out.println("Создано: " + response.getMessage());
                break;
            case 400:
                System.out.println("Ошибка запроса: " + response.getMessage());
                break;
            case 401:
                System.out.println("Ошибка аутентификации: " + response.getMessage());
                break;
            case 403:
                System.out.println("Доступ запрещен: " + response.getMessage());
                break;
            case 404:
                System.out.println("Не найдено: " + response.getMessage());
                break;
            case 409:
                System.out.println("Конфликт: " + response.getMessage());
                break;
            case 500:
                System.out.println("Ошибка сервера: " + response.getMessage());
                break;
            case 501:
                System.out.println("Не реализовано: " + response.getMessage());
                break;
            default:
                System.out.println("Неизвестный статус: " + response.getStatus() + " - " + response.getMessage());
        }
        if (response.getData() != null) {
            System.out.println("Данные: " + response.getData().toString());
        }
    }
}