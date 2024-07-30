package latibot.listeners;

import latibot.listeners.NicknameListener.NicknameHistory;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReadyListener extends ListenerAdapter {
	
	@Override
	public void onReady(ReadyEvent event) {
		NicknameListener.populateHistory(event.getJDA());
		boolean changed = false;
		for (NicknameHistory entry : NicknameListener.nicknamesHistory.values()) {
			if (!entry.getLatestNickname().nickname.equals(entry.getMember().getNickname())) {
				entry.addNickname(entry.getMember().getNickname(), entry.getMember().getId());
				changed = true;
			}
		}
		if (changed) NicknameListener.writeJson();
	}
}
