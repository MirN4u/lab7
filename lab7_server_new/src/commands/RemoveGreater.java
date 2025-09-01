package commands;

import managers.CollectionManager;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

import java.util.ArrayList;
import java.util.List;

public class RemoveGreater extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public RemoveGreater(Console console, CollectionManager collectionManager) {
        super("remove_greater", "удалить из коллекции все элементы, превышающие заданный");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        if (argument.isEmpty()) {
            return new ExecutionResponse(false, "Требуется указать ID элемента\nИспользование: '" + getName() + "'");
        }

        try {
            long targetId = Long.parseLong(argument.trim());
            int initialSize = collectionManager.getCollection().size();

            collectionManager.getCollection().values().removeIf(person ->
                    person.getId() > targetId
            );

            int removedCount = initialSize - collectionManager.getCollection().size();
            collectionManager.update();

            return new ExecutionResponse("Удалено элементов: " + removedCount);
        } catch (NumberFormatException e) {
            return new ExecutionResponse(false, "ID должен быть числом");
        }
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            if (command.getArgs() == null || command.getArgs().length < 1) {
                return new NetworkResponse(400, "Необходимо указать ID");
            }

            long targetId = (Long) command.getArgs()[0];
            String username = command.getUsername();

            int removedCount = 0;

            List<Long> keysToRemove = new ArrayList<>();
            for (var entry : collectionManager.getCollection().entrySet()) {
                if (entry.getValue().getId() > targetId && collectionManager.hasAccess(entry.getKey(), username)) {
                    keysToRemove.add(entry.getKey());
                }
            }

            for (Long key : keysToRemove) {
                if (collectionManager.remove(key, username)) {
                    removedCount++;
                }
            }

            return new NetworkResponse(200, "Удалено элементов: " + removedCount);
        } catch (ClassCastException e) {
            return new NetworkResponse(400, "Неверный формат ID");
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при удалении элементов: " + e.getMessage());
        }
    }
}