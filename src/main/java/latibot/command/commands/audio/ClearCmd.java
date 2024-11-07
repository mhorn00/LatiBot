package latibot.command.commands.audio;

import java.util.concurrent.TimeUnit;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ClearCmd extends BaseCommand {

    public ClearCmd() {
        super("clear", "Clears the queue.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        if (LatiBot.tm == null) {
            e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        } else if (LatiBot.tm.getQueue().isQueueEmpty()) {
            e.reply("the queue is already empty!").setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        } else {
            e.reply("queue cleared!").setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
            LatiBot.tm.clearQueue();
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
