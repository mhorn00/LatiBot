package latibot.llisteners;

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
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

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
			for (Map.Entry<TextChannel, List<Message>> c : allMsgs.entrySet()) {
				LatiBot.LOG.info("Channel " + c.getKey().getName() + " has " + c.getValue().size() + " messages");
				for (Message m : c.getValue()) {
					List<MessageReaction> msgReactions = m.getReactions();
					if (!msgReactions.isEmpty()) {
						msgReactions.forEach(r -> {
							if (r.getEmoji().getType().equals(Emoji.Type.CUSTOM)) { 
								CustomEmoji ce = (CustomEmoji)r.getEmoji();
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
			}
			List<EmoteStat> emotestats = new ArrayList<EmoteStat>(emotes.values());
			Collections.sort(emotestats);
			List<String> out = new ArrayList<String>();
			out.add("Here are the emote stats:\n");
			for (int i = 0, j = 0; i < emotestats.size(); i++) {
				EmoteStat cur = emotestats.get(i);
				if (cur.getCount() >= cutoff) {
					String add = cur.getEmote().getAsMention() + " " +cur.getCount()+", ";
					if (out.get(j) == null) out.add("");
					if (out.get(j).length()+add.length() > 2000) j++;
					out.set(j, out.get(j)+add);
				}
			}
			out.forEach(s -> e.getChannel().sendMessage(s).queue());
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
