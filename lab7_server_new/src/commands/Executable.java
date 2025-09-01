package commands;

import utility.ExecutionResponse;

/**
 * Интерфейс для выполнения команд
 *
 * @author Miroslav
 * @version 1.0
 */

public interface Executable {
    ExecutionResponse apply(String arguments);
}
