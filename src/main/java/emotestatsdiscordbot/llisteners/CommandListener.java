package emotestatsdiscordbot.llisteners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
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
import net.dv8tion.jda.api.requests.restaction.pagination.MessagePaginationAction;

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
	    e.getHook().editOriginal("done!").queue();
	    for (String s : out) if (s != null && !s.isEmpty() && !s.isBlank()) e.getChannel().sendMessage(s).queue();
	    System.out.println("DONE!");
	}
	
	private ConcurrentMap<TextChannel, List<Message>> getMsgs(List<TextChannel> channels) {
		ConcurrentMap<TextChannel, List<Message>> allMsgs = new ConcurrentHashMap<TextChannel, List<Message>>();
		ExecutorService ex = Executors.newFixedThreadPool(channels.size());
		List<CompletableFuture<Void>> promises = new ArrayList<>();
		for (TextChannel t : channels) {
			promises.add(CompletableFuture.runAsync(() -> {
				Thread.currentThread().setName(t.getName());
				String firstId = MessageHistory.getHistoryFromBeginning(t).limit(1).complete().getRetrievedHistory().get(0).getId();
				System.out.println("Retived first id for "+t.getName());
				MessagePaginationAction iterableHist = t.getIterableHistory();
				List<Message> msgs = new ArrayList<Message>();
				boolean done = false;
				while (!done) {
					try {
						msgs.addAll(iterableHist.takeUntilAsync(m -> m.getId().equals(firstId)).join());
						done = true;
						System.out.println("Got all msgs for "+t.getName());
					} catch (CancellationException | CompletionException e) {
						System.err.println("CAUGHT EXCEPTION IN THREAD "+Thread.currentThread().getName());
						System.err.println(e.getMessage());
						e.printStackTrace();
					}
				}
				allMsgs.put(t, null);
			}, ex));			
		}
		try {
	        CompletableFuture<Void> all = CompletableFuture.allOf(promises.toArray(new CompletableFuture[0]));
	        all.join();
	    } catch (CancellationException | CompletionException eex) {
	    	System.err.println("CAUGHT EXCPETION IN MAIN");
	    	System.err.println(eex.getMessage());
	        eex.printStackTrace();
	    } finally {
	    	ex.shutdown();	
		}
	    return allMsgs;
	}	
}
