package latibot.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections4.Bag;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;

import latibot.LatiBot;
import latibot.audio.AudioSendingHandler;
import latibot.audio.AudioTrackInfo;
import latibot.audio.TrackManager;
import latibot.audio.TrackManager.SongQueue;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.managers.AudioManager;

public class CommandListener extends ListenerAdapter {
	
	private TrackManager tm = null;
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		switch (e.getName()) {
		case "ping":
			pingCmd(e);
			break;
		case "emotestats":
			statsCmd(e);
			break;
		case "nickname":
			nicknameCmd(e);
			break;
		case "shutdown":
			shutdownCmd(e);
			break;
		case "say":
			sayCmd(e);
			break;
		case "q":
		case "queue":
			queueCmd(e);
			break;
		case "play":
			playCmd(e);
			break;
		case "playnow":
			playNowCmd(e);
			break;
		case "playnext":
			playNextCmd(e);
			break;
		case "playsilent":
			playSilentCmd(e);
			break;
		case "skip":
			skipCmd(e);
			break;
		case "join":
			joinCmd(e);
			break;
		case "leave":
			leaveCmd(e);
			break;
		case "pause":
			pauseCmd(e);
			break;
		case "shuffle":
			shuffleCmd(e);
			break;
		case "repeat":
			repeatCmd(e);
			break;
		case "clear":
			clearCmd(e);
			break;
		}
	}

	private void clearCmd(SlashCommandInteractionEvent e) {
		if (tm == null) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else if (tm.getQueue().isQueueEmpty()) {
			e.reply("the queue is already empty!").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			e.reply("queue cleared!").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			tm.clearQueue();
		}
	}

	private void repeatCmd(SlashCommandInteractionEvent e) {
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

	private void shuffleCmd(SlashCommandInteractionEvent e) {
		if (tm == null) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else if (tm.getQueue().isQueueEmpty()) {
			e.reply("the queue is currently empty").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			e.reply("ok queue shuffled").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			tm.shuffleQueue();
		}
	}

	private void pauseCmd(SlashCommandInteractionEvent e) {
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

	private void skipCmd(SlashCommandInteractionEvent e) {
		if (tm == null) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else if (tm.getQueue().getCurrent() == null) {
			e.reply("there isn't anything playing").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			e.reply("skipping...").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			tm.skip();
		}
	}

	private void playSilentCmd(SlashCommandInteractionEvent e) {
		String link = e.getOption("link").getAsString();
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
		LatiBot.audioPlayerManager.loadItemOrdered(LatiBot.audioPlayer, link, new AudioLoadResultHandler() {
			@Override
			public void trackLoaded(AudioTrack track) {
				e.reply("ok playing track "+track.getInfo().title).setEphemeral(true).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
				tm.queueNext(track, e.getMember());
			}
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				e.reply("ok queuing playlist next"+playlist.getName()).setEphemeral(true).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));;
				for (AudioTrack track : playlist.getTracks()) {
					tm.queueNext(track, e.getMember());
				}
			}
			@Override
			public void noMatches() {
				e.reply("no track found").setEphemeral(true).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
			@Override
			public void loadFailed(FriendlyException exception) {
				e.reply("exception occured! check logs").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(60, TimeUnit.SECONDS));
				LatiBot.LOG.warn("Exception occured while trying to play track "+link+": ", exception);
			}
		});
	}

	private void playNextCmd(SlashCommandInteractionEvent e) {
		String link = e.getOption("link").getAsString();
		AudioManager am = e.getGuild().getAudioManager();
		if (!am.isConnected() && e.getMember().getVoiceState().inAudioChannel()) {
			am.openAudioConnection(e.getMember().getVoiceState().getChannel().asVoiceChannel()); //this will fail if its a stage channel lol
		} else if (!am.isConnected() && !e.getMember().getVoiceState().inAudioChannel()) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
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
				e.reply("ok playing track next "+track.getInfo().title).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
				tm.queueNext(track, e.getMember());
			}
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				e.reply("ok queuing playlist next"+playlist.getName()).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));;
				for (AudioTrack track : playlist.getTracks()) {
					tm.queueNext(track, e.getMember());
				}
			}
			@Override
			public void noMatches() {
				e.reply("no track found").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
			@Override
			public void loadFailed(FriendlyException exception) {
				e.reply("exception occured! check logs").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(60, TimeUnit.SECONDS));
				LatiBot.LOG.warn("Exception occured while trying to play track "+link+": ", exception);
			}
		});
	}
	
	private void playNowCmd(SlashCommandInteractionEvent e) {
		String link = e.getOption("link").getAsString();
		AudioManager am = e.getGuild().getAudioManager();
		if (!am.isConnected() && e.getMember().getVoiceState().inAudioChannel()) {
			am.openAudioConnection(e.getMember().getVoiceState().getChannel().asVoiceChannel()); //this will fail if its a stage channel lol
		} else if (!am.isConnected() && !e.getMember().getVoiceState().inAudioChannel()) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
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
				e.reply("ok playing track now "+track.getInfo().title).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
				tm.queueNow(track, e.getMember());
			}
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				e.reply("don't queue a playlist with this command lol").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
			@Override
			public void noMatches() {
				e.reply("no track found").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
			@Override
			public void loadFailed(FriendlyException exception) {
				e.reply("exception occured! check logs").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(60, TimeUnit.SECONDS));
				LatiBot.LOG.warn("Exception occured while trying to play track "+link+": ", exception);
			}
		});
	}
	
	private void playCmd(SlashCommandInteractionEvent e) {
		String link = e.getOption("link").getAsString();
		AudioManager am = e.getGuild().getAudioManager();
		if (!am.isConnected() && e.getMember().getVoiceState().inAudioChannel()) {
			am.openAudioConnection(e.getMember().getVoiceState().getChannel().asVoiceChannel()); //this will fail if its a stage channel lol
		} else if (!am.isConnected() && !e.getMember().getVoiceState().inAudioChannel()) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
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
				e.reply("ok playing track "+track.getInfo().title).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
				tm.queue(track, e.getMember());
			}
			@Override
			public void playlistLoaded(AudioPlaylist playlist) {
				e.reply("ok queuing playlist "+playlist.getName()).setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
				for (AudioTrack track : playlist.getTracks()) {
					tm.queue(track, e.getMember());
				}
			}
			@Override
			public void noMatches() {
				e.reply("no track found").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
			@Override
			public void loadFailed(FriendlyException exception) {
				e.reply("exception occured! check logs").setSuppressedNotifications(true).queue(hook -> hook.deleteOriginal().queueAfter(60, TimeUnit.SECONDS));
				LatiBot.LOG.warn("Exception occured while trying to play track "+link+": ", exception);
			}
		});
	}

	private void queueCmd(SlashCommandInteractionEvent e) {
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

	private void leaveCmd(SlashCommandInteractionEvent e) {
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

	private void joinCmd(SlashCommandInteractionEvent e) {//TODO: add arg to specify channel name/id?
		if (e.getMember().getVoiceState().inAudioChannel()) {
			VoiceChannel vc = e.getMember().getVoiceState().getChannel().asVoiceChannel();
			AudioManager am = e.getGuild().getAudioManager();
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

	private void sayCmd(SlashCommandInteractionEvent e) {
		String msg = e.getOption("message").getAsString();
		String replyId = e.getOption("reply") != null ? e.getOption("reply").getAsString() : null;
		LatiBot.LOG.info("User "+e.getUser().getEffectiveName() + " used the 'say' command with args '"+msg+"'"+(replyId!=null ? " '"+replyId+"'" : ""));
		if (replyId == null) {
			e.reply("ok").setEphemeral(true).queue();
			e.getChannel().sendMessage(msg).queue();
		} else {
			Message replyMsg = e.getChannel().retrieveMessageById(replyId).complete();
			if (replyMsg != null) {
				e.reply("ok").setEphemeral(true).queue();
				replyMsg.reply(msg).queue();
			} else {
				e.reply("Couldn't find message with ID '"+replyId+"' in channel "+e.getChannel().getName()+"!").setEphemeral(true).queue();
			}
		}
	}

	private void shutdownCmd(SlashCommandInteractionEvent e) {
		e.reply("ok bye bye!").queue();
		e.getGuild().getAudioManager().closeAudioConnection();
		e.getJDA().shutdown();
	}

	private void pingCmd(SlashCommandInteractionEvent e) {
		long time = System.currentTimeMillis();
		e.reply("Pong!").setEphemeral(true).flatMap(v -> e.getHook().editOriginalFormat("Pong! (%d ms)", System.currentTimeMillis() - time)).queue();
	}

	private void statsCmd(SlashCommandInteractionEvent e) {
		LatiBot.LOG.info("User "+e.getUser().getEffectiveName() + " used the 'emotestats' command");
		int cutoff = e.getOption("cutoff").getAsInt();
		e.reply("ok this might take a while").queue();
		Map<String, EmoteStat> emotes = new HashMap<String, EmoteStat>();
		getMsgs(e.getGuild().getTextChannels(), e.getJDA().getRateLimitPool()).thenAcceptAsync(allMsgs -> {
			e.getChannel().sendTyping().queue();
			for (Map.Entry<TextChannel, List<Message>> c : allMsgs.entrySet()) {
				LatiBot.LOG.info("Channel " + c.getKey().getName() + " has " + c.getValue().size() + " messages");
				for (Message m : c.getValue()) {
					List<MessageReaction> msgReactions = m.getReactions();
					if (!msgReactions.isEmpty()) {
						msgReactions.forEach(r -> {
							if (r.getEmoji().getType().equals(Emoji.Type.CUSTOM)) { 
								CustomEmoji ce = (CustomEmoji)r.getEmoji();//try Emoji.fromCustom()?
								if (emotes.containsKey(ce.getId())) {
									emotes.get(ce.getId()).incCount(r.getCount());
								} else {
									emotes.put(ce.getId(), new EmoteStat(ce, r.getCount()));
								}
							}
						});
					}
					Bag<CustomEmoji> msgEmotes = m.getMentions().getCustomEmojisBag();
					if (!msgEmotes.isEmpty()) { 
						msgEmotes.forEach(emote -> {
							if (emotes.containsKey(emote.getId())) {
								emotes.get(emote.getId()).incCount();
							} else {
								emotes.put(emote.getId(), new EmoteStat(emote));
							}
						});
					}
				}
				LatiBot.LOG.info("Channel " + c.getKey().getName() + " done counting!");
			}
			//TODO: this is getting stuck somewhere after this point smh
			List<EmoteStat> emotestats = new ArrayList<EmoteStat>(emotes.values());
			Collections.sort(emotestats);
			String[] out = new String[100];
			out[0] = "Here are the emote stats:\n";
			for (int i = 0, j = 0; i < emotestats.size() && j < 100; i++) {
				EmoteStat cur = emotestats.get(i);
				if (cur.getCount() >= cutoff) {
					String add = cur.getEmote().getAsMention() + " " +cur.getCount()+", ";
					if (out[j].length()+add.length() > 2000) j++;
					if (out[j] == null) out[j] = "";
					out[j] = out[j]+add;
				}
			}
			for (String s : out) {
				e.getChannel().sendMessage(s).queue();
			}
			LatiBot.LOG.info("emotestats command finished");
		});
	}

	private CompletableFuture<ConcurrentMap<TextChannel, List<Message>>> getMsgs(final List<TextChannel> channels, ScheduledExecutorService ex) {
		return CompletableFuture.supplyAsync(() -> {
			ConcurrentMap<TextChannel, List<Message>> allMsgs = new ConcurrentHashMap<TextChannel, List<Message>>();
			List<CompletableFuture<List<Message>>> promises = new ArrayList<>();
			for (TextChannel t : channels) {
				String firstId = MessageHistory.getHistoryFromBeginning(t).limit(1).complete().getRetrievedHistory().get(0).getId();
				LatiBot.LOG.info("Retived first id for " + t.getName());
				promises.add(t.getIterableHistory().takeUntilAsync(0, m -> m.getId().equals(firstId)).whenCompleteAsync((msgs, err) -> {
					if (err != null) {
						LatiBot.LOG.error("Error getting messages in channel " + t.getName()+":", err);
					} else {
						if (msgs != null) {
							allMsgs.put(t, msgs);
							LatiBot.LOG.info(t.getName() + " done!");
						} else {
							LatiBot.LOG.error("Unknown error in getting messages in channel "+t.getName());
						}
					}
				}, ex));
			}
			try {
				return CompletableFuture.allOf(promises.toArray(new CompletableFuture[0])).thenApplyAsync(v -> allMsgs, ex).exceptionally(e -> {
					LatiBot.LOG.error("Error in all promise:", e);
					return allMsgs;
				}).join();
			} catch (CompletionException | CancellationException e) {
				LatiBot.LOG.error("Error in join promise:", e);
				return allMsgs;
			}
		});
	}

	private void nicknameCmd(SlashCommandInteractionEvent e) {
		Member user = e.getOption("user").getAsMember();
		String nickname = e.getOption("nickname").getAsString();
		LatiBot.LOG.info("User "+e.getUser().getEffectiveName() + " used the 'nickname' command with args "+user.getUser().getName()+" "+nickname);
		if (!user.isOwner()) {
			user.modifyNickname(nickname).queue();
			e.reply("Set nickname of "+user.getUser().getName()+" to "+nickname).setEphemeral(true).queue();
		} else {
			e.getGuild().getSystemChannel().sendMessage(e.getUser().getAsMention() + " updated your nickname to '"+nickname+"' "+e.getGuild().getOwner().getAsMention()).queue();
			e.reply("\"Set\" nickname of "+user.getUser().getName()+" to " + nickname).setEphemeral(true).queue();
		}
	}
	
	private static class EmoteStat implements Comparable<EmoteStat> {
		private int count;
		private final CustomEmoji emote;

		public EmoteStat(final CustomEmoji e) {
			this(e,1);
		}
		
		public EmoteStat(final CustomEmoji e, final int c) {
			count = c;
			emote = e;
		}
		
		public CustomEmoji getEmote() {
			return emote;
		}
		
		public int getCount() {
			return count;
		}
		
		public void incCount() {
			incCount(1);
		}
		
		public void incCount(final int c) {
			count += c;
		}
		
		@Override
		public int compareTo(final EmoteStat o) {
			return o.getCount() - this.getCount();
		}
	}
}
