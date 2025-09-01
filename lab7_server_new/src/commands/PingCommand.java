package commands;

import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

public class PingCommand extends Command {
    public PingCommand() {
        super("_ping", "проверка активности сервера");
    }

    @Override
    public ExecutionResponse apply(String argument) {
        return new ExecutionResponse("pong");
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        return new NetworkResponse(200, "pong");
    }
}