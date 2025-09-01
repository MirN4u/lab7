package commands;

import managers.CollectionManager;
import models.Ask;
import models.Person;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

import java.time.ZonedDateTime;

public class Update extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public Update(Console console, CollectionManager collectionManager) {
        super("update", "обновить значение элемента коллекции, id которого равен заданному");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        // Локальная реализация
        try {
            if (argument.isEmpty()) {
                return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
            }
            return new ExecutionResponse("Обновлено");
        } catch (Exception e) {
            return new ExecutionResponse(false, "Ошибка: " + e.getMessage());
        }
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            if (command.getArgs() == null || command.getArgs().length < 2) {
                return new NetworkResponse(400, "Необходимо указать ID и объект");
            }

            Long id = (Long) command.getArgs()[0];
            Person newPerson = (Person) command.getArgs()[1];
            String username = command.getUsername();

            Long key = collectionManager.getKeyByPersonId(id);
            if (key == null) {
                return new NetworkResponse(404, "Элемент с ID " + id + " не найден");
            }

            // Проверяем права доступа
            if (!collectionManager.hasAccess(key, username)) {
                return new NetworkResponse(403, "У вас нет прав для модификации этого объекта");
            }

            newPerson.setCreationDate(ZonedDateTime.now());
            newPerson.setID(id);
            if (!collectionManager.getDumpManager().updateObject(newPerson)) {
                return new NetworkResponse(500, "Ошибка обновления объекта в базе данных");
            }
            boolean result = collectionManager.update(key, newPerson, username);

            if (result) {
                return new NetworkResponse(200, "Элемент успешно обновлен");
            } else {
                return new NetworkResponse(500, "Ошибка при обновлении элемента в коллекции");
            }

        } catch (ClassCastException e) {
            return new NetworkResponse(400, "Неверный формат аргументов");
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при выполнении команды: " + e.getMessage());
        }
    }
}