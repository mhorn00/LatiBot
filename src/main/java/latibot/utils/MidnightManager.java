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
        ZonedDateTime nextMidnight = now.plusDays(1).withHour(0).withMinute(0).withSecond(5).withNano(0);
        if (now.isAfter(nextMidnight)) {
            nextMidnight = nextMidnight.plusDays(1);
            LatiBot.LOG.info("huh???");
        } 

        Duration duration = Duration.between(now, nextMidnight);
        long initialDelay = duration.getSeconds();

        // LatiBot.LOG.info("Scheduling next midnight task in " + duration.toHours() + " hours and " + duration.toMinutesPart() + " minutes from now");

        scheduler.schedule(midnightTask, initialDelay, TimeUnit.SECONDS);
    }

    public static void sendMidnight() {
        LatiBot.jdaInst.getTextChannelById(142409638556467200L)
            .sendMessage("midnight")
            .setSuppressedNotifications(true)
            .queue((m) -> LatiBot.LOG.info("midnight"));
    }
}
