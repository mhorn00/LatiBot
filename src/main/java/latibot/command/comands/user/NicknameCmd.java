package latibot.command.comands.user;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import latibot.listeners.NicknameListener;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

public class NicknameCmd extends BaseCommand {

    public NicknameCmd() {
        super("nickname", "Change a user's nickname.");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        Member user = e.getOption("user").getAsMember();
		String newNickname = e.getOption("nickname").getAsString();
		String oldNickname = user.getNickname();
		
        oldNickname = oldNickname != null ? oldNickname : "null";
		
        LatiBot.LOG.info("User "+e.getUser().getName() + " used the 'nickname' command with args "+user.getUser().getName()+" "+newNickname);
		
        String hash = NicknameListener.hashNameChange(oldNickname, newNickname, user.getId());
		if (!user.isOwner()) {
			NicknameListener.addHash(hash,new NicknameListener.NicknameCmdInfo(oldNickname, newNickname, user.getId(), e.getUser().getId(), hash));
			user.modifyNickname(newNickname).queue();
			e.reply("Set nickname of "+user.getUser().getName()+" from '"+oldNickname+"' to '"+newNickname+"'").setEphemeral(true).queue();
		} else {
			NicknameListener.addHash(hash,new NicknameListener.NicknameCmdInfo(oldNickname, newNickname, user.getId(), e.getUser().getId(), hash, e.getGuild().getSystemChannel().sendMessage(e.getUser().getAsMention() + " updated your nickname to '"+newNickname+"' "+e.getGuild().getOwner().getAsMention()).addActionRow(
				Button.success(hash+"-OK", Emoji.fromCustom("smwOK", 1150941028291985478l, false)),
				Button.danger(hash+"-NO", Emoji.fromCustom("smwNO", 1150941027360854067l, false))).complete().getId()));
			e.reply("\"Set\" nickname of "+user.getUser().getName()+" from '"+oldNickname+"' to '"+newNickname+"'").setEphemeral(true).queue();
		}
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash(name, description)
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_MANAGE))
        	.addOptions(
                new OptionData(OptionType.USER, "user", "The user to change.", true), 
                new OptionData(OptionType.STRING, "nickname", "The new nickname to set.", true)
                    .setRequiredLength(1, 32)
            );
    }

    @Override
    public SlashCommandData buildCommand(String alias) {
        return Commands.slash(alias, description)
            .setGuildOnly(true)
            .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_MANAGE))
        	.addOptions(
                new OptionData(OptionType.USER, "user", "The user to change.", true), 
                new OptionData(OptionType.STRING, "nickname", "The new nickname to set.", true)
                    .setRequiredLength(1, 32)
            );
    }

}
