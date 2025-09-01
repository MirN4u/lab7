package commands;

import managers.CollectionManager;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

import java.util.ArrayList;
import java.util.List;

public class RemoveLower extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public RemoveLower(Console console, CollectionManager collectionManager) {
        super("remove_lower_key", "удалить из коллекции все элементы, ключ которых меньше заданного");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        if (argument.isEmpty()) {
            return new ExecutionResponse(false, "Требуется указать ключ\nИспользование: '" + getName() + "'");
        }

        try {
            long targetKey = Long.parseLong(argument.trim());
            int initialSize = collectionManager.getCollection().size();

            collectionManager.getCollection().entrySet().removeIf(entry ->
                    entry.getKey() < targetKey
            );

            int removedCount = initialSize - collectionManager.getCollection().size();
            collectionManager.update();

            return new ExecutionResponse("Удалено элементов: " + removedCount);
        } catch (NumberFormatException e) {
            return new ExecutionResponse(false, "Ключ должен быть числом");
        }
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            if (command.getArgs() == null || command.getArgs().length < 1) {
                return new NetworkResponse(400, "Необходимо указать ключ");
            }

            long targetKey = (Long) command.getArgs()[0];
            String username = command.getUsername();

            int removedCount = 0;

            // Создаем копию списка ключей для безопасного удаления
            List<Long> keysToRemove = new ArrayList<>();
            for (var entry : collectionManager.getCollection().entrySet()) {
                if (entry.getKey() < targetKey && collectionManager.hasAccess(entry.getKey(), username)) {
                    keysToRemove.add(entry.getKey());
                }
            }

            // Удаляем объекты
            for (Long key : keysToRemove) {
                if (collectionManager.remove(key, username)) {
                    removedCount++;
                }
            }

            return new NetworkResponse(200, "Удалено элементов: " + removedCount);
        } catch (ClassCastException e) {
            return new NetworkResponse(400, "Неверный формат ключа");
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при удалении элементов: " + e.getMessage());
        }
    }
}