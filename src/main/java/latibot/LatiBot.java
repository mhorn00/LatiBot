package latibot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import latibot.llisteners.CommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class LatiBot {
	
	private static JDA jdaInst;
	public static final Logger LOG = LoggerFactory.getLogger(LatiBot.class);
	
    public static void main(String[] args) throws IOException {
        jdaInst = JDABuilder.createDefault(new String(LatiBot.class.getClassLoader().getResourceAsStream("token.txt").readAllBytes()))
        		.setActivity(Activity.watching("the fog coming"))
        		.enableIntents(GatewayIntent.MESSAGE_CONTENT)
        		.addEventListeners(new CommandListener()).build();
        jdaInst.updateCommands().addCommands(
        		Commands.slash("ping", "Pong!"), 
        		Commands.slash("emotestats", "Calculates emote usage statisitcs for the server. Will take a long time most likely.")
        			.setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        			.addOptions(new OptionData(OptionType.INTEGER, "cutoff", "Cutoff for how many times an emote needs to be counted to be displayed in the outut.", true).setRequiredRange(0, Integer.MAX_VALUE)),
        		Commands.slash("nickname", "Change a user's nickname.")
        			.setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_MANAGE))
        			.addOptions(new OptionData(OptionType.USER, "user", "The user to change.", true), new OptionData(OptionType.STRING, "nickname", "The new nickname to set.", true).setRequiredLength(1, 32)),
        		Commands.slash("shutdown", "Shutdown the bot.")
        			.setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
        		Commands.slash("say", "Say somthing as the bot.")
        			.setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.MANAGE_ROLES))
        			.addOptions(new OptionData(OptionType.STRING, "message", "The message to send.", true).setRequiredLength(1, 2000), new OptionData(OptionType.STRING, "reply", "Optional message id to reply to.", false))
        		).queue();
    }
}
