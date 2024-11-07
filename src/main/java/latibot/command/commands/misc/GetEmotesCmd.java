package latibot.command.commands.misc;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.List;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.emoji.RichCustomEmoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class GetEmotesCmd extends BaseCommand {

	public GetEmotesCmd() {
		super("getemotes", "Downloads all visible emotes.");
	}

	@Override
	public void execute(SlashCommandInteractionEvent e) {
		e.reply("ok").queue();
		List<RichCustomEmoji> emotes = e.getJDA().getEmojis();
		try {
			Files.createDirectories(Paths.get("emotes"));
			emotes.forEach(emoji -> {
				try {
					Files.createDirectories(Paths.get("emotes/" + emoji.getGuild().getName()));
				} catch (IOException e1) {
					LatiBot.LOG.error("Failed to create guild emote directory '" + emoji.getGuild().getName() + "'",
							e1);
				}
				emoji.getImage().downloadToFile(new File("emotes/" + emoji.getGuild().getName() + "/" + emoji.getName()
						+ (emoji.isAnimated() ? ".gif" : ".png"))).whenComplete((f, err) -> {
							if (err == null) {
								LatiBot.LOG.info("Wrote file " + f.getName());
							} else {
								LatiBot.LOG.error("Failed to write file " + f.getName(), err);
							}
						});
			});
		} catch (IOException err) {
			LatiBot.LOG.error("Failed to create emotes directory.", err);
		}
	}

	@Override
	public SlashCommandData buildCommand() {
		return Commands.slash(name, description)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
	}

	@Override
	public SlashCommandData buildCommand(String alias) {
		return Commands.slash(alias, description)
				.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR));
	}
}
