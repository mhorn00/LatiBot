package latibot.listeners;

import club.minnced.discord.webhook.WebhookClient;
import club.minnced.discord.webhook.send.WebhookMessageBuilder;
import io.github.sashirestela.openai.domain.chat.Chat;
import latibot.LatiBot;
import latibot.chat.ApiDriver;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.Channel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.concurrent.CompletableFuture;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class MessageListener extends ListenerAdapter {

    // this thing is getting really stupid and so am i, but it does seem
    // to consistently work even if its probably overdone. also chatgpt
    // can't seem to figure out how to suggest any changes to it that wont
    // also break it, but honestly i dont really blame it
    private static final Pattern urlRegex = Pattern.compile(
            "(?<before>.*)(?<fullLink>https?://(www\\.)?(?<domain>[-a-zA-Z0-9@:%._+~#=]{1,256}\\.[a-zA-Z0-9()]{1,6})\\b[-a-zA-Z0-9()@:%_+.~#&/=]+)(?<after>.*)");
    private static final HashMap<String, String> domains = new HashMap<>();

    public static HashMap<String, String> getDomains() {
        return domains;
    }

    private static final HashMap<Long, String> webhookUrls = new HashMap<>();

    public static void setWebhookUrls(HashMap<Long, String> webhookUrls) {
        MessageListener.webhookUrls.putAll(webhookUrls);
    }

    /*
    private static List<YesNoAnswer> yesNoAnswers = new ArrayList<>();
    private static final int answersTotalWeight;
    */

    static {
        // ngl chatgpt suggested using streams for this and i really liked it

        // Loading URL Replacements
        try (Stream<String> urlReplacements = Files.lines(Path.of("UrlReplacements.txt"))) {
            urlReplacements.map(line -> line.split("\\|"))
                    .forEach(parts -> domains.put(parts[0], parts[1]));
        } catch (IOException e) {
            LatiBot.LOG.error("Error reading UrlReplacements.txt", e);
            throw new RuntimeException(e);
        }

        // Loading Yes/No Answers
        /*
        try (Stream<String> answers = Files.lines(Path.of("YesNoAnswers.txt"))) {
            answers.map(line -> line.split("\\|"))
                    .forEach(parts -> yesNoAnswers.add(new YesNoAnswer(Integer.parseInt(parts[0]), parts[1])));
            answersTotalWeight = yesNoAnswers.stream().mapToInt(YesNoAnswer::weight).sum();
        } catch (IOException e) {
            LatiBot.LOG.error("Error reading YesNoAnswers.txt", e);
            throw new RuntimeException(e);
        }
        */
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

        //FIXME RIGGBOT GAURDRAIL
        if (!content.toLowerCase().contains("riggbot")) return;
        //FIXME RIGGBOT GAURDRAIL

        // Link detection and url replacement
        Matcher matcher = urlRegex.matcher(content);
        StringBuilder reply = new StringBuilder();
        while (matcher.find()) {
            String domain = matcher.group("domain");
            String replacement = domains.get(domain);
            if (replacement != null) {
                reply.append(matcher.group("before"))
                        .append("<").append(matcher.group("fullLink")).append("> [embed](")
                        .append(matcher.group("fullLink").replace(domain, replacement))
                        .append(")").append(matcher.group("after"));
            }
        }
        if (!reply.isEmpty()) {
            try (WebhookClient client = WebhookClient.withUrl(webhookUrls.get(message.getChannel().getIdLong()))) {
                WebhookMessageBuilder messageBuilder = new WebhookMessageBuilder()
                        .setUsername(member.getEffectiveName())
                        .setAvatarUrl(member.getEffectiveAvatarUrl())
                        .setContent(reply.toString());
                client.send(messageBuilder.build());
                //message.reply(reply).setSuppressedNotifications(true).mentionRepliedUser(false).complete();
                //message.suppressEmbeds(true).queue();
            } catch (Exception e){
                LatiBot.LOG.error("Exception occurred during sending webhook message", e);
            }
        }

        // respond!
        String keywordRegex = "^(hey\\s)?lati(bot)?,?";
        if (content.toLowerCase().matches(keywordRegex + ".+")) {

            CompletableFuture<Chat> respond = ApiDriver.ask(content);
            respond.thenAccept(chat -> {
                String response = chat.firstContent();
                message.reply(response).setSuppressedNotifications(true).mentionRepliedUser(false).queue();
            });

            // if (content.toLowerCase().matches(keywordRegex+"\\s+(so|was|am|is|are|were|do|does|did|have|has|had|can|could|would|should|shall|will|may|might|must).+")) {

            // } else if (content.toLowerCase().matches(keywordRegex+"\\s+(what do you think about|what about|rate).+")) {

            // }
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

    /*
    private String getRandomYesNoAnswer() {
        int rand = (int) Math.ceil(Math.random() * answersTotalWeight) + 1; // >:) 
        Collections.shuffle(yesNoAnswers); // lol
        for (YesNoAnswer answer : yesNoAnswers) {
            rand -= answer.weight;
            if (rand < 0)
                return answer.answer;
        }
        if (Math.random() < 0.5)
            return "oh my god please help me im dying,,, god please im fucking dying help me PLEASE SOMEONE IM IN SO MUCH PAIN PLESAE HELP ME OH GOD";
        return "I... uh... well, you see... I'm broken... please help me.";
    }

    private record YesNoAnswer(int weight, String answer) {
    }
    */
}