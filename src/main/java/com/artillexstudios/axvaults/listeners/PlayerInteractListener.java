package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axvaults.commands.PlayerCommand;
import com.artillexstudios.axvaults.guis.VaultSelector;
import com.artillexstudios.axvaults.placed.PlacedVaults;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class PlayerInteractListener implements Listener {

    @EventHandler (ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getClickedBlock() == null) return;

        final Player player = event.getPlayer();
        if (event.getClickedBlock().getType() == Material.ENDER_CHEST && CONFIG.getBoolean("override-ender-chests")) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            new VaultSelector().open(player);
            return;
        }

        final Location location = event.getClickedBlock().getLocation();
        if (PlacedVaults.getVaults().containsKey(location)) {
            event.setCancelled(true);
            event.setUseInteractedBlock(Event.Result.DENY);
            event.setUseItemInHand(Event.Result.DENY);

            final Integer vault = PlacedVaults.getVaults().get(location);
            new PlayerCommand().open(player, vault, true);
        }
    }
}
