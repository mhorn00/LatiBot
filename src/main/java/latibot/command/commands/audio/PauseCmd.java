package latibot.command.commands.audio;

import java.util.concurrent.TimeUnit;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class PauseCmd extends BaseCommand {

    public PauseCmd() {
        super("pause", "Pauses the current song.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        if (LatiBot.tm == null) {
            e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        } else if (LatiBot.tm.getQueue().getCurrent() == null) {
            e.reply("there isn't anything playing").setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        } else {
            if (LatiBot.tm.togglePause()) {
                e.reply("ok paused").setSuppressedNotifications(true)
                        .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
            } else {
                e.reply("ok unpaused").setSuppressedNotifications(true)
                        .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
            }
        }
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash(name, description)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK));
    }

    @Override
    public SlashCommandData buildCommand(String alias) {
        return Commands.slash(alias, description)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK));
    }
}
