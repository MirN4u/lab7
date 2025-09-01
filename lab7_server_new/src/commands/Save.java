package commands;

import managers.CollectionManager;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;


/**
 * Сохранение коллекции
 *
 * @author Miroslav
 * @version 1.0
 */



public class Save extends Command {
    private final Console console;
    private final CollectionManager collectionManager;

    public Save(Console console, CollectionManager collectionManager) {
        super("save", "сохранить коллекцию в файл");
        this.console = console;
        this.collectionManager = collectionManager;
    }

    @Override
    public ExecutionResponse apply(String arguments) {
        if (!arguments.isEmpty())
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");

        collectionManager.saveCollection();
        return new ExecutionResponse(true, "");
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        return null;
    }
}
