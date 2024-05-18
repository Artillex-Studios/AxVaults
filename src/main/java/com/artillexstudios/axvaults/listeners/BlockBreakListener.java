package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.placed.PlacedVaults;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public class BlockBreakListener implements Listener {

    @EventHandler (ignoreCancelled = true, priority = EventPriority.HIGHEST)
    public void onBreak(@NotNull BlockBreakEvent event) {
        if (!PlacedVaults.getVaults().containsKey(event.getBlock().getLocation())) return;
        event.setCancelled(true);
        AxVaults.getThreadedQueue().submit(() -> {
            PlacedVaults.removeVault(event.getBlock().getLocation());
            MESSAGEUTILS.sendLang(event.getPlayer(), "set.removed");
        });
    }
}
