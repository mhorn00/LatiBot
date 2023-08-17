package emotestatsdiscordbot.llisteners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {

	@Override
	 public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		switch (e.getName()) {
		case "ping": pingCmd(e); break;
		case "stats": statsCmd(e); break;
		}
	}
	
	private void pingCmd(SlashCommandInteractionEvent e) {
		long time = System.currentTimeMillis();
		e.reply("Pong!").setEphemeral(true).flatMap(v -> e.getHook().editOriginalFormat("Pong! (%d ms)", System.currentTimeMillis() - time)).queue();;
	}
	
	private void statsCmd(SlashCommandInteractionEvent e) {
		e.deferReply().queue();
		Map<String, Integer> emotes = new HashMap<String, Integer>();
		ConcurrentMap<TextChannel, List<Message>> allMsgs = getMsgs(e.getGuild().getTextChannels());
		for (Map.Entry<TextChannel, List<Message>> c : allMsgs.entrySet()) {
			System.out.println("Channel "+c.getKey().getName()+" has "+c.getValue().size()+" messages");
			for (Message m : c.getValue()) {
				Matcher matcher = Pattern.compile("<:([^:]+):[0-9]+>").matcher(m.getContentRaw());
		        while (matcher.find()) {
		        	emotes.put(matcher.group(0), emotes.get(matcher.group(0))==null?1:emotes.get(matcher.group(0))+1);
		        }	
			}
		}
		String out[] = new String[100];
	    out[0] = "Ok here are the stats (that are unordered bc i dont want to sort them)\n";
	    int i = 1;
	    for (Map.Entry<String, Integer> es : emotes.entrySet()) {
	    	if (es.getValue() > 4) {
	    		String a = es.getKey() + " => " + es.getValue() + "\n";
	    		if (out[i] == null) out[i] = "";
	    		if (out[i].length() + a.length() > 2000) i++;
	    		out[i] += a;
	    	}
	    }
	    System.out.println(out);
	    e.getHook().editOriginal("done!").queue();
	    for (String s : out) if (!s.isEmpty() && !s.isBlank()) e.getChannel().sendMessage(s).queue();
	    System.out.println("DONE!");
	}
	
	private ConcurrentMap<TextChannel, List<Message>> getMsgs(List<TextChannel> channels) {
		ConcurrentMap<TextChannel, List<Message>> allMsgs = new ConcurrentHashMap<TextChannel, List<Message>>();
		ExecutorService ex = Executors.newFixedThreadPool(channels.size());
		List<CompletableFuture<Void>> promises = new ArrayList<>();
		for (TextChannel t : channels) {
			promises.add(CompletableFuture.runAsync(() -> {
				String firstId = MessageHistory.getHistoryFromBeginning(t).limit(1).complete().getRetrievedHistory().get(0).getId();
				System.out.println("Retived first id for "+t.getName());
				try {
					allMsgs.put(t, t.getIterableHistory().takeUntilAsync(m -> {return m.getId().equals(firstId);}).get());
					System.out.println("Got all msgs for "+t.getName());
				} catch (InterruptedException | ExecutionException e) {
					e.printStackTrace();
				}
			}, ex));			
		}
		try {
	        CompletableFuture.allOf(promises.toArray(new CompletableFuture[0])).get();
	    } catch (InterruptedException | ExecutionException eex) {
	        eex.printStackTrace();
	    }
	    ex.shutdown();
		return allMsgs;
	}	
	
	private void statsCmd2(SlashCommandInteractionEvent e) {
		List<TextChannel> channels = e.getGuild().getTextChannels();
		ConcurrentHashMap<String, Integer> emotes = new ConcurrentHashMap<String, Integer>();
		e.deferReply().queue();
		ExecutorService ex = Executors.newFixedThreadPool(channels.size());
		List<CompletableFuture<Void>> promise = new ArrayList<>();
		for (TextChannel tc : channels) {
			CompletableFuture<Void> p = CompletableFuture.runAsync(() -> {
				MessageHistory mh =  tc.getHistory();
				int c = 0;
				while (true) {
					List<Message> msgs = mh.retrievePast(100).complete();
					System.out.println(tc.getName() + "=>" + c);
					if (msgs.isEmpty()) break;
					c += msgs.size();
					for (Message msg : msgs) {
						Matcher matcher = Pattern.compile("<:([^:]+):[0-9]+>").matcher(msg.getContentRaw());
				        while (matcher.find()) {
				        	emotes.put(matcher.group(0), emotes.get(matcher.group(0))==null?1:emotes.get(matcher.group(0))+1);
				        }
					}
					//if (c > 10000) break;
					if (msgs.size() < 100) break;
				}
				System.out.println(tc.getName() + " Done!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
			}, ex);
			promise.add(p);
		}
		CompletableFuture<Void> allOf = CompletableFuture.allOf(promise.toArray(new CompletableFuture[0]));
	    try {
	        allOf.get();
	    } catch (InterruptedException | ExecutionException eex) {
	        eex.printStackTrace();
	    }
	    ex.shutdown();
	    
	    String out[] = new String[100];
	    out[0] = "Ok here are the stats (that are unordered bc i dont want to sort them <:shy:934137983228067930>)\n";
	    int i = 1;
	    for (Map.Entry<String, Integer> es : emotes.entrySet()) {
	    	if (es.getValue() > 4) {
	    		String a = es.getKey() + " => " + es.getValue() + "\n";
	    		if (out[i] == null) out[i] = "";
	    		if (out[i].length() + a.length() > 2000) i++;
	    		out[i] += a;
	    	}
	    }
	    System.out.println(out);
	    e.getHook().editOriginal("done!").queue();
	    for (String s : out) if (!s.isEmpty() && !s.isBlank()) e.getChannel().sendMessage(s).queue();
	    System.out.println("DONE!");
	}
}
