package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axapi.utils.Cooldown;
import com.artillexstudios.axvaults.commands.subcommands.Open;
import com.artillexstudios.axvaults.placed.PlacedVaults;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class PlayerInteractListener implements Listener {
    private static final Cooldown<Player> cooldown = Cooldown.create();
    private static final List<Material> selectorMaterials = new ArrayList<>();

    public PlayerInteractListener() {
        reload();
    }

    public static void reload() {
        selectorMaterials.clear();
        for (String type : CONFIG.getStringList("vault-selector-blocks")) {
            Material material = Material.matchMaterial(type);
            selectorMaterials.add(material);
        }
    }

    @EventHandler (ignoreCancelled = true)
    public void onInteract(@NotNull PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (event.getClickedBlock() == null) return;

        Player player = event.getPlayer();
        Location location = event.getClickedBlock().getLocation();

        Integer vault = null;
        if (PlacedVaults.getVaults().containsKey(location)) {
            vault = PlacedVaults.getVaults().get(location);
        } else if (!selectorMaterials.contains(event.getClickedBlock().getType())) {
            return;
        }
        event.setCancelled(true);
        if (cooldown.hasCooldown(player)) return;
        cooldown.addCooldown(player, 100L);
        Open.INSTANCE.execute(player, vault, true);
    }
}
