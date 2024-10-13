package latibot.listeners;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import latibot.command.Commands;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		
        BaseCommand cmd  = Commands.COMMANDS.getCommand(e.getName());

        if (cmd != null) {
            cmd.execute(e);
        } else {
            e.reply("Command not found?").setEphemeral(true).queue();
            LatiBot.LOG.warn("Unknown command: " + e.getName());
        }

	
	}
}