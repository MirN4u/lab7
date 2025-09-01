package commands;

import managers.CollectionManager;
import models.Person;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Clear extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public Clear(Console console, CollectionManager collectionManager) {
        super("clear", "очистить коллекцию");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        String username = command.getUsername();
        int removedCount = 0;
        int failedCount = 0;
        Map<Long, Person> userObjects = collectionManager.getUserObjects(username);
        List<Long> keysToRemove = new ArrayList<>(userObjects.keySet());

        for (Long key : keysToRemove) {
            if (collectionManager.remove(key, username)) {
                removedCount++;
            } else {
                failedCount++;
            }
        }

        String message = "Удалено " + removedCount + " объектов пользователя " + username;
        if (failedCount > 0) {
            message += ". Не удалось удалить " + failedCount + " объектов";
        }

        return new NetworkResponse(200, message);
    }
}