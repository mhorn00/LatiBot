package latibot.command.commands.audio;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.container.wav.WavAudioTrack;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;

import latibot.LatiBot;
import latibot.audio.AudioSendingHandler;
import latibot.audio.DecTalkWrapper;
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

public class SpeakCmd extends BaseCommand {

    public SpeakCmd() {
        super("speak", "Speak using LatiBot.dectalk.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        String text = e.getOption("text").getAsString();
        if (LatiBot.dectalk == null) {
            LatiBot.dectalk = new DecTalkWrapper();
            LatiBot.dectalk.ttsStartup();
            try {
                Files.createDirectories(Paths.get("tts"));
            } catch (IOException err) {
                LatiBot.LOG.error("Failed to create tts directory.", err);
            }
        }
        UUID uuid = UUID.randomUUID();
        LatiBot.dectalk.ttsSpeak(text, "tts/" + uuid.toString() + ".wav");

        AudioManager am = e.getGuild().getAudioManager();
        if (!am.isConnected() && e.getMember().getVoiceState().inAudioChannel()) {
            am.openAudioConnection(e.getMember().getVoiceState().getChannel().asVoiceChannel()); // this will fail if
                                                                                                 // its a stage channel
                                                                                                 // lol
        } else if (!am.isConnected() && !e.getMember().getVoiceState().inAudioChannel()) {
            e.reply("i'm not currently in a voice channel").setEphemeral(true).setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
            return;
        }
        if (LatiBot.tm == null) {
            LatiBot.tm = new TrackManager(LatiBot.audioPlayer);
            LatiBot.audioPlayer.addListener(LatiBot.tm);
            am.setSendingHandler(new AudioSendingHandler(LatiBot.audioPlayer));
        }
        try {
            LatiBot.tm.queueNow(new WavAudioTrack(
                    new com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo("TTS", "LatiBot.dectalk", 1, "", false,
                            ""),
                    new NonSeekableInputStream(new FileInputStream(new File("tts/" + uuid.toString() + ".wav")))),
                    e.getMember());
            e.reply("ok playing tts").setEphemeral(true).setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
        } catch (FileNotFoundException e1) {
            e.reply("Failed to play tts").setSuppressedNotifications(true)
                    .queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
            e1.printStackTrace();
        }
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash(name, description)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK))
                .addOptions(new OptionData(OptionType.STRING, "text", "Text to speak.", true));
    }

    @Override
    public SlashCommandData buildCommand(String alias) {
        return Commands.slash(alias, description)
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK))
                .addOptions(new OptionData(OptionType.STRING, "text", "Text to speak.", true));
    }
}
