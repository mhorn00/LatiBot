package latibot.command;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class CommandRegistry {
    private Map<String, BaseCommand> commands = new HashMap<>();
    private Map<String, String> aliases = new HashMap<>();

    public <C extends BaseCommand> C registerCommand(final C command) {
        commands.put(command.name, command);
        return command;
    }

    public <C extends BaseCommand> C registerCommandWithAlias(final C command, final String alias) {
        if (commands.containsKey(alias))
            throw new IllegalArgumentException("Alias already in use");
        // add to alias map
        aliases.put(alias, command.name);
        // add the cmd
        commands.put(command.name, command.addAlias(alias));
        return command;
    }

    public BaseCommand getCommand(final String name) {
        // return from alias map if it exists
        if (aliases.containsKey(name))
            return commands.get(aliases.get(name));
        return commands.get(name);
    }

    public Map<String, BaseCommand> getCommands() {
        return Collections.unmodifiableMap(commands);
    }

}
