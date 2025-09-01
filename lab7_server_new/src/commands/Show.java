package commands;

import managers.CollectionManager;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

public class Show extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public Show(Console console, CollectionManager collectionManager) {
        super("show", "вывести все элементы коллекции");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        if (!argument.isEmpty()) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        }
        return new ExecutionResponse(collectionManager.toStringWithCreators());
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            return new NetworkResponse(200, collectionManager.toStringWithCreators());
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при получении коллекции: " + e.getMessage());
        }
    }
}