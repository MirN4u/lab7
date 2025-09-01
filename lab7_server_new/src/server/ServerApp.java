package server;

import commands.*;
import managers.AuthManager;
import managers.CollectionManager;
import managers.CommandManager;
import managers.DumpManager;
import utility.Console;
import utility.StandartConsole;

public class ServerApp {
    private static CollectionManager collectionManager;
    private static UDPServer server;
    private static AuthManager authManager;

    public static void main(String[] args) {
        Console console = new StandartConsole();
        String dbUrl = "jdbc:postgresql://127.0.0.1:5432/studs";
        String dbUser = "s419310";
        String dbPassword = "smFiqsBnDGisvcp8";

        DumpManager dumpManager = new DumpManager(dbUrl, dbUser, dbPassword, console);
        AuthManager authManager = dumpManager.getAuthManager();
        collectionManager = new CollectionManager(dumpManager);

        if (!collectionManager.loadCollection()) {
            System.err.println("Не удалось загрузить коллекцию из БД");
            System.exit(1);
        }

        CommandManager commandManager = new CommandManager(authManager);

        int port = 6789;
        server = new UDPServer(port, commandManager);
        registerCommands(commandManager, console, collectionManager, authManager);

        server.start();

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            console.println("Завершение работы сервера...");
            if (collectionManager != null) {
                collectionManager.saveCollection();
            }
            server.stop();
            console.println("Пулы потоков сервера остановлены.");
        }));
    }

    private static void registerCommands(CommandManager commandManager, Console console,
                                         CollectionManager collectionManager, AuthManager authManager) {
        commandManager.register("_ping", new PingCommand());
        commandManager.register("insert", new Insert(console, collectionManager));
        commandManager.register("show", new Show(console, collectionManager));
        commandManager.register("clear", new Clear(console, collectionManager));
        commandManager.register("update", new Update(console, collectionManager));
        commandManager.register("info", new Info(console, collectionManager));
        commandManager.register("remove_key", new RemoveKey(console, collectionManager));
        commandManager.register("remove_greater", new RemoveGreater(console, collectionManager));
        commandManager.register("remove_lower_key", new RemoveLower(console, collectionManager));
        commandManager.register("filter_contains_name", new FilterContains(console, collectionManager));
        commandManager.register("filter_starts_with_name", new FilterStarts(console, collectionManager));
        commandManager.register("count_greater_than_location", new CountLocation(console, collectionManager));
        commandManager.register("replace_if_greater", new Replace(console, collectionManager));
        commandManager.register("save", new ServerSave(console, collectionManager));
        commandManager.register("_shutdown", new ServerShutdown(collectionManager, server));
        commandManager.register("help", new Help(console, commandManager));
        commandManager.register("login",new Login(authManager));
        commandManager.register("register", new Register(authManager));
    }
}