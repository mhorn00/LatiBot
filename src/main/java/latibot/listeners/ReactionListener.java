package latibot.listeners;

import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.react.MessageReactionAddEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReactionListener extends ListenerAdapter {

    @Override
    public void onMessageReactionAdd(MessageReactionAddEvent event){
        Message message = event.retrieveMessage().complete();

        //checks to only trigger on riggbot's messages
        //TODO: replace riggbot with latibot
        if(!message.getAuthor().isBot()&&!message.getAuthor().getName().equals("riggbot")) return;

        if(event.getEmoji().toString().equals("UnicodeEmoji(codepoints=U+1f501)")){
            message.editMessage(message.getContentRaw()).queue();
            message.removeReaction(event.getEmoji(),event.getUser()).queue();
        }
    }
}
