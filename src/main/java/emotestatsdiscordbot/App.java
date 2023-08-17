package emotestatsdiscordbot;

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
	public static final String token = "MTgwMTI3MTM2MjQwMjM4NTky.GXQsQq.qE7aPoLYK_b3VtbS7ohaUlhh-uPVp5M9b647Eg";
	
    public static void main(String[] args) {
        jdaInst = JDABuilder.createDefault(token)
        		.setActivity(Activity.listening("your walls"))
        		.enableIntents(GatewayIntent.MESSAGE_CONTENT)
        		.addEventListeners(new CommandListener()).build();
        jdaInst.updateCommands().addCommands(
        		Commands.slash("ping", "Pong!"), 
        		Commands.slash("stats", "Calculates emote statisitcs for the server")
        			.setGuildOnly(true).setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.ADMINISTRATOR))
        		).queue();
    }
}
