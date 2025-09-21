package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axapi.utils.PaperUtils;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.vaults.Vault;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class InventoryClickListener implements Listener {

    @EventHandler
    public void onInventoryClickEvent(InventoryClickEvent event) {
        if (!(PaperUtils.getHolder(event.getInventory(), false) instanceof Vault vault)) {
            return;
        }

        if (AxVaults.stopping) {
            event.setCancelled(true);
            return;
        }
    }
}
