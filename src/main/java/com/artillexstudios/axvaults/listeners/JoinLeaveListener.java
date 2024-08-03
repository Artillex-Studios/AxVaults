package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.jetbrains.annotations.NotNull;

public class JoinLeaveListener implements Listener {

    public JoinLeaveListener() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            VaultManager.loadPlayer(player.getUniqueId());
        }
    }

    @EventHandler
    public void onJoin(@NotNull PlayerJoinEvent event) {
        VaultManager.loadPlayer(event.getPlayer().getUniqueId());
    }

//    @EventHandler
//    public void onQuit(@NotNull PlayerQuitEvent event) {
//        VaultManager.removePlayer(event.getPlayer());
//    }
}
