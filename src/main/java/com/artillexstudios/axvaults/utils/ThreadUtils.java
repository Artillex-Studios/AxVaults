package com.artillexstudios.axvaults.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import org.bukkit.Bukkit;

public class ThreadUtils {

    public static void runSync(Runnable runnable) {
        if (Bukkit.isPrimaryThread()) runnable.run();
        else Scheduler.get().run(runnable);
    }
}
