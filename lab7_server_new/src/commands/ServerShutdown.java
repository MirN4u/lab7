package commands;

import managers.CollectionManager;
import managers.DumpManager;
import server.UDPServer;
import utility.ExecutionResponse;
import utility.NetworkCommand;
import utility.NetworkResponse;

public class ServerShutdown extends Command {
    private final CollectionManager collectionManager;
    private final UDPServer server;
    private final DumpManager dumpManager;

    public ServerShutdown(CollectionManager collectionManager, UDPServer server) {
        super("_shutdown", "корректное завершение работы сервера");
        this.collectionManager = collectionManager;
        this.server = server;
        this.dumpManager = collectionManager.getDumpManager();
    }

    @Override
    public ExecutionResponse apply(String argument) {
        return new ExecutionResponse(false, "Эта команда доступна только через сетевой интерфейс");
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        try {
            collectionManager.saveCollection();
            dumpManager.resetSequence();
            new Thread(() -> {
                try {
                    Thread.sleep(500); // Даем время на отправку ответа
                    server.stop();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            }).start();

            return new NetworkResponse(200, "Сервер завершает работу... Sequence сброшен.");
        } catch (Exception e) {
            return new NetworkResponse(500, "Ошибка при завершении работы: " + e.getMessage());
        }
    }
}