package client;

import models.Ask;
import utility.NetworkCommand;
import utility.StandartConsole;

import java.util.Scanner;

public class CommandParser {
    private final Scanner scanner;
    private final StandartConsole console;
    private String username = null;
    private String password = null;
    private boolean isAuthenticated = false;

    public CommandParser() {
        this.scanner = new Scanner(System.in);
        this.console = new StandartConsole();
    }
    public void setCredentials(String username, String password) {
        this.username = username;
        this.password = password;
    }

    public void authenticate() throws Ask.AskBreak {
        while (!isAuthenticated) {
            System.out.print("Введите команду (register/login): ");
            String input = scanner.nextLine().trim();
            String[] parts = input.split(" ", 3);
            String commandName = parts[0].toLowerCase();

            try {
                switch (commandName) {
                    case "register":
                        if (parts.length < 3) throw new IllegalArgumentException("Требуется логин и пароль");
                        handleRegistration(parts[1], parts[2]);
                        break;
                    case "login":
                        if (parts.length < 3) throw new IllegalArgumentException("Требуется логин и пароль");
                        handleLogin(parts[1], parts[2]);
                        break;
                    case "exit":
                        throw new Ask.AskBreak();
                    default:
                        System.out.println("Неизвестная команда. Используйте register или login");
                }
            } catch (IllegalArgumentException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void handleRegistration(String username, String password) {
        NetworkCommand command = new NetworkCommand("register",
                new Object[]{username, password}, null, null);
        System.out.println("Регистрация отправлена на сервер");
        // После успешной регистрации можно автоматически войти
        this.username = username;
        this.password = password;
    }

    private void handleLogin(String username, String password) {
        NetworkCommand command = new NetworkCommand("login",
                new Object[]{username, password}, null, null);
        System.out.println("Авторизация отправлена на сервер");
        // После успешного входа сохраняем учетные данные
        this.username = username;
        this.password = password;
        this.isAuthenticated = true;
    }

    public NetworkCommand parseCommand() throws Ask.AskBreak {
        if (!isAuthenticated) {
            authenticate();
        }

        System.out.print("Введите команду: ");
        String input = scanner.nextLine().trim();
        String[] parts = input.split(" ", 2);
        String commandName = parts[0].toLowerCase();
        String argument = parts.length > 1 ? parts[1] : "";

        try {
            switch (commandName) {
                case "insert":
                    if (argument.isEmpty()) throw new IllegalArgumentException("Требуется ключ");
                    return new NetworkCommand("insert", new Object[]{
                            Long.parseLong(argument),
                            Ask.askPerson(console, 1L)
                    }, username, password);

                case "update":
                    if (argument.isEmpty()) throw new IllegalArgumentException("Требуется ID");
                    return new NetworkCommand("update", new Object[]{
                            Long.parseLong(argument),
                            Ask.askPerson(console, Long.parseLong(argument))
                    }, username, password);

                case "remove_key":
                    if (argument.isEmpty()) throw new IllegalArgumentException("Требуется ключ");
                    return new NetworkCommand("remove_key",
                            new Object[]{Long.parseLong(argument)}, username, password);

                case "remove_lower_key":
                    if (argument.isEmpty()) throw new IllegalArgumentException("Требуется ключ");
                    return new NetworkCommand("remove_lower_key",
                            new Object[]{Long.parseLong(argument)}, username, password);

                case "remove_greater":
                    if (argument.isEmpty()) throw new IllegalArgumentException("Требуется ID");
                    return new NetworkCommand("remove_greater",
                            new Object[]{Long.parseLong(argument)}, username, password);

                case "replace_if_greater":
                    if (argument.isEmpty()) throw new IllegalArgumentException("Требуется ключ");
                    return new NetworkCommand("replace_if_greater", new Object[]{
                            Long.parseLong(argument),
                            Ask.askPerson(console, 1L)
                    }, username, password);

                case "filter_contains_name":
                    if (argument.isEmpty()) throw new IllegalArgumentException("Требуется имя");
                    return new NetworkCommand("filter_contains_name",
                            new Object[]{argument}, username, password);

                case "filter_starts_with_name":
                    if (argument.isEmpty()) throw new IllegalArgumentException("Требуется подстрока");
                    return new NetworkCommand("filter_starts_with_name",
                            new Object[]{argument}, username, password);

                case "count_greater_than_location":
                    if (argument.isEmpty()) throw new IllegalArgumentException("Требуется значение");
                    return new NetworkCommand("count_greater_than_location",
                            new Object[]{Integer.parseInt(argument)}, username, password);

                case "execute_script":
                    if (argument.isEmpty()) throw new IllegalArgumentException("Требуется имя файла");
                    return new NetworkCommand("execute_script",
                            new Object[]{argument}, username, password);

                case "shutdown":
                    if (!argument.isEmpty()) {
                        throw new IllegalArgumentException("Команда shutdown не принимает аргументов");
                    }
                    return new NetworkCommand("_shutdown", new Object[0], username, password);

                case "show":
                case "clear":
                case "info":
                case "help":
                    return new NetworkCommand(commandName, new Object[0], username, password);

                case "exit":
                    return new NetworkCommand("exit", new Object[0], username, password);

                case "logout":
                    isAuthenticated = false;
                    username = null;
                    password = null;
                    System.out.println("Вы вышли из системы");
                    return parseCommand();

                default:
                    throw new IllegalArgumentException("Неизвестная команда: " + commandName);
            }
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("Некорректный числовой аргумент");
        } catch (Ask.AskBreak e) {
            throw e;
        }
    }

}