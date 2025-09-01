package commands;

import managers.CollectionManager;
import models.Person;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

import java.time.ZonedDateTime;

public class Insert extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public Insert(Console console, CollectionManager collectionManager) {
        super("insert", "добавить новый элемент в коллекцию");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        // Локальная реализация
        try {
            if (argument.isEmpty()) {
                return new ExecutionResponse(false, "Требуется указать ключ");
            }
            Long key = Long.parseLong(argument.trim());
            return new ExecutionResponse("Объект добавлен");
        } catch (NumberFormatException e) {
            return new ExecutionResponse(false, "Ключ должен быть числом");
        }
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            Object[] args = command.getArgs();
            if (args == null || args.length < 2) {
                return new NetworkResponse(400, "Необходимо указать ключ и объект");
            }

            Long key = (Long) args[0];
            Person person = (Person) args[1];
            String username = command.getUsername();

            // Валидация объекта
            if (person == null) {
                return new NetworkResponse(400, "Объект не может быть null");
            }

            if (!person.validate()) {
                return new NetworkResponse(400, "Объект не прошел валидацию");
            }

            // Проверка ключа
            if (collectionManager.getCollection().containsKey(key)) {
                return new NetworkResponse(400, "Ключ " + key + " уже занят. Используйте другой ключ.");
            }

            person.setCreationDate(ZonedDateTime.now());

            boolean result = collectionManager.add(person, key, username);

            if (result) {
                return new NetworkResponse(200, "Объект успешно добавлен с ID: " + person.getId() + " и ключом: " + key);
            } else {
                return new NetworkResponse(400, "Ошибка добавления объекта. Возможно, объект уже существует или произошла ошибка базы данных.");
            }
        } catch (ClassCastException e) {
            return new NetworkResponse(400, "Неверный формат аргументов");
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при выполнении команды: " + e.getMessage());
        }
    }
}