package commands;

import managers.CollectionManager;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

import java.time.ZonedDateTime;

public class Info extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public Info(Console console, CollectionManager collectionManager) {
        super("info", "вывести информацию о коллекции");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        if (!argument.isEmpty()) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        }

        ZonedDateTime lastInitTime = collectionManager.getLastInitTime();
        String lastInitTimeStr = (lastInitTime == null) ? "в данной сессии инициализации еще не происходило" :
                lastInitTime.toString();

        ZonedDateTime lastSaveTime = collectionManager.getLastSaveTime();
        String lastSaveTimeStr = (lastSaveTime == null) ? "в данной сессии сохранения еще не происходило" :
                lastSaveTime.toString();

        String info = "Сведения о коллекции:\n" +
                " Тип: " + collectionManager.getCollection().getClass().getName() + "\n" +
                " Количество элементов: " + collectionManager.getCollection().size() + "\n" +
                " Дата последнего сохранения: " + lastSaveTimeStr + "\n" +
                " Дата последней инициализации: " + lastInitTimeStr;

        return new ExecutionResponse(info);
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            ZonedDateTime lastInitTime = collectionManager.getLastInitTime();
            ZonedDateTime lastSaveTime = collectionManager.getLastSaveTime();

            String response = String.format(
                    "Тип: %s\nРазмер: %d\nПоследняя инициализация: %s\nПоследнее сохранение: %s",
                    collectionManager.getCollection().getClass().getName(),
                    collectionManager.getCollection().size(),
                    lastInitTime != null ? lastInitTime.toString() : "N/A",
                    lastSaveTime != null ? lastSaveTime.toString() : "N/A"
            );

            return new NetworkResponse(200, response);
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при получении информации о коллекции");
        }
    }
}