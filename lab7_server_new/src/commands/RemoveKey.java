package commands;

import models.Person;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;
import managers.CollectionManager;

public class RemoveKey extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public RemoveKey(Console console, CollectionManager collectionManager) {
        super("remove_key", "удалить элемент по ключу");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        try {
            if (argument.isEmpty()) {
                return new ExecutionResponse(false, "Требуется указать ключ");
            }
            Long key = Long.parseLong(argument.trim());
            return new ExecutionResponse("Объект удален");
        } catch (NumberFormatException e) {
            return new ExecutionResponse(false, "Ключ должен быть числом");
        }
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            Object[] args = command.getArgs();
            if (args == null || args.length < 1) {
                return new NetworkResponse(400, "Необходимо указать ключ");
            }

            Long key = (Long) args[0];
            String username = command.getUsername();

            boolean result = collectionManager.remove(key, username);

            if (result) {
                return new NetworkResponse(200, "Объект успешно удален");
            } else {
                return new NetworkResponse(403, "У вас нет прав для удаления этого объекта или объект не найден");
            }

        } catch (ClassCastException e) {
            return new NetworkResponse(400, "Неверный формат ключа");
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при выполнении команды: " + e.getMessage());
        }
    }
}