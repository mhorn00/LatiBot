
package latibot.chat;

import java.io.IOException;
import java.util.concurrent.CompletableFuture;

import io.github.sashirestela.openai.SimpleOpenAI;
import io.github.sashirestela.openai.domain.chat.Chat;
import io.github.sashirestela.openai.domain.chat.ChatMessage.SystemMessage;
import io.github.sashirestela.openai.domain.chat.ChatMessage.UserMessage;
import latibot.LatiBot;
import io.github.sashirestela.openai.domain.chat.ChatRequest;

public class ApiDriver {
    private static SimpleOpenAI client;

    public static void init() throws IllegalStateException {
        LatiBot.LOG.info("Initializing OpenAI API");
        String KEY = null;
        try {
            KEY = new String(ApiDriver.class.getClassLoader().getResourceAsStream("openai_key.txt").readAllBytes());
        } catch (IOException e) {
            LatiBot.LOG.error("Error reading openai_key.txt", e);
        }
        if (KEY == null)
            throw new IllegalStateException("API key not found");
        client = SimpleOpenAI.builder().apiKey(KEY).build();
    }

    public static SystemMessage buildSystemMessage() {
        return SystemMessage.of(
                "You are a man a few words. All of your responses are always a few words to 1 short sentence at most. Very concise and to the point, but make sure that you have varied responses and are not repeating 'Yes' or 'No' over and over. You also generally use little punctuation, unless it would change the sentence meaning."
                        + "Most of the time you will be asked yes or no questions. Remember to be concise and to the point with varied responses. "
                        + "You will also be asked what you think about a topic. You are still to keep it to the point but will always give an opinion, regardless of what the topic is. "
                        + "That opinion can also never be 'i do not have an opinion because i am only an ai'. Good luck. ");
    }

    public static CompletableFuture<Chat> ask(String msg) {
        return client.chatCompletions().create(
                ChatRequest.builder()
                        .model("gpt-4o")
                        .message(buildSystemMessage())
                        .message(UserMessage.of(msg))
                        .maxCompletionTokens(100)
                        .temperature(1.5)
                        .build());
    }
}
