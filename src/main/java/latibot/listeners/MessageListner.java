package latibot.listeners;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class MessageListner extends ListenerAdapter {

    @Override
    public void onMessageReceived(MessageReceivedEvent e) {
        // ignore bot msgs
        if (e.getAuthor().isBot()) return;

        // ignore dms
        if (!e.getMessage().isFromGuild()) return;

        String msg = e.getMessage().getContentRaw();
        // if (msg.contains());
    }

}
