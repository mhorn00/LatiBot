package latibot.command;

import latibot.command.commands.audio.ClearCmd;
import latibot.command.commands.audio.NowPlayingCmd;
import latibot.command.commands.audio.PauseCmd;
import latibot.command.commands.audio.PlayCmd;
import latibot.command.commands.audio.QueueCmd;
import latibot.command.commands.audio.RepeatCmd;
import latibot.command.commands.audio.ShuffleCmd;
import latibot.command.commands.audio.SkipCmd;
import latibot.command.commands.audio.SpeakCmd;
import latibot.command.commands.bot.JoinVoiceCmd;
import latibot.command.commands.bot.LeaveVoiceCmd;
import latibot.command.commands.bot.PingCmd;
import latibot.command.commands.bot.SayCmd;
import latibot.command.commands.bot.ShutdownCmd;
import latibot.command.commands.chat.ChatTestCmd;
import latibot.command.commands.misc.*;
import latibot.command.commands.user.NicknameCmd;
import latibot.command.commands.user.NicknamesCmd;

public class Commands {

    public static final CommandRegistry COMMANDS = new CommandRegistry();

    // Bot cmds
    public static final JoinVoiceCmd JOIN_VOICE = COMMANDS.registerCommand(new JoinVoiceCmd());
    public static final LeaveVoiceCmd LEAVE_VOICE = COMMANDS.registerCommand(new LeaveVoiceCmd()); 

    public static final PingCmd PING = COMMANDS.registerCommand(new PingCmd());
    public static final SayCmd SAY = COMMANDS.registerCommand(new SayCmd());
    public static final ShutdownCmd SHUTDOWN = COMMANDS.registerCommand(new ShutdownCmd());

    //user cmds
    public static final NicknameCmd NICKNAME = COMMANDS.registerCommand(new NicknameCmd());
    public static final NicknamesCmd NICKNAMES = COMMANDS.registerCommand(new NicknamesCmd());
    
    //misc cmds
    public static final EmoteStatsCmd EMOTE_STATS = COMMANDS.registerCommand(new EmoteStatsCmd()); 
    public static final GetEmotesCmd GET_EMOTES = COMMANDS.registerCommand(new GetEmotesCmd());
    public static final ReplaceUrlCmd REPLACE_URL = COMMANDS.registerCommand(new ReplaceUrlCmd());
    public static final StatusCmd STATUS = COMMANDS.registerCommand(new StatusCmd());
    public static final ToggleReplaceCmd TOGGLE_REPLACE = COMMANDS.registerCommand(new ToggleReplaceCmd());
    public static final ToggleWebhooksCmd TOGGLE_WEBHOOKS = COMMANDS.registerCommand(new ToggleWebhooksCmd());
    
    //audio cmds
    public static final ClearCmd CLEAR = COMMANDS.registerCommand(new ClearCmd());
    public static final NowPlayingCmd NOW_PLAYING = COMMANDS.registerCommandWithAlias(new NowPlayingCmd(), "np");
    public static final PauseCmd PAUSE = COMMANDS.registerCommand(new PauseCmd());
    public static final PlayCmd PLAY = COMMANDS.registerCommand(new PlayCmd());
    public static final QueueCmd QUEUE = COMMANDS.registerCommandWithAlias(new QueueCmd(), "q");
    public static final RepeatCmd REPEAT = COMMANDS.registerCommand(new RepeatCmd());
    public static final ShuffleCmd SHUFFLE = COMMANDS.registerCommand(new ShuffleCmd());
    public static final SkipCmd SKIP = COMMANDS.registerCommand(new SkipCmd());
    public static final SpeakCmd SPEAK = COMMANDS.registerCommand(new SpeakCmd());

    //chat cmds
    public static final ChatTestCmd CHAT = COMMANDS.registerCommand(new ChatTestCmd());


}
