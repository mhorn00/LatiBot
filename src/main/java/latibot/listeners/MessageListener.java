package latibot.listeners;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import latibot.LatiBot;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.PermissionException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListener extends ListenerAdapter {

    // this thing is getting really stupid and so am i, but it does seem
    // to consistently work even if its probably overdone. also chatgpt
    // can't seem to figure out how to suggest any changes to it that wont
    // also break it, but honestly i dont really blame it
    private static final Pattern urlRegex = Pattern.compile(
            "(?<before>.*)(?<fullLink>https?://(www\\.)?(?<domain>[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6})\\b[-a-zA-Z0-9()@:%_+.~#&/=]+)[-a-zA-Z0-9()@:%_+.~#&?/=]*(?<after>.*)");
    private static final HashMap<String, String> domains = new HashMap<>();

    public static HashMap<String, String> getDomains() {
        return domains;
    }

    public static List<Long> userBlacklist = new ArrayList<>();

    static {
        // Loading URL Replacements
        try (Stream<String> urlReplacements = Files.lines(Path.of("UrlReplacements.txt"))) {
            urlReplacements.map(line -> line.split("\\|"))
                    .forEach(parts -> domains.put(parts[0], parts[1]));
        } catch (IOException e) {
            LatiBot.LOG.error("Error reading UrlReplacements.txt", e);
            throw new RuntimeException(e);
        }
    }

    @Override
    public void onMessageReceived(MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return; // must be a user message
        Member member = event.getMember();
        Message message = event.getMessage();
        String content = message.getContentRaw();

        // 420 & 69
        if (content.matches(".*\\b4:?20\\b.*") || content.matches(".*\\b69\\b.*")) {
            event.getChannel().sendMessage("nice").setSuppressedNotifications(true).queue();
            return;
        }

        // check blacklist
        if (userBlacklist.contains(member.getIdLong())) return;

        // Link detection and url replacement
        Matcher matcher = urlRegex.matcher(content);
        StringBuilder reply = new StringBuilder();
        while (matcher.find()) {
            String domain = matcher.group("domain");
            String replacement = domains.get(domain);
            if (replacement != null) {
                reply.append(matcher.group("before"))
                        .append("<").append(matcher.group("fullLink")).append("> [.](")
                        .append(matcher.group("fullLink").replace(domain, replacement))
                        .append(")").append(matcher.group("after"));
            }
        }
        // if msg has content
        if (!reply.isEmpty()) {
            try {
                // create webhook
                Webhook webhook = message.getChannel().asTextChannel().createWebhook("Latibot Url Replacer").complete();
                
                // send the msg
                webhook.sendMessage(reply.toString())
                        .setUsername(member.getEffectiveName())
                        .setAvatarUrl(member.getEffectiveAvatarUrl())
                        .setSuppressedNotifications(true)
                        .queue();

                message.delete().queue();
                webhook.delete().queue();
            } catch (PermissionException pe) {
                LatiBot.LOG.info("Missing MANAGE_WEBHOOKS permission in channel: {}", message.getChannel());
                message.getChannel()
                        .sendMessage("bro i tried but my mom said no (im missing the manage webhooks perm in here)")
                        .queue();
            }
        }
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
            LatiBot.LOG.error("Error writing to UrlReplacements.txt", e);
            return false;
        }
    }
}