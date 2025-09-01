package utility;

import java.io.Serializable;

public class NetworkCommand implements Serializable {
    private static final long serialVersionUID = 1L;

    private String commandName;
    private Object[] args;
    private String username;
    private String password;

    public NetworkCommand(String commandName, Object[] args, String username, String password) {
        this.commandName = commandName;
        this.args = args;
        this.username = username;
        this.password = password;
    }

    public NetworkCommand(String commandName, Object[] args) {
        this(commandName, args, null, null);
    }

    public String getCommandName() { return commandName; }
    public Object[] getArgs() { return args; }
    public String getUsername() { return username; }
    public String getPassword() { return password; }

    public void setArgs(Object[] args) { this.args = args; }
    public void setCommandName(String commandName) { this.commandName = commandName; }
    public void setUsername(String username) { this.username = username; }
    public void setPassword(String password) { this.password = password; }
}