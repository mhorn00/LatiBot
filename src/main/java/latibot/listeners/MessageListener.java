package latibot.listeners;

import latibot.LatiBot;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageListener extends ListenerAdapter {

    private static final Pattern urlRegex = Pattern.compile(
            "(?<fullLink>https?://(www\\.)?((?<domain>[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6})\\b([-a-zA-Z0-9()@:%_+.~#&/=]*)))");

    private static final HashMap<String, String> domains = new HashMap<>();

    public static HashMap<String, String> getDomains() {
        return domains;
    }

    static {
        try {
            String[] urlReplacements = new String(Files.readAllBytes(Path.of("UrlReplacements.txt"))).split("\n");
            // urlReplacements[0] = urlReplacements[0].substring(0, urlReplacements[0].length() - 1);
            for (String s : urlReplacements) {
                domains.put(s.substring(0, s.indexOf("|")),
                        s.substring(s.indexOf("|") + 1).strip());
            }
        } catch (IOException e) {
            LatiBot.LOG.error("Error reading UrlReplacements.txt");
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        User author = event.getAuthor();
        if (author.isBot()) return; //must be a user message
        Message message = event.getMessage();
        String content = message.getContentRaw();

        if(content.matches("\\b4:?20\\b") || content.matches("\\b69\\b")) {
            event.getChannel().sendMessage("nice").setSuppressedNotifications(true).queue();
            return;
        }

        //quick link check
        if (content.contains("https://")) {
            StringBuilder reply = new StringBuilder();
            Matcher matcher = urlRegex.matcher(content);
            //actual link check
            while (matcher.find()) {
                if (domains.get(matcher.group("domain")) != null) {
                    reply.append(matcher.group("fullLink")
                            .replace(matcher.group("domain"), domains.get(matcher.group("domain"))))
                            .append("\n");
                }
            }
            if (!reply.toString().isEmpty()) {
                message.reply(reply).setSuppressedNotifications(true).mentionRepliedUser(false).queue();
                message.suppressEmbeds(true).queue();
            }
        }
        /*
        if (content.toLowerCase().matches("^riggbot (am|is|are|were|do|does|did|have|has|had|can|could|would|should|shall|will|may|might|must).+")) {
            Random random = new Random();
            message.getChannel().sendMessage(yesNoAnswers.get(random.nextInt(yesNoAnswers.size()))).queue();
        }*/
    }

    public static boolean saveUrlReplacements() {
        String filePath = "UrlReplacements.txt";

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath))) {
            for (String domain : domains.keySet()) {
                writer.write(domain + "|" + domains.get(domain));
                writer.newLine();
            }
            LatiBot.LOG.info("Changes were saved to UrlReplacements.txt");
            return true;
        } catch (IOException e) {
            LatiBot.LOG.error("Error writing to file: " + e.getMessage());
            return false;
        }
    }
}