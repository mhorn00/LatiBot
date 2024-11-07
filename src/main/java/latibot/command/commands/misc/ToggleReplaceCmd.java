package latibot.command.commands.misc;

import latibot.command.BaseCommand;
import latibot.listeners.MessageListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ToggleReplaceCmd extends BaseCommand {

	public ToggleReplaceCmd() {
		super("toggle", "Toggle the bot's URL replacement feature for yourself or another user.");
	}

	@Override
	public void execute(SlashCommandInteractionEvent e) {
		User user = e.getOption("user") == null ? e.getUser() : e.getOption("user").getAsUser();
		if (MessageListener.userBlacklist.contains(user.getIdLong())) {
			MessageListener.userBlacklist.remove(user.getIdLong());
			e.reply("URL replacement enabled for " + user.getAsMention()).setSuppressedNotifications(true).queue();
		} else {
			MessageListener.userBlacklist.add(user.getIdLong());
			e.reply("URL replacement disabled for " + user.getAsMention()).setSuppressedNotifications(true).queue();
		}
	}

	@Override
	public SlashCommandData buildCommand() {
		return Commands.slash(name, description)
				.addOptions(new OptionData(OptionType.USER, "user", "The user to toggle the feature for.", false))
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND));
	}

	@Override
	public SlashCommandData buildCommand(String alias) {
		return Commands.slash(alias, description)
				.addOptions(new OptionData(OptionType.USER, "user", "The user to toggle the feature for.", false))
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MESSAGE_SEND));
	}
}
