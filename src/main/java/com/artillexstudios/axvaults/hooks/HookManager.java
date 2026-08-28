package com.artillexstudios.axvaults.hooks;

import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.Bukkit;

public class HookManager {
    private static Placeholders placeholderParser;

    public static void setupHooks() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            placeholderParser = new PlaceholderAPIParser();
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FF00[AxVaults] Hooked into PlaceholderAPI!"));
        } else {
            placeholderParser = new Placeholders() {};
        }
    }

    public static Placeholders getPlaceholderParser() {
        return placeholderParser;
    }
}
