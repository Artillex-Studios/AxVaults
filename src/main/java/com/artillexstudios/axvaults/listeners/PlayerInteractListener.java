package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axvaults.commands.subcommands.Open;
import com.artillexstudios.axvaults.placed.PlacedVaults;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

public class PlayerInteractListener implements Listener {

    @EventHandler (ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();

        if (!PlacedVaults.getVaults().containsKey(location)) return;
        event.setCancelled(true);

        Integer vault = PlacedVaults.getVaults().get(location);
        Open.INSTANCE.execute(player, vault, true);
    }
}
