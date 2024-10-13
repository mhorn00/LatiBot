package latibot.command.comands.misc;

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
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.MessageReaction;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.CustomEmoji;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class EmoteStatsCmd extends BaseCommand {

    public EmoteStatsCmd() {
        super("emotestats", "Calculates emote usage statistics for the server. Will take a long time most likely.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
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

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash(name, description)
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        	.addOptions(new OptionData(OptionType.INTEGER, "cutoff", "Cutoff for how many times an emote needs to be counted to be displayed in the output.", true)
            .setRequiredRange(0, Integer.MAX_VALUE));
    }

	@Override
	public SlashCommandData buildCommand(String alias) {
        return Commands.slash(alias, description)
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        	.addOptions(new OptionData(OptionType.INTEGER, "cutoff", "Cutoff for how many times an emote needs to be counted to be displayed in the output.", true)
            .setRequiredRange(0, Integer.MAX_VALUE));
    }

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
