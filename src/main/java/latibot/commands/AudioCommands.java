package latibot.commands;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import com.sedmelluq.discord.lavaplayer.container.wav.WavAudioTrack;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.tools.io.NonSeekableInputStream;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import latibot.LatiBot;
import latibot.audio.AudioSendingHandler;
import latibot.audio.AudioTrackInfo;
import latibot.audio.DecTalkWrapper;
import latibot.audio.TrackManager;
import latibot.audio.TrackManager.SongQueue;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.managers.AudioManager;

public class AudioCommands {

	private static TrackManager tm = null;
	private static DecTalkWrapper dectalk = null;
	
	//======== Queue Commands ========
	
	public static void clearCmd(SlashCommandInteractionEvent e) {
		if (tm == null) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else if (tm.getQueue().isQueueEmpty()) {
			e.reply("the queue is already empty!").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			e.reply("queue cleared!").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			tm.clearQueue();
		}
	}
	
	public static void shuffleCmd(SlashCommandInteractionEvent e) {
		if (tm == null) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else if (tm.getQueue().isQueueEmpty()) {
			e.reply("the queue is currently empty").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			e.reply("ok queue shuffled").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			tm.shuffleQueue();
		}
	}
	
	public static void queueCmd(SlashCommandInteractionEvent e) {
		if (tm == null) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else if (tm.getQueue().isQueueEmpty()) {
			e.reply("the queue is empty!").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			SongQueue queue = tm.getQueue();
			String[] out = new String[100];
			out[0] = "Current song:\n"+queue.getCurrent().getAudioTrack().getInfo().title+" - Queued by "+queue.getCurrent().getMember().getEffectiveName()+"\n";
			out[1] = "Queue:\n";
			for (int i = 0, j = 1; i < queue.size() && j < 100; i++) {
				AudioTrackInfo cur = queue.get(i);
				String add = (i+1) + ". " + cur.getAudioTrack().getInfo().title + " - Queued by "+cur.getMember().getEffectiveName()+"\n";
				if (out[j].length()+add.length() > 2000) j++;
				if (out[j] == null) out[j] = "";
				out[j] = out[j]+add;
			}
			e.reply(out[0]).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(60, TimeUnit.SECONDS));
			for (int i=1;i<out.length;i++) {
				if (out[i]!=null && !out[i].isEmpty() && !out[i].isBlank()) e.getChannel().sendMessage(out[i]).setSuppressedNotifications(true).queue(hook -> hook.delete().queueAfter(60, TimeUnit.SECONDS));
			}
		}
	}
	
	
	//======== Bot Control Commands ========
	
	public static void joinCmd(SlashCommandInteractionEvent e) {
		Member user = e.getOption("user").getAsMember();
		AudioManager am = e.getGuild().getAudioManager();
		if (user != null && user.getVoiceState().inAudioChannel()) {
			VoiceChannel vc = user.getVoiceState().getChannel().asVoiceChannel();
			if (!am.isConnected()) {
				am.openAudioConnection(vc);
				e.reply("ok joining "+user.getEffectiveName()).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			} else if (am.getConnectedChannel().getId().equals(vc.getId())) {
				e.reply("i'm already in "+user.getEffectiveName()+"'s voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			} else {
				am.openAudioConnection(vc);
				e.reply("ok moving to "+user.getEffectiveName()).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
		} else if (e.getMember().getVoiceState().inAudioChannel()) {
			VoiceChannel vc = e.getMember().getVoiceState().getChannel().asVoiceChannel();
			if (!am.isConnected()) {
				am.openAudioConnection(vc);
				e.reply("ok joining").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			} else if (am.getConnectedChannel().getId().equals(vc.getId())) {
				e.reply("i'm already in your voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			} else {
				am.openAudioConnection(vc);
				e.reply("ok moving").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
		} else {
			e.reply("you're not in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		}
	}
	
	public static void leaveCmd(SlashCommandInteractionEvent e) {
		AudioManager am = e.getGuild().getAudioManager();
		if (tm == null || !am.isConnected()) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			am.closeAudioConnection();
			LatiBot.audioPlayer.removeListener(tm);
			tm = null;
			e.reply("ok bye").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		}
	}

	
	//======== Music Control Commands ========
	
	public static void pauseCmd(SlashCommandInteractionEvent e) {
		if (tm == null) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else if (tm.getQueue().getCurrent() == null) {
			e.reply("there isn't anything playing").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			if (tm.togglePause()) {
				e.reply("ok paused").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			} else {
				e.reply("ok unpaused").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
		}
	}
	
	public static void repeatCmd(SlashCommandInteractionEvent e) {
		if (tm == null) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else if (tm.getQueue().getCurrent() == null) {
			e.reply("there's no song currently playing").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			if (tm.toggleRepeat()) {
				e.reply("ok repeating track "+tm.getQueue().getCurrent().getAudioTrack().getInfo().title).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			} else {
				e.reply("ok turned off repeat").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
		}
	}
	
	public static void skipCmd(SlashCommandInteractionEvent e) {
		if (tm == null) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else if (tm.getQueue().getCurrent() == null) {
			e.reply("there isn't anything playing").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			e.reply("skipping...").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			tm.skip();
		}
	}
	
	
	//======== Play Commands ========
	
	public static void playCmd(SlashCommandInteractionEvent e, PlayCommands type) {
		String link = e.getOption("link").getAsString();
		AudioManager am = e.getGuild().getAudioManager();
		boolean isSilent = type == PlayCommands.Silent;
		if (!am.isConnected() && e.getMember().getVoiceState().inAudioChannel()) {
			am.openAudioConnection(e.getMember().getVoiceState().getChannel().asVoiceChannel()); //this will fail if its a stage channel lol
		} else if (!am.isConnected() && !e.getMember().getVoiceState().inAudioChannel()) {
			e.reply("i'm not currently in a voice channel").setEphemeral(isSilent).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			return;
		} 
		if (tm == null) {
			tm = new TrackManager(LatiBot.audioPlayer);
			LatiBot.audioPlayer.addListener(tm);
			am.setSendingHandler(new AudioSendingHandler(LatiBot.audioPlayer));
		}
		LatiBot.audioPlayerManager.loadItemOrdered(LatiBot.audioPlayer, link, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				switch (type) {
				case Next:
					e.reply("ok playing track next "+track.getInfo().title).setEphemeral(isSilent).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
					tm.queueNext(track, e.getMember());
					break;
				case Now:
					e.reply("ok playing track now "+track.getInfo().title).setEphemeral(isSilent).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
					tm.queueNow(track, e.getMember());
					break;
				case Normal:
				default:
					e.reply("ok playing track "+track.getInfo().title).setEphemeral(isSilent).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
					tm.queue(track, e.getMember());
					break;	
				}
			}
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				switch (type) {
				case Next:
					e.reply("ok queuing playlist next"+playlist.getName()).setEphemeral(isSilent).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
					for (AudioTrack track : playlist.getTracks()) {
						tm.queueNext(track, e.getMember());
					}
					break;
				case Now:
					e.reply("don't queue a playlist with this command lol").setEphemeral(isSilent).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
					break;
				case Normal:
				default:
					e.reply("ok queuing playlist "+playlist.getName()).setEphemeral(isSilent).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
					for (AudioTrack track : playlist.getTracks()) {
						tm.queue(track, e.getMember());
					}
					break;	
				}
			}
			@Override
			public void noMatches() {
				e.reply("no track found").setEphemeral(isSilent).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
			@Override
			public void loadFailed(FriendlyException exception) {
				e.reply("exception occurred! check logs").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(60, TimeUnit.SECONDS));
				LatiBot.LOG.warn("Exception occurred while trying to play track "+link+": ", exception);
			}
		});
	}
	
	public enum PlayCommands {
		Normal,
		Now,
		Next,
		Silent
	}
	
	
	//======== TTS Commands ========
	
	public static void speakCmd(SlashCommandInteractionEvent e) {
		String text = e.getOption("text").getAsString();
		if (dectalk == null) {
			dectalk = new DecTalkWrapper();
			dectalk.ttsStartup();
			try {
				Files.createDirectories(Paths.get("tts"));
			} catch (IOException err) {
				LatiBot.LOG.error("Failed to create tts directory.",err);
			}
		}
		UUID uuid = UUID.randomUUID();
		dectalk.ttsSpeak(text, "tts/"+uuid.toString()+".wav");
		
		AudioManager am = e.getGuild().getAudioManager();
		if (!am.isConnected() && e.getMember().getVoiceState().inAudioChannel()) {
			am.openAudioConnection(e.getMember().getVoiceState().getChannel().asVoiceChannel()); //this will fail if its a stage channel lol
		} else if (!am.isConnected() && !e.getMember().getVoiceState().inAudioChannel()) {
			e.reply("i'm not currently in a voice channel").setEphemeral(true).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			return;
		} 
		if (tm == null) {
			tm = new TrackManager(LatiBot.audioPlayer);
			LatiBot.audioPlayer.addListener(tm);
			am.setSendingHandler(new AudioSendingHandler(LatiBot.audioPlayer));
		}
		try {
			tm.queueNow(new WavAudioTrack(
					new com.sedmelluq.discord.lavaplayer.track.AudioTrackInfo("TTS", "DECtalk", 1, "", false, ""),
					new NonSeekableInputStream(new FileInputStream(new File("tts/"+uuid.toString()+".wav")))), e.getMember());
			e.reply("ok playing tts").setEphemeral(true).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(5, TimeUnit.SECONDS));
		} catch (FileNotFoundException e1) {
			e.reply("Failed to play tts").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			e1.printStackTrace();
		}
	}
	
	
	//======== Helpers ========
	
	public static void shutdownTTS() {
		if (dectalk != null) {
			dectalk.ttsShutdown();
		}
	}
}
