package commands;

import utility.*;

/**
 * Абстрактный класс с именем и описанием команд
 *
 * @author Miroslav
 * @version 1.0
 */

import utility.NetworkCommand;
import utility.NetworkResponse;

/**
 * Абстрактный класс с именем и описанием команд
 *
 * @author Miroslav
 * @version 1.0
 */

public abstract class Command implements Describable, Executable {
    private final String name;
    private final String description;

    public Command(String name, String description) {
        this.name = name;
        this.description = description;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    // Основной метод для сетевых команд
    public abstract NetworkResponse execute(NetworkCommand command);

    // Метод для обратной совместимости
    public ExecutionResponse apply(String argument) {
        return new ExecutionResponse(false, "This command requires network context");
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        Command command = (Command) obj;
        return name.equals(command.name) && description.equals(command.description);
    }

    @Override
    public int hashCode() {
        return name.hashCode() + description.hashCode();
    }

    @Override
    public String toString() {
        return "Command{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                '}';
    }
}