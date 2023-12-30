package com.artillexstudios.axvaults.vaults;

import com.artillexstudios.axapi.utils.ClassUtils;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlaceholderUtils {

    public static List<String> parsePlaceholders(@NotNull List<String> txt, @Nullable Player player) {
        if (ClassUtils.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            txt = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, txt);
        }
        return txt;
    }

    public static String parsePlaceholders(@NotNull String txt, @Nullable Player player) {
        if (ClassUtils.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            txt = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, txt);
        }
        return txt;
    }
}
