package latibot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import latibot.audio.DecTalkWrapper;
import latibot.audio.TrackManager;
import latibot.command.Commands;
import latibot.listeners.*;
import latibot.utils.MidnightManager;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Webhook;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;

/* TODO: General
 *  - Rewrite latibot in dart lol
 *  - Wordle stats
 *  - midnight stats
 *  - reminders?
 *  - speak cmd in chat
 *  - make dectalk louder lol
 *  - dectalk copy pastas
 *  - random sound effects
 *  - fake quote cmd
 *  - music controls via buttons
 */

public class LatiBot {

    public static JDA jdaInst;
    public static final Logger LOG = LoggerFactory.getLogger(LatiBot.class);
    public static final AudioPlayerManager audioPlayerManager = new DefaultAudioPlayerManager();
    public static AudioPlayer audioPlayer;
    public static TrackManager tm = null;
    public static DecTalkWrapper dectalk = null;

    public static void shutdownTTS() {
        if (dectalk != null) {
            dectalk.ttsShutdown();
        }
    }

    public static void main(String[] args) throws IOException {
        jdaInst = JDABuilder
                .createDefault(
                        new String(LatiBot.class.getClassLoader().getResourceAsStream("token.txt").readAllBytes()))
                .setActivity(Activity.competing("a jorkin it contest"))
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .enableIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.MESSAGE_CONTENT,
                        GatewayIntent.GUILD_MESSAGE_REACTIONS)
                .enableCache(CacheFlag.VOICE_STATE)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(new CommandListener(), new NicknameListener(), new ReadyListener(),
                        new ActionListener(), new MessageListener(), new ReactionListener())
                .build();

        try {
            jdaInst.awaitReady();
        } catch (InterruptedException e) {
            LOG.error("Failed to start JDA", e);
            System.exit(-1);
        }


        LOG.info("Latibot is in " + jdaInst.getGuilds().size() + " guilds");

        //> Check needed perms
        List<Permission> perms = List.of(
                Permission.VOICE_CONNECT,
                Permission.VOICE_SPEAK,
                Permission.VOICE_USE_VAD,
                Permission.MESSAGE_MANAGE,
                Permission.MESSAGE_SEND,
                Permission.MESSAGE_ADD_REACTION,
                Permission.MESSAGE_ATTACH_FILES,
                Permission.MESSAGE_EMBED_LINKS,
                //Permission.MESSAGE_ATTACH_VOICE_MESSAGE,
                Permission.MESSAGE_HISTORY,
                Permission.MANAGE_WEBHOOKS,
                Permission.NICKNAME_CHANGE,
                Permission.NICKNAME_MANAGE,
                Permission.MESSAGE_EXT_EMOJI,
                Permission.MESSAGE_EXT_STICKER
        );

        boolean okFlag = true;
        Guild guild = jdaInst.getGuildById(142409638556467200L); //LATV
        //Guild guild = jdaInst.getGuildById(968236034250924052L); //RiggBot Testing
        LOG.info("Checking permissions in guild " + guild.getName());
        for (Permission perm : perms) {
            if (!guild.getSelfMember().hasPermission(perm)) {
                LOG.error("Missing permission: " + perm.getName());
                guild.getSystemChannel().sendMessage("Missing permission: " + perm.getName()).queue((s) -> {
                }, (f) -> LOG.info("Failed to send missing permission message"));
                okFlag = false;
            }
        }
        if (!okFlag) {
            LOG.error("Bot is missing permissions, exiting...");
            jdaInst.shutdown();
            System.exit(-3);
        }

        // Loading webhooks
        LatiBot.LOG.info("Retrieving WebHooks");
        HashMap<Long, String> webhookUrls = new HashMap<>();
        for (Guild g : jdaInst.getGuilds()) {
            List<Webhook> webhooks = g.retrieveWebhooks().complete();
            for (Webhook w : webhooks) {
                if (w.getName().equals("Url Replacer")) {
                    LatiBot.LOG.info("Url Replacer Webhook found in Channel: #{}", w.getChannel().getName());
                    webhookUrls.put(w.getChannel().getIdLong(), w.getUrl());
                }
            }
        }
        // cum-zone
        webhookUrls.put(996959766440058883L, "https://discord.com/api/webhooks/1303406233943806084/6haDb6EMxld_WQti1-Qgb01jDAe9C3CqTlR_AvN41xNt-Tmz66jnqne3uEox3yIGnYyx");
        LatiBot.LOG.info("Url Replacer Webhook found in Channel: #cum-zone");
        MessageListener.setWebhookUrls(webhookUrls);
        MessageListener.setJda(jdaInst);

        List<SlashCommandData> cmds = Commands.COMMANDS.getCommands().values().stream().flatMap((v) -> {
            Stream.Builder<SlashCommandData> b = Stream.builder();

            LOG.info("Registering command " + v.getName());
            b.add(v.buildCommand());

            if (v.hasAlias())
                for (String alias : v.getAliases()) {
                    LOG.info("Registering alias " + alias + " for command " + v.getName());
                    b.add(v.buildCommand(alias));
                }

            return b.build();
        }).toList();

        jdaInst.updateCommands().addCommands(cmds).queue();

        //ApiDriver.init();

        AudioSourceManagers.registerRemoteSources(audioPlayerManager);
        audioPlayer = audioPlayerManager.createPlayer();

        MidnightManager.scheduleMidnight();
    }
}
