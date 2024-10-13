package latibot.command;

import java.util.ArrayList;
import java.util.List;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public abstract class BaseCommand {

    public final String name;
    public final String description;
    public final List<String> aliases;

    public BaseCommand(String name, String description) {
        this.name = name;
        this.description = description;
        this.aliases = new ArrayList<String>();
    }

    public abstract void execute(SlashCommandInteractionEvent e);

    public BaseCommand addAlias(String alias) {
        aliases.add(alias);
        return this;
    }

    public List<String> getAliases() {
        return aliases;
    }

    public boolean hasAlias() {
        return !aliases.isEmpty();
    }

    public String getName() {
        return name;
    }

    public abstract SlashCommandData buildCommand();

    public abstract SlashCommandData buildCommand(String alias);
}
