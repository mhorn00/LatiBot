package latibot.command.comands.bot;

import java.util.concurrent.TimeUnit;

import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.channel.concrete.VoiceChannel;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.managers.AudioManager;

public class JoinVoiceCmd extends BaseCommand {

	public JoinVoiceCmd() {
		super("join", "Joins the voice channel you are in or join a specific user.");
	}

	@Override
	public void execute(SlashCommandInteractionEvent e) {
		Member user = e.getOption("user") == null ? e.getMember() : e.getOption("user").getAsMember();
		AudioManager am = e.getGuild().getAudioManager();
		if (user != null && user.getVoiceState().inAudioChannel()) {
			VoiceChannel vc = user.getVoiceState().getChannel().asVoiceChannel();
			if (!am.isConnected()) {
				am.openAudioConnection(vc);
				e.reply("ok joining " + user.getEffectiveName()).setSuppressedNotifications(true)
						.queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			} else if (am.getConnectedChannel().getId().equals(vc.getId())) {
				e.reply("i'm already in " + user.getEffectiveName() + "'s voice channel")
						.setSuppressedNotifications(true)
						.queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			} else {
				am.openAudioConnection(vc);
				e.reply("ok moving to " + user.getEffectiveName()).setSuppressedNotifications(true)
						.queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
		} else if (e.getMember().getVoiceState().inAudioChannel()) {
			VoiceChannel vc = e.getMember().getVoiceState().getChannel().asVoiceChannel();
			if (!am.isConnected()) {
				am.openAudioConnection(vc);
				e.reply("ok joining").setSuppressedNotifications(true)
						.queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			} else if (am.getConnectedChannel().getId().equals(vc.getId())) {
				e.reply("i'm already in your voice channel").setSuppressedNotifications(true)
						.queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			} else {
				am.openAudioConnection(vc);
				e.reply("ok moving").setSuppressedNotifications(true)
						.queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
			}
		} else {
			e.reply("you're not in a voice channel").setSuppressedNotifications(true)
					.queue(hook -> hook.deleteOriginal().queueAfter(10, TimeUnit.SECONDS));
		}
	}

	@Override
	public SlashCommandData buildCommand() {
		return Commands.slash(name, description)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK))
				.addOptions(new OptionData(OptionType.USER, "user", "The specific user to join.", false));
	}

	@Override
	public SlashCommandData buildCommand(String alias) {
		return Commands.slash(alias, description)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.VOICE_SPEAK))
				.addOptions(new OptionData(OptionType.USER, "user", "The specific user to join.", false));
	}

}
