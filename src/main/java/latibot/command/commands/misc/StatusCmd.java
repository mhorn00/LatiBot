package latibot.command.commands.misc;

import latibot.command.BaseCommand;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Activity.ActivityType;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.DefaultMemberPermissions;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;

public class StatusCmd extends BaseCommand {
    public StatusCmd() {
        super("status", "set the bot status");
    }

    @Override
    public void execute(SlashCommandInteractionEvent e) {
        String status = e.getOption("status").getAsString();
        ActivityType type = ActivityType.valueOf(e.getOption("type") == null ? "PLAYING" : e.getOption("type").getAsString());

        switch (type) {
            case PLAYING:
                e.getJDA().getPresence().setActivity(Activity.playing(status));
                break;
            case WATCHING:
                e.getJDA().getPresence().setActivity(Activity.watching(status));
                break;
            case LISTENING:
                e.getJDA().getPresence().setActivity(Activity.listening(status));
                break;
            case COMPETING:
                e.getJDA().getPresence().setActivity(Activity.competing(status));
                break;
            case CUSTOM_STATUS:
                e.getJDA().getPresence().setActivity(Activity.customStatus(status));
                break;
            default:
                break;
        }
        e.reply("Status set to: " + status).setEphemeral(true).setSuppressedNotifications(true).queue();
    }

    @Override
    public SlashCommandData buildCommand() {
        return Commands.slash(name, description)
                .addOption(OptionType.STRING, "status", "The new status to set", true)
                .addOptions(
                        new OptionData(OptionType.STRING, "type", "The type of status to set", false).setMaxLength(128)
                                .addChoice(ActivityType.PLAYING.name(), ActivityType.PLAYING.name())
                                .addChoice(ActivityType.WATCHING.name(), ActivityType.WATCHING.name())
                                .addChoice(ActivityType.LISTENING.name(), ActivityType.LISTENING.name())
                                .addChoice(ActivityType.COMPETING.name(), ActivityType.COMPETING.name())
                                .addChoice(ActivityType.CUSTOM_STATUS.name(), ActivityType.CUSTOM_STATUS.name()))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_MANAGE));
    }

    @Override
    public SlashCommandData buildCommand(String alias) {
        return Commands.slash(alias, description)
                .addOption(OptionType.STRING, "status", "The new status to set", true)
                .addOptions(
                        new OptionData(OptionType.STRING, "type", "The type of status to set", false).setMaxLength(128)
                                .addChoice(ActivityType.PLAYING.name(), ActivityType.PLAYING.name())
                                .addChoice(ActivityType.WATCHING.name(), ActivityType.WATCHING.name())
                                .addChoice(ActivityType.LISTENING.name(), ActivityType.LISTENING.name())
                                .addChoice(ActivityType.COMPETING.name(), ActivityType.COMPETING.name())
                                .addChoice(ActivityType.CUSTOM_STATUS.name(), ActivityType.CUSTOM_STATUS.name()))
                .setDefaultPermissions(DefaultMemberPermissions.enabledFor(Permission.NICKNAME_MANAGE));
    }
}
