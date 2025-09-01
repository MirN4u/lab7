package commands;

import managers.CommandManager;
import utility.Console;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

public class Help extends Command {
    private final Console console;
    private final CommandManager commandManager;

    public Help(Console console, CommandManager commandManager) {
        super("help", "вывести справку по доступным командам");
        this.console = console;
        this.commandManager = commandManager;
    }

    @Override
    public ExecutionResponse apply(String argument) {
        if (!argument.isEmpty()) {
            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
        }

        StringBuilder helpMessage = new StringBuilder();
        commandManager.getCommands().values().forEach(command -> {
            helpMessage.append(String.format(" %-35s%-1s%n", command.getName(), command.getDescription()));
        });

        return new ExecutionResponse(helpMessage.toString());
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            StringBuilder helpMessage = new StringBuilder();
            commandManager.getCommands().values().forEach(cmd -> {
                helpMessage.append(String.format("%s - %s\n", cmd.getName(), cmd.getDescription()));
            });

            return new NetworkResponse(200, helpMessage.toString());
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при получении справки");
        }
    }
}