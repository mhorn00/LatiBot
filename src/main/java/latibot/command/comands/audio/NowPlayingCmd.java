package latibot.command.comands.audio;

import java.util.concurrent.TimeUnit;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class NowPlayingCmd extends BaseCommand {

    public NowPlayingCmd() {
        super("nowplaying", "Displays the currnetly playing track.");
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
            e.reply("now playing: " + LatiBot.tm.getQueue().getCurrent().getAudioTrack().getInfo().title
                    + " - Queued by " + LatiBot.tm.getQueue().getCurrent().getMember().getEffectiveName())
                    .setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
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
