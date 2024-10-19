package latibot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

public class MessageListener extends ListenerAdapter {

    private final String urlRegex = ".*(https?://(www\\.)?\\w+\\.com).*";
    private static final HashMap<String, String> domains = new HashMap<>();

    public static HashMap<String, String> getDomains() {
        return domains;
    }

    static {
        try {
            String[] urlReplacements = new String((MessageListener.class.getClassLoader().getResourceAsStream("UrlReplacements.txt")).readAllBytes()).split("\n");
            urlReplacements[0] = urlReplacements[0].substring(0, urlReplacements[0].length() - 1);
            for (String s : urlReplacements) {
                domains.put(s.substring(0, s.indexOf(":")), s.substring(s.indexOf(":") + 1).strip());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.isBot()) return;
        Message message = event.getMessage();
        String content = message.getContentRaw();
        if(content.contains("riggbot are you alive")) message.getChannel().sendMessage("im alive!").queue();
        //checks for any link
        if (content.contains("https://")) {
            boolean isReplaced = false;
            for (String domain : domains.keySet()) {
                if (content.contains(domain)) {
                    content = content.replaceAll("/(www\\.)?" + domain, "/" + domains.get(domain)) + "\n";
                    isReplaced = true;
                }
            }
            if (isReplaced) {
                message.reply(content).setSuppressedNotifications(true).queue();
                message.suppressEmbeds(true).queue();
            }
        }
    }

    public static boolean saveUrlReplacements() {
        String filePath = "src/main/resources/UrlReplacements.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String domain : domains.keySet()) {
                writer.write(domain + ":" + domains.get(domain));
                writer.newLine();
            }
            return true;
        } catch (IOException e) {
            System.err.println("Error writing to file: " + e.getMessage());
            return false;
        }
    }
}