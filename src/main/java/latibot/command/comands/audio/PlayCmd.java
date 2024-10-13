package latibot.command.comands.audio;

import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import latibot.LatiBot;
import latibot.audio.AudioSendingHandler;
import latibot.audio.TrackManager;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

public class PlayCmd extends BaseCommand {

    public PlayCmd() {
        super("play", "Adds a song to the queue.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        String link = e.getOption("link").getAsString();
        PlayTypes type = e.getOption("type") == null
                ? PlayTypes.Normal
                : PlayTypes.fromCmd(e.getOption("type").getAsString());
        boolean isSilent = e.getOption("silent") == null ? false : e.getOption("silent").getAsBoolean();

        AudioManager am = e.getGuild().getAudioManager();

        // if not connected and the user is in a voice channel, connect to that
        // channel
        if (!am.isConnected() && e.getMember().getVoiceState().inAudioChannel()) {
            am.openAudioConnection(e.getMember().getVoiceState().getChannel().asVoiceChannel());
        }
        // otherwise if not connected and the user is not in a voice channel, reply
        else if (!am.isConnected() && !e.getMember().getVoiceState().inAudioChannel()) {
            e.reply("i'm not currently in a voice channel").setEphemeral(true).setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
            return;
        }

        // if not track manager, create one
        if (LatiBot.tm == null) {
            LatiBot.tm = new TrackManager(LatiBot.audioPlayer);
            LatiBot.audioPlayer.addListener(LatiBot.tm);
            am.setSendingHandler(new AudioSendingHandler(LatiBot.audioPlayer));
        }

        // add the track loader to play our song
        LatiBot.audioPlayerManager.loadItemOrdered(LatiBot.audioPlayer, link, new LatiALRH(e, link, type, isSilent));
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash(name, description)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK))
                .addOptions(
                        new OptionData(OptionType.STRING, "link", "The link to add to the queue.", true),
                        new OptionData(OptionType.STRING, "type", "Option type of play command", false)
                                .addChoice(PlayTypes.Next.name, PlayTypes.Next.description)
                                .addChoice(PlayTypes.Now.name, PlayTypes.Now.description),
                        new OptionData(OptionType.BOOLEAN, "silent", "suppress the bot response for this command",
                                false));
    }

    @Override
    public SlashCommandData buildCommand(String alias) {
        return Commands.slash(alias, description)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK))
                .addOptions(
                        new OptionData(OptionType.STRING, "link", "The link to add to the queue.", true),
                        new OptionData(OptionType.STRING, "type", "Option type of play command", false)
                                .addChoice(PlayTypes.Next.name, PlayTypes.Next.description)
                                .addChoice(PlayTypes.Now.name, PlayTypes.Now.description),
                        new OptionData(OptionType.BOOLEAN, "silent", "suppress the bot response for this command",
                                false));
    }

    // enum for the different types of play commands
    private enum PlayTypes {
        Normal("play", "play this track normally"),
        Next("play next", "play this track next"),
        Now("play now", "play this track now"),
        ;

        public final String name;
        public final String description;

        private PlayTypes(String name, String description) {
            this.name = name;
            this.description = description;
        }

        public static PlayTypes fromCmd(String name) {
            for (PlayTypes type : values()) {
                if (type.name.equals(name)) {
                    return type;
                }
            }
            return null;
        }
    }

    // audio load result handler for playing the track
    private class LatiALRH implements AudioLoadResultHandler {

        private SlashCommandInteractionEvent e;
        private String link;
        private PlayTypes type;
        private boolean isSilent;

        public LatiALRH(SlashCommandInteractionEvent event, String link, PlayTypes type, boolean silent) {
            this.e = event;
            this.link = link;
            this.type = type;
            this.isSilent = silent;
        }

        @Override
        public void trackLoaded(AudioTrack track) {
            switch (type) {
                case Next:
                    e.reply("ok playing track next " + track.getInfo().title).setEphemeral(isSilent)
                            .setSuppressedNotifications(true)
                            .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                    LatiBot.tm.queueNext(track, e.getMember());
                    break;
                case Now:
                    e.reply("ok playing track now " + track.getInfo().title).setEphemeral(isSilent)
                            .setSuppressedNotifications(true)
                            .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                    LatiBot.tm.queueNow(track, e.getMember());
                    break;
                case Normal:
                default:
                    e.reply("ok playing track " + track.getInfo().title).setEphemeral(isSilent)
                            .setSuppressedNotifications(true)
                            .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                    LatiBot.tm.queue(track, e.getMember());
                    break;
            }
        }

        @Override
        public void playlistLoaded(AudioPlaylist playlist) {
            switch (type) {
                case Next:
                    if (!isSilent)
                        e.reply("ok queuing playlist next" + playlist.getName()).setEphemeral(isSilent)
                                .setSuppressedNotifications(true)
                                .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                    for (AudioTrack track : playlist.getTracks()) {
                        LatiBot.tm.queueNext(track, e.getMember());
                    }
                    break;
                case Now:
                    if (!isSilent)
                        e.reply("don't queue a playlist with play now command lol").setEphemeral(isSilent)
                                .setSuppressedNotifications(true)
                                .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                    break;
                case Normal:
                default:
                    e.reply("ok queuing playlist " + playlist.getName()).setEphemeral(isSilent)
                            .setSuppressedNotifications(true)
                            .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
                    for (AudioTrack track : playlist.getTracks()) {
                        LatiBot.tm.queue(track, e.getMember());
                    }
                    break;
            }
        }

        @Override
        public void noMatches() {
            e.reply("no track found").setEphemeral(isSilent).setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
        }

        @Override
        public void loadFailed(FriendlyException exception) {
            // no silent for this one lol
            e.reply("exception occurred! check logs").setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(60, TimeUnit.SECONDS));
            LatiBot.LOG.warn("Exception occurred while trying to play track " + link + ": ", exception);
        }

    }
}
