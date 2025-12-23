package com.artillexstudios.axvaults.utils;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.logging.DebugMode;
import com.artillexstudios.axapi.utils.logging.LogUtils;

import java.util.function.Supplier;

public class DebugUtils {
    private static Config config;
    private static boolean enabled;

    public static void init(Config config) {
        DebugUtils.config = config;
        reload();
    }

    public static void reload() {
        enabled = config.getBoolean("debug", false);
    }

    public static void log(Supplier<String> text) {
        log(text, DebugMode.ALL);
    }

    public static void log(Supplier<String> text, DebugMode debugMode) {
        if (!enabled) return;
        LogUtils.debug(text.get(), debugMode);
    }

    public static boolean toggle() {
        enabled = !enabled;
        config.set("debug", enabled);
        config.save();
        return enabled;
    }

    public static boolean isEnabled() {
        return enabled;
    }
}
