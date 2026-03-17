package com.artillexstudios.axvaults.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axvaults.AxVaults;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
    private static final Logger log = LoggerFactory.getLogger(ThreadUtils.class);

    public static void checkMain(String message) {
        if (!Scheduler.get().isGlobalTickThread()) {
            log.error("Thread {} failed main thread check for {}!", Thread.currentThread().getName(), message, new Throwable());
            throw new RuntimeException();
        }
    }

    public static void checkNotMain(String message) {
        if (Scheduler.get().isGlobalTickThread()) {
            log.error("Thread {} failed main thread check for {}!", Thread.currentThread().getName(), message, new Throwable());
            throw new RuntimeException();
        }
    }

    public static void runAsync(Runnable runnable) {
        if (AxVaults.isStopping()) {
            runSync(runnable);
        } else if (!Scheduler.get().isGlobalTickThread()) {
            runnable.run();
        } else {
            AxVaults.getThreadedQueue().submit(runnable);
        }
    }

    public static void runSync(Runnable runnable) {
        if (Scheduler.get().isGlobalTickThread() || AxVaults.isStopping()) {
            runnable.run();
        } else {
            Scheduler.get().run(runnable);
        }
    }
}
