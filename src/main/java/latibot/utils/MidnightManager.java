package latibot.utils;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import latibot.LatiBot;

public class MidnightManager {
    
    private static final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public static void scheduleMidnight() {
        Runnable midnightTask = new Runnable() {
            @Override
            public void run() {
                sendMidnight();
                scheduleNextRun(this);
            }
        };

        scheduleNextRun(midnightTask);
    }

    private static void scheduleNextRun(Runnable midnightTask) {
        ZonedDateTime now = ZonedDateTime.now(ZoneId.of("America/Chicago"));
        ZonedDateTime nextMidnight = now.withHour(0).withMinute(0).withSecond(5).withNano(0);
        if (now.compareTo(nextMidnight) > 0) {
            nextMidnight = nextMidnight.plusDays(1);
        }

        Duration duration = Duration.between(now, nextMidnight);
        long initialDelay = duration.getSeconds();

        scheduler.schedule(midnightTask, initialDelay, TimeUnit.SECONDS);
    }

    public static void sendMidnight() {
        LatiBot.jdaInst.getTextChannelById(142409638556467200L)
            .sendMessage("midnight")
            .setSuppressedNotifications(true)
            .queue((m) -> LatiBot.LOG.info("midnight"));
    }
}
