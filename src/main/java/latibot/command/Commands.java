package latibot.command;

import latibot.command.comands.audio.ClearCmd;
import latibot.command.comands.audio.NowPlayingCmd;
import latibot.command.comands.audio.PauseCmd;
import latibot.command.comands.audio.PlayCmd;
import latibot.command.comands.audio.QueueCmd;
import latibot.command.comands.audio.RepeatCmd;
import latibot.command.comands.audio.ShuffleCmd;
import latibot.command.comands.audio.SkipCmd;
import latibot.command.comands.audio.SpeakCmd;
import latibot.command.comands.bot.JoinVoiceCmd;
import latibot.command.comands.bot.LeaveVoiceCmd;
import latibot.command.comands.bot.PingCmd;
import latibot.command.comands.bot.SayCmd;
import latibot.command.comands.bot.ShutdownCmd;
import latibot.command.comands.chat.ChatTestCmd;
import latibot.command.comands.misc.EmoteStatsCmd;
import latibot.command.comands.misc.GetEmotesCmd;
import latibot.command.comands.misc.ReplaceUrlCmd;
import latibot.command.comands.misc.StatusCmd;
import latibot.command.comands.user.NicknameCmd;
import latibot.command.comands.user.NicknamesCmd;

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
