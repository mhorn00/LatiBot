package latibot.listeners;

import latibot.command.AudioCommands;
import latibot.command.AudioCommands.PlayCommands;
import latibot.command.GeneralCommands;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class CommandListener extends ListenerAdapter {
	
	@Override
	public void onSlashCommandInteraction(SlashCommandInteractionEvent e) {
		
		
		
		switch (e.getName()) {
		
		//Bot Control Commands
		case "shutdown":
			GeneralCommands.shutdownCmd(e);
			break;
		case "say":
			GeneralCommands.sayCmd(e);
			break;
			
		//User Commands
		case "nickname":
			GeneralCommands.nicknameCmd(e);
			break;
		case "nicknames":	
			GeneralCommands.nicknamesCmd(e);
			break;
		
		//Misc Commands
		case "ping":
			GeneralCommands.pingCmd(e);
			break;
		case "wordle":
			GeneralCommands.wordleCmd(e);
			break;
			
		//Emote Commands
		case "emotestats":
			GeneralCommands.statsCmd(e);
			break;
		case "getemotes":
			GeneralCommands.getEmotesCmd(e);
			break;
			
		//Audio Commands
		case "q":
		case "queue":
			AudioCommands.queueCmd(e);
			break;
		case "np":
		case "nowplaying":
			AudioCommands.nowPlayingCmd(e);
			break;
		case "play":
			AudioCommands.playCmd(e, PlayCommands.Normal);
			break;
		case "playnow":
			AudioCommands.playCmd(e, PlayCommands.Now);
			break;
		case "playnext":
			AudioCommands.playCmd(e, PlayCommands.Next);
			break;
		case "playsilent":
			AudioCommands.playCmd(e, PlayCommands.Silent);
			break;
		case "skip":
			AudioCommands.skipCmd(e);
			break;
		case "join":
			AudioCommands.joinCmd(e);
			break;
		case "leave":
			AudioCommands.leaveCmd(e);
			break;
		case "pause":
			AudioCommands.pauseCmd(e);
			break;
		case "shuffle":
			AudioCommands.shuffleCmd(e);
			break;
		case "repeat":
			AudioCommands.repeatCmd(e);
			break;
		case "clear":
			AudioCommands.clearCmd(e);
			break;
		case "speak":
			AudioCommands.speakCmd(e);
			break;
		}
	}
}
