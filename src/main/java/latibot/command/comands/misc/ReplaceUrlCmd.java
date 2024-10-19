package latibot.command.comands.misc;

import latibot.command.BaseCommand;
import latibot.listeners.MessageListener;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class ReplaceUrlCmd extends BaseCommand {
    public ReplaceUrlCmd() {
        super("replaceurl", "updates/adds URL replacement");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        String domain = e.getOption("domain").getAsString();
        String replacement = e.getOption("replacement").getAsString();
        if (domain.equals(replacement)) {
            MessageListener.getDomains().remove(domain);
            if (MessageListener.saveUrlReplacements()) {
                e.reply("Urls with '" + domain + "' will no longer be replaced").queue();
            }
        } else {
            MessageListener.getDomains().put(domain, replacement);
            if (MessageListener.saveUrlReplacements()) {
                e.reply("Urls with '" + domain + "' will now be replaced with '" + replacement + "'").queue();
            }
        }
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("replaceurl", "updates/adds URL replacement")
                .addOption(OptionType.STRING, "domain", "The domain to be replaced (e.g., 'x.com')")
                .addOption(OptionType.STRING, "replacement", "What the domain should be replaced with (e.g., 'fxtwitter.com')");
    }

    @Override
    public SlashCommandData buildCommand(String alias) {
        return Commands.slash(alias, "updates/adds URL replacement")
                .addOption(OptionType.STRING, "domain", "The domain to be replaced (e.g., 'x.com')")
                .addOption(OptionType.STRING, "replacement", "What the domain should be replaced with (e.g., 'fxtwitter.com')");
    }
}
