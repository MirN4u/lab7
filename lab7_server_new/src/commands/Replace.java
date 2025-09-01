package commands;

import managers.CollectionManager;
import models.Person;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

import java.time.ZonedDateTime;

public class Replace extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public Replace(Console console, CollectionManager collectionManager) {
        super("replace_if_greater", "заменить значение по ключу, если новое значение больше старого");
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
            return new ExecutionResponse("Заменено");
        } catch (Exception e) {
            return new ExecutionResponse(false, "Ошибка: " + e.getMessage());
        }
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            if (command.getArgs() == null || command.getArgs().length < 2) {
                return new NetworkResponse(400, "Необходимо указать ключ и объект");
            }

            Long key = (Long) command.getArgs()[0];
            Person newPerson = (Person) command.getArgs()[1];
            String username = command.getUsername();

            Person oldPerson = collectionManager.byId(key);
            if (oldPerson == null) {
                return new NetworkResponse(404, "Элемент с ключом " + key + " не найден");
            }

            // Проверяем права доступа
            if (!collectionManager.hasAccess(key, username)) {
                return new NetworkResponse(403, "У вас нет прав для модификации этого объекта");
            }

            if (newPerson == null || !newPerson.validate()) {
                return new NetworkResponse(400, "Невалидные данные Person");
            }

            newPerson.setCreationDate(ZonedDateTime.now());
            newPerson.setID(oldPerson.getId());

            double oldSum = oldPerson.getCoordinates().getX() + oldPerson.getLocation().getX();
            double newSum = newPerson.getCoordinates().getX() + newPerson.getLocation().getX();

            if (newSum > oldSum) {
                if (!collectionManager.getDumpManager().updateObject(newPerson)) {
                    return new NetworkResponse(500, "Ошибка обновления объекта в базе данных");
                }
                if (collectionManager.update(key, newPerson, username)) {
                    return new NetworkResponse(200, "Элемент успешно заменен");
                } else {
                    return new NetworkResponse(500, "Ошибка при замене элемента");
                }
            } else {
                return new NetworkResponse(400, "Новый элемент не больше старого");
            }
        } catch (ClassCastException e) {
            return new NetworkResponse(400, "Неверный формат аргументов");
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при замене элемента: " + e.getMessage());
        }
    }
}