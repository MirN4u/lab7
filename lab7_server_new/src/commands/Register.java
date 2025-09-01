package commands;

import utility.NetworkCommand;
import utility.NetworkResponse;
import managers.AuthManager;

public class Register extends Command {
    private final AuthManager authManager;

    public Register(AuthManager authManager) {
        super("register", "регистрация нового пользователя");
        this.authManager = authManager;
    }

    @Override
    public NetworkResponse execute(NetworkCommand command) {
        Object[] args = command.getArgs();
        if (args == null || args.length < 2) {
            return new NetworkResponse(400, "Необходимо указать логин и пароль");
        }

        String username = (String) args[0];
        String password = (String) args[1];

        if (authManager.userExists(username)) {
            return new NetworkResponse(409, "Пользователь уже существует");
        }

        if (authManager.register(username, password)) {
            return new NetworkResponse(200, "Пользователь успешно зарегистрирован");
        } else {
            return new NetworkResponse(500, "Ошибка регистрации пользователя");
        }
    }
}