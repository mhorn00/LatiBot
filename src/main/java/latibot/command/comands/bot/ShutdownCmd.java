package latibot.command.comands.bot;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ShutdownCmd extends BaseCommand {

    public ShutdownCmd() {
        super("shutdown", "Shuts down the bot");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        LatiBot.shutdownTTS();
        e.getGuild().getAudioManager().closeAudioConnection();
        e.reply("ok bye bye!").complete();
        e.getJDA().shutdown();
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash(name, description)
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }

    @Override
    public SlashCommandData buildCommand(String alias) {
        return Commands.slash(alias, description)
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
    }
}
