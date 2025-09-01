package commands;

import managers.CollectionManager;
import models.Person;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

import java.util.List;
import java.util.stream.Collectors;

public class FilterContains extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public FilterContains(Console console, CollectionManager collectionManager) {
        super("filter_contains_name name", "вывести элементы, значение поля name которых содержит заданную подстроку");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        if (argument.isEmpty()) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        }

        String name = argument.trim();
        List<Person> filtered = collectionManager.getCollection().values().stream()
                .filter(person -> person.getName().contains(name))
                .collect(Collectors.toList());

        if (filtered.isEmpty()) {
            return new ExecutionResponse("Нет элементов, содержащих подстроку: " + name);
        } else {
            return new ExecutionResponse(filtered.toString());
        }
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            if (command.getArgs().length < 1) {
                return new NetworkResponse(400, "Недостаточно аргументов");
            }

            String name = (String) command.getArgs()[0];
            List<Person> filtered = collectionManager.getCollection().values().stream()
                    .filter(person -> person.getName().contains(name))
                    .collect(Collectors.toList());

            if (filtered.isEmpty()) {
                return new NetworkResponse(404, "Элементы не найдены");
            } else {
                return new NetworkResponse(200, filtered.toString());
            }
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при фильтрации");
        }
    }
}