package latibot.command.commands.misc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import latibot.LatiBot;
import latibot.command.BaseCommand;
import latibot.listeners.MessageListener;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.interactions.commands.build.SubcommandData;

public class ReplaceUrlCmd extends BaseCommand {
    public ReplaceUrlCmd() {
        super("replaceurl", "updates/adds URL replacement");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        String subcommand = e.getSubcommandName();
        if (subcommand == null) {
            e.reply("invalid subcommand????").setEphemeral(true).queue();
            return;
        }

        switch (subcommand) {
            case "add":
                handleAddSubcommand(e);
                break;
            case "remove":
                handleRemoveSubcommand(e);
                break;
            case "list":
                StringBuilder sb = new StringBuilder();
                MessageListener.getDomains().forEach((domain, replacements) -> {
                    sb.append(domain).append(" -> ").append(replacements).append("\n");
                });
                e.reply(sb.toString()).queue();
                break;
            default:
                e.reply("????????????????").setEphemeral(true).queue();
                break;
        }
    }

    private void handleAddSubcommand(SlashCommandInteractionEvent e) {
        String domain = e.getOption("domain").getAsString();
        String replacement = e.getOption("replacement").getAsString();
        boolean asAlternate = e.getOption("as_alternate").getAsBoolean();
        if (asAlternate) {
            //> Add the replacement as an alternate or create a new list with the replacement if it doesn't exist
            MessageListener.getDomains().put(domain, MessageListener.getDomains().compute(domain, (k, list) -> {
                //> If the list is null or empty, create a new list with the replacement
                if (list == null || list.size() < 1) return Arrays.asList(new String[] {replacement});
                //> Add the replacement to the list
                List<String> out = new ArrayList<>();
                out.addAll(list);
                out.add(replacement);
                return out;
            }));
            LatiBot.LOG.info(MessageListener.getDomains().get(domain).toString());
        } else {
            //> Replace the first element in the list
            MessageListener.getDomains().put(domain, MessageListener.getDomains().compute(domain, (k, list) -> {
                //> If the list is null or empty, create a new list with the replacement
                if (list == null || list.size() < 1) return Arrays.asList(new String[] {replacement});
                //> Replace the first element with the new replacement
                List<String> out = new ArrayList<>();
                out.addAll(list);
                out.set(0, replacement);
                return out;
            }));
        }

        if (MessageListener.saveUrlReplacements()) {
            if (asAlternate) {
                e.reply("Added '" + replacement + "' as an alternate for '" + domain + "'").queue();
            } else {
                e.reply("Urls with '" + domain + "' will now be replaced with '" + replacement + "'").queue();
            }
        } else {
            e.reply("Failed to save URL replacements.").queue();
        }
    }

    private void handleRemoveSubcommand(SlashCommandInteractionEvent e) {
        String domain = e.getOption("domain").getAsString();

        if (!MessageListener.getDomains().containsKey(domain)) {
            e.reply("'" + domain + "' isnt a url i know????").queue();
            return;
        }

        MessageListener.getDomains().remove(domain);

        if (MessageListener.saveUrlReplacements()) {
            e.reply("Urls with '" + domain + "' will no longer be replaced").queue();
        } else {
            e.reply("Failed to save URL replacements.").queue();
        }
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash("replaceurl", "updates/adds URL replacement").addSubcommands(
                new SubcommandData("add", "Add a URL replacement")
                        .addOption(OptionType.STRING, "domain", "The domain to be replaced (e.g., 'x.com')", true)
                        .addOption(OptionType.STRING, "replacement", "What the domain should be replaced with (e.g., 'fxtwitter.com')", true)
                        .addOption(OptionType.BOOLEAN, "as_alternate",
                                "Add domain as an alt for if the main fails if true, otherwise replace if exists", false, false),
                new SubcommandData("remove", "Remove a URL replacement")
                        .addOption(OptionType.STRING, "domain", "The domain to be removed (e.g., 'x.com')", true),
                        new SubcommandData("list", "List all URL replacements"));
    }

    @Override
    public SlashCommandData buildCommand(String alias) {
        return Commands.slash(alias, "updates/adds URL replacement").addSubcommands(
            new SubcommandData("add", "Add a URL replacement")
                    .addOption(OptionType.STRING, "domain", "The domain to be replaced (e.g., 'x.com')", true)
                    .addOption(OptionType.STRING, "replacement", "What the domain should be replaced with (e.g., 'fxtwitter.com')", true)
                    .addOption(OptionType.BOOLEAN, "as_alternate",
                            "Add domain as an alt for if the main fails if true, otherwise replace if exists", false, false),
            new SubcommandData("remove", "Remove a URL replacement")
                    .addOption(OptionType.STRING, "domain", "The domain to be removed (e.g., 'x.com')", true),
                    new SubcommandData("list", "List all URL replacements"));
    }
}
