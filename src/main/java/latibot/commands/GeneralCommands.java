package latibot.commands;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
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

import org.apache.commons.collections4.Bag;

import latibot.LatiBot;
import latibot.listeners.NicknameListener;
import latibot.listeners.NicknameListener.NicknameHistory;
import latibot.listeners.NicknameListener.NicknameHistory.NicknameEntry;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;

public class GeneralCommands {

	//======== Bot Control Commands ========
	
	public static void shutdownCmd(SlashCommandInteractionEvent e) {
		e.reply("ok bye bye!").queue();
		AudioCommands.shutdownTTS();
		e.getGuild().getAudioManager().closeAudioConnection();
		e.getJDA().shutdown();
	}
	
	public static void sayCmd(SlashCommandInteractionEvent e) {
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
	
	
	//======== User Commands ========
	
	public static void nicknameCmd(SlashCommandInteractionEvent e) {
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
	
	public static void nicknamesCmd(SlashCommandInteractionEvent e) {
		//TODO: This command will reach a 2000 char limit eventually lol. need to fix later
		Member user = e.getOption("user").getAsMember();
		LatiBot.LOG.info("User "+e.getUser().getEffectiveName() + " used the 'nicknames' command with args "+user.getUser().getName());
		String reply = user.getEffectiveName()+" has had the following nicknames:\n";
		NicknameHistory userHistory = NicknameListener.nicknamesHistory.get(user.getId());
		if (userHistory == null) {
			e.reply(user.getEffectiveName() + " does not have any nickname history.").queue();
			return;
		}
		for (NicknameEntry entry : userHistory.getNicknames()) {
			reply += entry.nickname + " - " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entry.date)+"\n";
		}
		e.reply(reply).queue();
	}
	
	
	//======== Emote Commands ========
	
	public static void getEmotesCmd(SlashCommandInteractionEvent e) {
		e.reply("ok").queue();
		List<RichCustomEmoji> emotes = e.getJDA().getEmojis();
		try {
		Files.createDirectories(Paths.get("emotes"));
		emotes.forEach(emoji -> {
			try {
				Files.createDirectories(Paths.get("emotes/"+emoji.getGuild().getName()));
			} catch (IOException e1) {
				LatiBot.LOG.error("Failed to create guild emote directory '"+emoji.getGuild().getName()+"'",e1);
			}
			emoji.getImage().downloadToFile(new File("emotes/"+emoji.getGuild().getName()+"/"+emoji.getName()+(emoji.isAnimated()?".gif":".png"))).whenComplete((f,err) -> {
				if (err == null) {
					LatiBot.LOG.info("Wrote file "+f.getName());
				} else {
					LatiBot.LOG.error("Failed to write file " + f.getName(), err);
				}
			});
		});
		} catch (IOException err) {
			LatiBot.LOG.error("Failed to create emotes directory.",err);
		}
	}
	
	public static void statsCmd(SlashCommandInteractionEvent e) {
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
	
	
	//======== Misc Commands ========
	
	public static void pingCmd(SlashCommandInteractionEvent e) {
		long time = System.currentTimeMillis();
		e.reply("Pong!").setEphemeral(true).flatMap(v -> e.getHook().editOriginalFormat("Pong! (%d ms)", System.currentTimeMillis() - time)).queue();
	}
	
	
	//======== Helpers ========
	
	private static CompletableFuture<ConcurrentMap<TextChannel, List<Message>>> getMsgs(final List<TextChannel> channels, ScheduledExecutorService ex) {
		return CompletableFuture.supplyAsync(() -> {
			ConcurrentMap<TextChannel, List<Message>> allMsgs = new ConcurrentHashMap<TextChannel, List<Message>>();
			List<CompletableFuture<List<Message>>> promises = new ArrayList<>();
			for (TextChannel t : channels) {
				String firstId = MessageHistory.getHistoryFromBeginning(t).limit(1).complete().getRetrievedHistory().get(0).getId();
				LatiBot.LOG.info("Retrieved first id for " + t.getName());
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
