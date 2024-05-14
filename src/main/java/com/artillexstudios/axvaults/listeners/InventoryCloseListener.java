package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axvaults.utils.SoundUtils;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axvaults.AxVaults.MESSAGES;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent event) {
        if (VaultManager.getVaults().keySet().stream().filter(vault -> vault.getStorage().equals(event.getInventory())).findAny().isEmpty()) return;
        SoundUtils.playSound((Player) event.getPlayer(), MESSAGES.getString("sounds.open"));
    }
}
