package client;

import utility.NetworkCommand;
import utility.NetworkResponse;

import java.util.NoSuchElementException;
import java.util.Scanner;

public class ClientApp {
    private static final int SERVER_CHECK_INTERVAL = 3000;
    private static final int MAX_RETRIES = 2;
    private static String username = null;
    private static String password = null;

    public static void main(String[] args) {
        try {
            UDPClient client = new UDPClient("localhost", 6789);
            CommandParser parser = new CommandParser();
            Scanner scanner = new Scanner(System.in);
            int retryCount = 0;

            // Аутентификация перед началом работы
            if (!authenticate(client, parser, scanner)) {
                System.err.println("Не удалось пройти аутентификацию. Завершение работы.");
                return;
            }

            // Основной цикл выполнения команд
            while (true) {
                try {
                    if (!client.isServerAvailable()) {
                        System.err.println("Соединение с сервером потеряно. Попытка переподключения...");
                        if (!reconnect(client) || retryCount++ >= MAX_RETRIES) {
                            System.err.println("Не удалось восстановить соединение. Завершение работы.");
                            break;
                        }
                        continue;
                    }

                    retryCount = 0;

                    NetworkCommand command = parser.parseCommand();
                    if (command.getCommandName().equalsIgnoreCase("exit")) {
                        System.out.println("Завершение работы клиента...");
                        break;
                    }

                    NetworkResponse response = client.sendCommand(command);
                    ResponseHandler.handleResponse(response);

                    if (command.getCommandName().equalsIgnoreCase("_shutdown")) {
                        System.out.println("Сервер завершает работу...");
                        break;
                    }

                    if (response.getStatus() == 401) {
                        System.out.println("Требуется повторная аутентификация.");
                        if (!authenticate(client, parser, scanner)) {
                            System.err.println("Не удалось пройти аутентификацию. Завершение работы.");
                            break;
                        }
                    }

                } catch (NullPointerException | NoSuchElementException e) {
                    System.err.println("Ошибка: пустая строка");
                    break;
                } catch (Exception e) {
                    System.err.println("Ошибка: " + e.getMessage());

                    // Проверяем доступность сервера после ошибки
                    if (!client.isServerAvailable()) {
                        System.err.println("Сервер недоступен");
                        if (!reconnect(client) || retryCount++ >= MAX_RETRIES) {
                            System.err.println("Критическая ошибка. Завершение работы.");
                            break;
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("Фатальная ошибка: " + e.getMessage());
        }
    }

    private static boolean authenticate(UDPClient client, CommandParser parser, Scanner scanner) {
        int attempts = 0;
        while (attempts < 3) {
            try {
                System.out.println("=== Аутентификация ===");
                System.out.print("Введите логин: ");
                String inputUsername = scanner.nextLine().trim();
                System.out.print("Введите пароль: ");
                String inputPassword = scanner.nextLine().trim();
                NetworkCommand loginCommand = new NetworkCommand("login",
                        new Object[]{inputUsername, inputPassword}, inputUsername, inputPassword);
                NetworkResponse response = client.sendCommand(loginCommand);

                if (response.getStatus() == 200) {
                    username = inputUsername;
                    password = inputPassword;
                    parser.setCredentials(username, password);
                    System.out.println("Аутентификация успешна!");
                    return true;
                } else {
                    System.out.println("Ошибка аутентификации: " + response.getMessage());
                    System.out.println("Попробуйте зарегистрироваться или ввести другие данные.");

                    System.out.print("Хотите зарегистрироваться? (y/n): ");
                    String answer = scanner.nextLine().trim().toLowerCase();

                    if (answer.equals("y") || answer.equals("yes")) {
                        NetworkCommand registerCommand = new NetworkCommand("register",
                                new Object[]{inputUsername, inputPassword}, inputUsername, inputPassword);
                        NetworkResponse registerResponse = client.sendCommand(registerCommand);

                        if (registerResponse.getStatus() == 200) {
                            username = inputUsername;
                            password = inputPassword;
                            parser.setCredentials(username, password);
                            System.out.println("Регистрация успешна! Вы автоматически вошли в систему.");
                            return true;
                        } else {
                            System.out.println("Ошибка регистрации: " + registerResponse.getMessage());
                        }
                    }
                }

                attempts++;
                System.out.println("Попытка " + attempts + " из 3");

            } catch (Exception e) {
                System.err.println("Ошибка при аутентификации: " + e.getMessage());
                attempts++;
            }
        }
        return false;
    }

    private static boolean checkServerAvailable(UDPClient client) {
        if (!client.isServerAvailable()) {
            System.err.println("Сервер недоступен");
            return false;
        }
        System.out.println("Соединение с сервером установлено");
        return true;
    }

    private static boolean reconnect(UDPClient client) {
        try {
            Thread.sleep(SERVER_CHECK_INTERVAL);
            return client.checkConnection();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            return false;
        } catch (Exception e) {
            System.err.println("Ошибка переподключения: " + e.getMessage());
            return false;
        }
    }
}