package latibot.utils;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import latibot.LatiBot;

public class MidnightManager {
    
    public static void scheduleMidnight() {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Chicago"));
        ZonedDateTime nextMidnight = now.withHour(0).withMinute(0).withSecond(2);
        if (now.compareTo(nextMidnight) > 0)
            nextMidnight = nextMidnight.plusDays(1);

        Duration duration = Duration.between(now, nextMidnight);
        long initialDelay = duration.getSeconds();

        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);            
        scheduler.scheduleAtFixedRate(new TimerTask() {
            @Override
            public void run() {
                sendMidnight();
            }
        },
        initialDelay,
        TimeUnit.DAYS.toSeconds(1),
        TimeUnit.SECONDS);
    }
    
    public static void sendMidnight() {
        LatiBot.jdaInst.getTextChannelById(142409638556467200l).sendMessage("midnight").queue((m) -> LatiBot.LOG.info("midnight"));
    }
}
