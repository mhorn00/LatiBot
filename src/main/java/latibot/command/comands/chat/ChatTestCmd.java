package latibot.command.comands.chat;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;

import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;

import latibot.LatiBot;
import latibot.audio.DecTalkWrapper;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.FileUpload;
import okhttp3.MediaType;

public class ChatTestCmd extends BaseCommand {

    private static final String enablePhonemes = "[:phoneme arpabet speak on]";

    public ChatTestCmd() {
        super("chat", "Test chat command. you dont see this lol");
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
        LatiBot.dectalk.ttsSpeak(enablePhonemes + text, "tts/" + uuid.toString() + ".wav");
        String path = "tts/" + uuid.toString() + ".wav";
        File audio = new File(path);
        if (!audio.exists()) {
            e.reply("Failed to generate tts file.");
            return;
        }
        try {
            // Get the length of the audio file
            AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(audio);
            long frameLength = audioInputStream.getFrameLength();
            float frameRate = audioInputStream.getFormat().getFrameRate();
            double durationInSeconds = frameLength / frameRate;
            
            // Get a byte array sample of the entire waveform
            byte[] audioBytes = Files.readAllBytes(audio.toPath());
            byte[] sample = new byte[256];
            int sampleInterval = audioBytes.length / 256;
            for (int i = 0; i < 256; i++) {
                int sum = 0;
                for (int j = 0; j < sampleInterval; j++) {
                    sum += audioBytes[i * sampleInterval + j];
                }
                sample[i] = (byte) (sum / sampleInterval);
            }
            // Reply with the voice message
            e.replyFiles(FileUpload.fromData(audio).asVoiceMessage(MediaType.parse("audio/wav"), sample, durationInSeconds)).setSuppressedNotifications(true).queue();

        } catch (IOException err) {
            LatiBot.LOG.error("Failed to upload tts file.", err);
        } catch (UnsupportedAudioFileException err) {
            LatiBot.LOG.error("Failed to read tts file.", err);
        }

    }

    @Override
    public SlashCommandData buildCommand() {
      return Commands.slash(name, description)
                .addOption(OptionType.STRING, "text", "Text to speak")
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK));
    }

    @Override
    public SlashCommandData buildCommand(String alias) {
        return Commands.slash(name, description)
        .addOption(OptionType.STRING, "text", "Text to speak")
        .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK));
    }
    
}
