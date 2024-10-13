package latibot.command.comands.audio;

import java.util.concurrent.TimeUnit;

import latibot.LatiBot;
import latibot.audio.AudioTrackInfo;
import latibot.audio.TrackManager.SongQueue;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class QueueCmd extends BaseCommand {

    public QueueCmd() {
        super("queue", "Display the current queue.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        if (LatiBot.tm == null) {
            e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        } else if (LatiBot.tm.getQueue().isQueueEmpty()) {
            e.reply("the queue is empty!").setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        } else {
            SongQueue queue = LatiBot.tm.getQueue();
            String[] out = new String[100];
            out[0] = "Current song:\n" + queue.getCurrent().getAudioTrack().getInfo().title + " - Queued by "
                    + queue.getCurrent().getMember().getEffectiveName() + "\n";
            out[1] = "Queue:\n";
            for (int i = 0, j = 1; i < queue.size() && j < 100; i++) {
                AudioTrackInfo cur = queue.get(i);
                String add = (i + 1) + ". " + cur.getAudioTrack().getInfo().title + " - Queued by "
                        + cur.getMember().getEffectiveName() + "\n";
                if (out[j].length() + add.length() > 2000)
                    j++;
                if (out[j] == null)
                    out[j] = "";
                out[j] = out[j] + add;
            }
            e.reply(out[0]).setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(60, TimeUnit.SECONDS));
            for (int i = 1; i < out.length; i++) {
                if (out[i] != null && !out[i].isEmpty() && !out[i].isBlank())
                    e.getChannel().sendMessage(out[i]).setSuppressedNotifications(true)
                            .queue(hook -> hook.delete().queueAfter(60, TimeUnit.SECONDS));
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
