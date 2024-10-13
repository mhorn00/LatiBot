package latibot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;

import latibot.audio.DecTalkWrapper;
import latibot.audio.TrackManager;
import latibot.command.Commands;
import latibot.listeners.ActionListener;
import latibot.listeners.CommandListener;
import latibot.listeners.NicknameListener;
import latibot.listeners.ReadyListener;
import latibot.utils.MidnightManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
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
        jdaInst = JDABuilder.createDefault(new String(LatiBot.class.getClassLoader().getResourceAsStream("token.txt").readAllBytes()))
        		.setActivity(Activity.watching("for midnight..."))
        		.setMemberCachePolicy(MemberCachePolicy.ALL)
        		.enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_MESSAGE_REACTIONS)
        		.enableCache(CacheFlag.VOICE_STATE)
        		.setChunkingFilter(ChunkingFilter.ALL)
        		.addEventListeners(new CommandListener(), new NicknameListener(), new ReadyListener(), new ActionListener()).build();
        
		jdaInst.updateCommands().addCommands(
			Commands.COMMANDS.getCommands().values().stream().map((v) -> {
				if (v.hasAlias()) for (String alias : v.getAliases()) {
					LOG.info("Registering alias "+alias+" for command "+v.getName());
					return v.buildCommand(alias);
				}
				LOG.info("Registering command "+v.getName());
				return v.buildCommand();
			}).toList()
		).complete();

        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        audioPlayer = audioPlayerManager.createPlayer();

		MidnightManager.scheduleMidnight();
    }
}
