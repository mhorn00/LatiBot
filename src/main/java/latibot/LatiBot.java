package latibot;

import java.io.IOException;
import java.util.stream.Stream;
import java.util.List;

import latibot.listeners.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import latibot.audio.DecTalkWrapper;
import latibot.audio.TrackManager;
import latibot.command.Commands;
import latibot.utils.MidnightManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

/* TODO: General
 *  - Rewrite latibot in dart lol
 *  - Wordle stats
 *  - midnight stats
 *  - reminders?
 *  - speak cmd in chat
 *  - make dectalk louder lol
 *  - dectalk copy pastas
 *  - random sound effects
 *  - fake quote cmd
 *  - music controls via buttons
 */

public class LatiBot {

    public static JDA jdaInst;
    public static final Logger LOG = LoggerFactory.getLogger(LatiBot.class);
    public static final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    public static AudioPlayer audioPlayer;
    public static TrackManager tm = null;
    public static DecTalkWrapper dectalk = null;

    public static void shutdownTTS() {
        if (dectalk != null) {
            dectalk.ttsShutdown();
        }
    }

    public static void main(String[] args) throws IOException {
        jdaInst = JDABuilder
                .createDefault(
                        new String(LatiBot.class.getClassLoader().getResourceAsStream("token.txt").readAllBytes()))
                .setActivity(Activity.watching("for midnight..."))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .enableCache(CacheFlag.VOICE_STATE)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(new CommandListener(), new NicknameListener(), new ReadyListener(),
                        new ActionListener(), new MessageListener(), new ReactionListener())
                .build();

        List<SlashCommandData> cmds = Commands.COMMANDS.getCommands().values().stream().flatMap((v) -> {
            Stream.Builder<SlashCommandData> b = Stream.builder();

            LOG.info("Registering command " + v.getName());
            b.add(v.buildCommand());

            if (v.hasAlias())
                for (String alias : v.getAliases()) {
                    LOG.info("Registering alias " + alias + " for command " + v.getName());
                    b.add(v.buildCommand(alias));
                }

            return b.build();
        }).toList();

        jdaInst.updateCommands().addCommands(cmds).queue();

        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        audioPlayer = audioPlayerManager.createPlayer();

        MidnightManager.scheduleMidnight();
    }
}
