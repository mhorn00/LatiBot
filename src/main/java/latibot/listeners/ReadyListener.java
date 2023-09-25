package latibot.listeners;

import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class ReadyListener extends ListenerAdapter {
	
	@Override
	public void onReady(ReadyEvent event) {
		NicknameListener.populateHistory(event.getJDA());
	}
}
