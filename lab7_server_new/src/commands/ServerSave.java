package commands;

import managers.CollectionManager;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

public class ServerSave extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public ServerSave(Console console, CollectionManager collectionManager) {
        super("_save", "сохранить коллекцию в файл (серверная команда)");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        try {
            collectionManager.saveCollection();
            return new ExecutionResponse("Коллекция успешно сохранена");
        } catch (Exception e) {
            return new ExecutionResponse(false, "Ошибка при сохранении: " + e.getMessage());
        }
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            collectionManager.saveCollection();
            return new NetworkResponse(200, "Коллекция сохранена в файл");
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка сохранения: " + e.getMessage());
        }
    }
}
