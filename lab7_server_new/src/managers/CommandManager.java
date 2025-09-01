package managers;

import commands.Command;
import utility.NetworkCommand;
import utility.NetworkResponse;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class CommandManager {
    private final Map<String, Command> commands = new LinkedHashMap<>();
    private final List<String> commandHistory = new ArrayList<>();
    private final AuthManager authManager;

    private final List<String> noAuthCommands = List.of(
            "_ping", "register", "login", "help",
            "show", "info", "filter_contains_name",
            "filter_starts_with_name", "count_greater_than_location"
    );

    public CommandManager(AuthManager authManager) {
        this.authManager = authManager;
    }

    public void register(String commandName, Command command) {
        commands.put(commandName, command);
    }

    public Map<String, Command> getCommands() {
        return commands;
    }

    public List<String> getCommandHistory() {
        return commandHistory;
    }

    public void addToHistory(String command) {
        commandHistory.add(command);
    }

    public NetworkResponse executeNetworkCommand(NetworkCommand command) {
        String commandName = command.getCommandName();
        if (noAuthCommands.contains(commandName)) {
            Command cmd = commands.get(commandName);
            if (cmd == null) {
                return new NetworkResponse(404, "Команда не найдена");
            }
            return cmd.execute(command);
        }

        //Команды требуют аутентификации
        if (command.getUsername() == null || command.getPassword() == null) {
            return new NetworkResponse(401, "Требуется аутентификация. Используйте команду 'login'");
        }

        if (!authManager.authenticate(command.getUsername(), command.getPassword())) {
            return new NetworkResponse(401, "Неверные учетные данные. Используйте команду 'login'");
        }

        Command cmd = commands.get(commandName);
        if (cmd == null) {
            return new NetworkResponse(404, "Команда не найдена");
        }

        return cmd.execute(command);
    }
}