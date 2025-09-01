//package commands;
//
//import utility.Console;
//import utility.ExecutionResponse;
//import utility.NetworkCommand;
//import utility.NetworkResponse;
//
//public class ExecuteScript extends Command {
//    private final Console console;
//
//    public ExecuteScript(Console console) {
//        super("execute_script file_name", "исполнить скрипт из указанного файла");
//        this.console = console;
//    }
//
//    @Override
//    public ExecutionResponse apply(String argument) {
//        if (argument.isEmpty()) {
//            return new ExecutionResponse(false, "Неправильное количество аргументов!\nИспользование: '" + getName() + "'");
//        }
//        return new ExecutionResponse("Выполнение скрипта '" + argument + "'...");
//    }
//
//    @Override
//    public NetworkResponse execute(NetworkCommand command) {
//        return new NetworkResponse(501, "Выполнение скриптов через сеть не поддерживается");
//    }
//}