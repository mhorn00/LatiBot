package latibot.command.comands.user;

import java.text.SimpleDateFormat;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import latibot.listeners.NicknameListener;
import latibot.listeners.NicknameListener.NicknameHistory;
import latibot.listeners.NicknameListener.NicknameHistory.NicknameEntry;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class NicknamesCmd extends BaseCommand {

	public NicknamesCmd() {
		super("nickanmes", "Check the nickname history for a user.");
	}

	@Override
	public void execute(SlashCommandInteractionEvent e) {
		// TODO: This command will reach a 2000 char limit eventually lol. need to fix
		// later
		Member user = e.getOption("user").getAsMember();
		LatiBot.LOG.info("User " + e.getUser().getEffectiveName() + " used the 'nicknames' command with args "
				+ user.getUser().getName());
		String reply = user.getEffectiveName() + " has had the following nicknames:\n";
		NicknameHistory userHistory = NicknameListener.nicknamesHistory.get(user.getId());
		if (userHistory == null) {
			e.reply(user.getEffectiveName() + " does not have any nickname history.").queue();
			return;
		}
		for (NicknameEntry entry : userHistory.getNicknames()) {
			reply += entry.nickname + " - " + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(entry.date)
					+ " - Changed by " + e.getGuild().getMemberById(entry.changedById).getUser().getName() + "\n";
		}
		e.reply(reply).queue();
	}

	@Override
	public SlashCommandData buildCommand() {
		return Commands.slash(name, description)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
				.addOptions(new OptionData(OptionType.USER, "user", "The user to get the nicknames for.", true));
	}

	@Override
	public SlashCommandData buildCommand(String alias) {
		return Commands.slash(alias, description)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND))
				.addOptions(new OptionData(OptionType.USER, "user", "The user to get the nicknames for.", true));
	}
}
