package latibot.listeners;

import latibot.LatiBot;
import latibot.listeners.NicknameListener.NicknameCmdInfo;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ActionListener extends ListenerAdapter {
    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {      
        // "<str hash>-<buttontype>"
        if (!event.getMember().isOwner()) return;
        String[] buttonId = event.getComponentId().split("-");
        if (buttonId == null || buttonId.length != 2) return; 
        if (NicknameListener.hashes.containsKey(buttonId[0])) {
            NicknameCmdInfo info = NicknameListener.hashes.get(buttonId[0]);
            if (buttonId[1].equals("OK")) {
                String curName = event.getGuild().getMemberById(info.victimId).getNickname();
                LatiBot.LOG.info("curName: " + curName + " newNickname: " + info.newNickname);
                if (!curName.equals(info.newNickname)) {
                    event.reply("your nickname doesnt match???").queue();
                    return;
                }
                NicknameListener.confirmHash(buttonId[0]);
                event.getMessage().addReaction(Emoji.fromUnicode("\uD83D\uDC4D")).queue();
                event.deferEdit().queue();
            } else if (buttonId[1].equals("NO")) {
                NicknameListener.hashes.remove(buttonId[0]);
                event.getMessage().addReaction(Emoji.fromUnicode("\uD83D\uDC4E")).queue();
                event.deferEdit().queue();
            }
        }
    } 
}
