
package commands;

import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;


/**
 * Команда выхода из приложения
 *
 * @author Miroslav
 * @version 1.0
 */


public class Exit extends Command {
    private final Console console;

    public Exit(Console console) {
        super("exit", "завершить программу (без сохранения в файл)");
        this.console = console;
    }

    @Override
    public ExecutionResponse apply(String arguments) {
        if (!arguments.isEmpty())
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");

        return new ExecutionResponse("exit"); //"Завершение выполнения
    }
    @Override
    public NetworkResponse execute(NetworkCommand command) {
        return new NetworkResponse(200, "Завершение работы сервера");
    }
}
