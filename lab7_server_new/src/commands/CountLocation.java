package commands;

import managers.CollectionManager;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

public class CountLocation extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public CountLocation(Console console, CollectionManager collectionManager) {
        super("count_greater_than_location location", "вывести количество элементов, значение поля location которых больше заданного");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        if (argument.isEmpty()) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        }

        try {
            int locationValue = Integer.parseInt(argument.trim());
            long count = collectionManager.getCollection().values().stream()
                    .filter(person -> (person.getLocation().getX().intValue() + person.getLocation().getY()) > locationValue)
                    .count();

            return new ExecutionResponse("Количество элементов: " + count);
        } catch (NumberFormatException e) {
            return new ExecutionResponse(false, "Значение location должно быть числом");
        }
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            if (command.getArgs().length < 1) {
                return new NetworkResponse(400, "Недостаточно аргументов");
            }

            int locationValue = (int) command.getArgs()[0];
            long count = collectionManager.getCollection().values().stream()
                    .filter(person -> (person.getLocation().getX().intValue() + person.getLocation().getY()) > locationValue)
                    .count();

            return new NetworkResponse(200, "Количество элементов: " + count);
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при подсчете элементов");
        }
    }
}