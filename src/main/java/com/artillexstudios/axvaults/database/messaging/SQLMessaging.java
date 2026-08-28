package com.artillexstudios.axvaults.database.messaging;

import com.artillexstudios.axapi.executor.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.database.impl.MySQL;

import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class SQLMessaging {
    private static ScheduledExecutorService executor = null;

    public static void start() {
        String support = CONFIG.getString("multi-server-support", null);
        if (support == null) return;
        if (support.equalsIgnoreCase("none")) { // remove unsupported setting if unused
            CONFIG.remove("multi-server-support");
            CONFIG.save();
            return;
        }
        if (!(AxVaults.getDatabase() instanceof MySQL db)) { // remove unsupported setting if unused
            CONFIG.remove("multi-server-support");
            CONFIG.save();
            return;
        }
        if (executor != null) executor.shutdown();
        executor = new ExceptionReportingScheduledThreadPool(1);
        executor.scheduleAtFixedRate(db::checkForChanges, 5, 5, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(db::removeOldChanges, 10, 10, TimeUnit.SECONDS);
    }

    public static void stop() {
        if (executor == null) return;
        executor.shutdown();
    }
}
