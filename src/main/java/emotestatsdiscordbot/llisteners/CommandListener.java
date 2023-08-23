package emotestatsdiscordbot.llisteners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
		case "stats":
			statsCmd(e);
			break;
		case "sort":
			sortCmd(e);
			break;
		}
	}

	private void pingCmd(SlashCommandInteractionEvent e) {
		long time = System.currentTimeMillis();
		e.reply("Pong!").setEphemeral(true).flatMap(v -> e.getHook().editOriginalFormat("Pong! (%d ms)", System.currentTimeMillis() - time)).queue();
	}

	private void statsCmd(SlashCommandInteractionEvent e) {
		e.reply("ok this might take a while").queue();
		Map<String, Integer> emotes = new HashMap<String, Integer>();
		getMsgs(e.getGuild().getTextChannels(), e.getJDA().getRateLimitPool()).thenAcceptAsync(allMsgs -> {
			for (Map.Entry<TextChannel, List<Message>> c : allMsgs.entrySet()) {
				System.out.println("Channel " + c.getKey().getName() + " has " + c.getValue().size() + " messages");
				for (Message m : c.getValue()) {
					List<MessageReaction> reacts = m.getReactions();
					if (!reacts.isEmpty()) {
						reacts.forEach(r -> {
							Emoji re = r.getEmoji();
							if (re.getType().equals(Emoji.Type.CUSTOM)) { emotes.put(re.getFormatted(), emotes.get(re.getFormatted()) == null ? 1 : emotes.get(re.getFormatted()) + 1); }
						});
					}
					List<CustomEmoji> me = m.getMentions().getCustomEmojis();
					if (!me.isEmpty()) { me.forEach(emote -> { emotes.put(emote.getFormatted(), emotes.get(emote.getFormatted()) == null ? 1 : emotes.get(emote.getFormatted()) + 1); }); }
				}
			}
			String out[] = new String[100];
			out[0] = "Ok here are the stats (that are unordered bc i dont want to sort them)\n";
			int i = 1;
			for (Map.Entry<String, Integer> es : emotes.entrySet()) {
				System.out.println(es.getKey() + " => " + es.getValue());
				// if (es.getValue() > 4) {
				String a = es.getKey() + " => " + es.getValue() + "\n";
				if (out[i] == null) out[i] = "";
				if (out[i].length() + a.length() > 2000) i++;
				out[i] = out[i] == null ? a : out[i] + a;
				// }
			}
			for (String s : out)
				if (s != null && !s.isEmpty() && !s.isBlank())
					e.getChannel().sendMessage(s).queue();
			System.out.println("DONE!");
		});
	}

	private CompletableFuture<ConcurrentMap<TextChannel, List<Message>>> getMsgs(List<TextChannel> channels, ScheduledExecutorService ex) {
		return CompletableFuture.supplyAsync(() -> {
			ConcurrentMap<TextChannel, List<Message>> allMsgs = new ConcurrentHashMap<TextChannel, List<Message>>();
			List<CompletableFuture<List<Message>>> promises = new ArrayList<>();
			for (TextChannel t : channels) {
				if (!t.getName().equals("latibot"))
					continue;
				String firstId = MessageHistory.getHistoryFromBeginning(t).limit(1).complete().getRetrievedHistory().get(0).getId();
				System.out.println("Retived first id for " + t.getName());
				promises.add(t.getIterableHistory().takeUntilAsync(0, m -> m.getId().equals(firstId)).whenCompleteAsync((msgs, err) -> {
					if (err != null) {
						System.err.println("Error getting messages in channel " + t.getName());
						err.printStackTrace();
					} else {
						if (msgs != null) {
							System.out.println(t.getName() + " done!");
							allMsgs.put(t, msgs);
						} else {
							System.err.println("this shouldnt happen!!");
						}
					}
				}, ex));
			}
			try {
				return CompletableFuture.allOf(promises.toArray(new CompletableFuture[0])).thenApplyAsync(v -> allMsgs, ex).exceptionally(e -> {
					System.err.println("Error in all promise!");
					e.printStackTrace();
					return allMsgs;
				}).join();
			} catch (CompletionException | CancellationException e) {
				System.err.println("Error on join!");
				e.printStackTrace();
				return allMsgs;
			}
		});
	}

	private void sortCmd(SlashCommandInteractionEvent e) {
		BufferedReader msgs = new BufferedReader(new InputStreamReader(this.getClass().getClassLoader().getResourceAsStream("message.txt")));
		ArrayList<EmoteStat> emotes = new ArrayList<EmoteStat>();
		String line;
		try {
			while ((line = msgs.readLine()) != null) {
				String[] vk = line.split(" => ");
				emotes.add(new EmoteStat(Integer.parseInt(vk[1]), vk[0]));
			}
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		e.reply("ok").queue();
		Collections.sort(emotes);
		String out[] = new String[100];
		out[0] = "Ok here are the stats (that are ordered now!!!!)\n";
		int i = 1;
		for (EmoteStat es : emotes) {
			System.out.println(es.emote + " => " + es.count);
			if (es.count > 4) {
				String a = es.emote + " => " + es.count + "\n";
				if (out[i] == null)
					out[i] = "";
				if (out[i].length() + a.length() > 2000)
					i++;
				out[i] = out[i] == null ? a : out[i] + a;
			}
		}
		for (String s : out)
			if (s != null && !s.isEmpty() && !s.isBlank())
				e.getChannel().sendMessage(s).queue();
	}

	private static class EmoteStat implements Comparable<EmoteStat> {
		public int count;
		public String emote;

		@Override
		public int compareTo(EmoteStat o) {
			return o.count - count;
		}

		public EmoteStat(int c, String e) {
			count = c;
			emote = e;
		}
	}
}
