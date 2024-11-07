package latibot.command.commands.bot;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class SayCmd extends BaseCommand {

    public SayCmd() {
        super("say", "Say something as the bot.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        String msg = e.getOption("message").getAsString();
		String replyId = e.getOption("reply") != null ? e.getOption("reply").getAsString() : null;
		LatiBot.LOG.info("User "+e.getUser().getEffectiveName() + " used the 'say' command with args '"+msg+"'"+(replyId!=null ? " '"+replyId+"'" : ""));
		if (replyId == null) {
			e.reply("ok").setEphemeral(true).queue();
			e.getChannel().sendMessage(msg).queue();
		} else {
			Message replyMsg = e.getChannel().retrieveMessageById(replyId).complete();
			if (replyMsg != null) {
				e.reply("ok").setEphemeral(true).queue();
				replyMsg.reply(msg).queue();
			} else {
				e.reply("Couldn't find message with ID '"+replyId+"' in channel "+e.getChannel().getName()+"!").setEphemeral(true).queue();
			}
		}
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash(name, description)
           	.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
        	.addOptions(
				new OptionData(OptionType.STRING, "message", "The message to send.", true)
					.setRequiredLength(1, 2000), 
				new OptionData(OptionType.STRING, "reply", "Optional message id to reply to.", false)
			);
    }

	@Override
	public SlashCommandData buildCommand(String alias) {
		return Commands.slash(alias, description)
			.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
			.addOptions(
				new OptionData(OptionType.STRING, "message", "The message to send.", true)
					.setRequiredLength(1, 2000), 
				new OptionData(OptionType.STRING, "reply", "Optional message id to reply to.", false)
			);
	}

}
