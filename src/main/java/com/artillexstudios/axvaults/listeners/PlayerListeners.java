package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.database.impl.MySQL;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerListeners implements Listener {

    public PlayerListeners() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            VaultManager.loadPlayer(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        VaultManager.loadPlayer(event.getPlayer().getUniqueId());
        if (AxVaults.getDatabase() instanceof MySQL db) db.checkForChanges();
    }

    @EventHandler
    public void onQuit(@NotNull PlayerQuitEvent event) {
        VaultManager.getPlayer(event.getPlayer().getUniqueId(), vaultPlayer -> {
            vaultPlayer.save();
            if (AxVaults.getDatabase() instanceof MySQL db) db.checkForChanges();
        });
//        VaultManager.removePlayer(event.getPlayer());
    }
}
