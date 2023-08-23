package emotestatsdiscordbot;

import java.io.IOException;

import emotestatsdiscordbot.llisteners.CommandListener;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.requests.GatewayIntent;

public class App {
	
	public static JDA jdaInst;
	
    public static void main(String[] args) throws IOException {
        jdaInst = JDABuilder.createDefault(new String(App.class.getClassLoader().getResourceAsStream("token.txt").readAllBytes()))
        		.setActivity(Activity.listening("your walls"))
        		.enableIntents(GatewayIntent.MESSAGE_CONTENT)
        		.addEventListeners(new CommandListener()).build();
        jdaInst.updateCommands().addCommands(
        		Commands.slash("ping", "Pong!"), 
        		Commands.slash("stats", "Calculates emote statisitcs for the server")
        			.setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR)),
        		Commands.slash("sort", "Sort stats output")
        			.setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        		).queue();
    }
}
