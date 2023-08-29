package emotestatsdiscordbot;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import emotestatsdiscordbot.llisteners.CommandListener;
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
        		.setActivity(Activity.listening("your walls"))
        		.enableIntents(GatewayIntent.MESSAGE_CONTENT)
        		.addEventListeners(new CommandListener()).build();
        jdaInst.updateCommands().addCommands(
        		Commands.slash("ping", "Pong!"), 
        		Commands.slash("emotestats", "Calculates emote usage statisitcs for the server. Will take a long time most likely.")
        			.setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        			.addOptions(new OptionData(OptionType.INTEGER, "cutoff", "Cutoff for how many times an emote needs to be counted to be displayed in the outut.", true).setRequiredRange(0, Integer.MAX_VALUE))
        		).queue();
    }
}
