package latibot.command.commands.misc;

import latibot.command.BaseCommand;
import latibot.listeners.MessageListener;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ToggleWebhooksCmd extends BaseCommand {
    public ToggleWebhooksCmd() {
        super("togglewebhooks", "toggles between webhook and message reply url replacement");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        MessageListener.useWebhooks = !MessageListener.useWebhooks;
        e.reply("Now using " + (MessageListener.useWebhooks ? "webhooks" : "message replies") + " for URL replacement.").queue();
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash(name, description);
    }

    @Override
    public SlashCommandData buildCommand(String alias) {
        return Commands.slash(alias, description);
    }
}
