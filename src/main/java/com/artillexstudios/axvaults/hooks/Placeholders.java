package com.artillexstudios.axvaults.hooks;

import org.bukkit.OfflinePlayer;

import java.util.List;

public interface Placeholders {

    default String setPlaceholders(OfflinePlayer player, String txt) {
        return txt.replace("%player%", player.getName());
    }

    default List<String> setPlaceholders(OfflinePlayer player, List<String> txt) {
        txt.replaceAll(s -> s.replace("%player%", player.getName()));
        return txt;
    }
}
