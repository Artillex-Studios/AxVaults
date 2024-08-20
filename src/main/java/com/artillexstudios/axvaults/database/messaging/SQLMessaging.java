package com.artillexstudios.axvaults.database.messaging;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.database.impl.MySQL;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class SQLMessaging {
    private static ScheduledExecutorService executor = null;

    public static void start() {
        if (!(AxVaults.getDatabase() instanceof MySQL db)) return;
        if (CONFIG.getString("multi-server-support", "sql").equalsIgnoreCase("none")) return;
        if (executor != null) executor.shutdown();
        executor = Executors.newSingleThreadScheduledExecutor();
        executor.scheduleAtFixedRate(db::checkForChanges, 5, 5, TimeUnit.SECONDS);
        executor.scheduleAtFixedRate(db::removeOldChanges, 10, 10, TimeUnit.SECONDS);
    }

    public static void stop() {
        if (executor == null) return;
        executor.shutdown();
    }
}
