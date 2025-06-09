package com.artillexstudios.axvaults.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import org.bukkit.OfflinePlayer;

import java.util.List;

public class PlaceholderAPIParser implements Placeholders {

    @Override
    public String setPlaceholders(OfflinePlayer player, String txt) {
        return PlaceholderAPI.setPlaceholders(player, txt);
    }

    @Override
    public List<String> setPlaceholders(OfflinePlayer player, List<String> txt) {
        return PlaceholderAPI.setPlaceholders(player, txt);
    }
}
