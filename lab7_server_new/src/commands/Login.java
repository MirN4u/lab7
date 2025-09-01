package commands;

import utility.NetworkCommand;
import utility.NetworkResponse;
import managers.AuthManager;

public class Login extends Command {
    private final AuthManager authManager;

    public Login(AuthManager authManager) {
        super("login", "аутентификация пользователя");
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

        if (authManager.authenticate(username, password)) {
            return new NetworkResponse(200, "Аутентификация успешна");
        } else {
            return new NetworkResponse(401, "Неверные учетные данные");
        }
    }
}