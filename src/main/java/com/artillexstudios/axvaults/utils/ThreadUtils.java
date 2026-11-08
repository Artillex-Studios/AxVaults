package com.artillexstudios.axvaults.utils;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axvaults.AxVaults;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadUtils {
    private static final Logger log = LoggerFactory.getLogger(ThreadUtils.class);
    private static final ThreadLocal<Integer> ENTITY_TASK_DEPTH = ThreadLocal.withInitial(() -> 0);
    private static final ThreadLocal<Entity> ENTITY_TASK_ENTITY = new ThreadLocal<>();

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

    public static void runAtEntity(Entity entity, Runnable runnable) {
        if (isEntityTask(entity) || AxVaults.isStopping()) {
            runEntityTask(entity, runnable);
            return;
        }

        Scheduler.get().run(entity, task -> runEntityTask(entity, runnable), () -> {});
    }

    public static void runAtSender(CommandSender sender, Runnable runnable) {
        if (sender instanceof Entity entity) {
            runAtEntity(entity, runnable);
            return;
        }

        runSync(runnable);
    }

    public static boolean isEntityTask() {
        return ENTITY_TASK_DEPTH.get() > 0;
    }

    public static boolean isEntityTask(Entity entity) {
        return isEntityTask() && ENTITY_TASK_ENTITY.get() == entity;
    }

    private static void runEntityTask(Entity entity, Runnable runnable) {
        int previousDepth = ENTITY_TASK_DEPTH.get();
        final Entity previousEntity = ENTITY_TASK_ENTITY.get();
        ENTITY_TASK_DEPTH.set(previousDepth + 1);
        ENTITY_TASK_ENTITY.set(entity);
        try {
            runnable.run();
        } finally {
            if (previousDepth <= 0) {
                ENTITY_TASK_DEPTH.remove();
                ENTITY_TASK_ENTITY.remove();
            } else {
                ENTITY_TASK_DEPTH.set(previousDepth);
                ENTITY_TASK_ENTITY.set(previousEntity);
            }
        }
    }
}
