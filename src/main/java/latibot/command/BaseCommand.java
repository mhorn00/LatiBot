package latibot.command;

public abstract class BaseCommand {

    public static BaseCommand instance;

    public abstract void execute();
}
