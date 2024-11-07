package latibot.command.commands.bot;

import java.util.concurrent.TimeUnit;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

public class LeaveVoiceCmd extends BaseCommand {

	public LeaveVoiceCmd() {
		super("leave", "Leaves the voice channel.");
	}

	@Override
	public void execute(SlashCommandInteractionEvent e) {
		AudioManager am = e.getGuild().getAudioManager();
		if (LatiBot.tm == null || !am.isConnected()) {
			e.reply("i'm not currently in a voice channel").setSuppressedNotifications(true)
					.queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		} else {
			am.closeAudioConnection();
			LatiBot.audioPlayer.removeListener(LatiBot.tm);
			LatiBot.tm = null;
			e.reply("ok bye").setSuppressedNotifications(true)
					.queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		}
	}

	@Override
	public SlashCommandData buildCommand() {
		return Commands.slash(name, description)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK));
	}

	@Override
	public SlashCommandData buildCommand(String alias) {
		return Commands.slash(alias, description)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK));
	}

}
