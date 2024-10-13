package latibot.command.comands.bot;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PingCmd extends BaseCommand {
    
        public PingCmd() {
            super("ping", "Pong!");
        }
    
        @Override
        public void execute(SlashCommandInteractionEvent e) {
           long time = System.currentTimeMillis();
		    e.reply("Pong!").setEphemeral(true).flatMap(v -> e.getHook().editOriginalFormat("Pong! (%d ms)", System.currentTimeMillis() - time)).queue();
            LatiBot.LOG.info("Ping from "+e.getUser().getName());
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
