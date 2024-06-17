package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.utils.SoundUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Map;

import static com.artillexstudios.axvaults.AxVaults.MESSAGES;
import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent event) {
        AxVaults.getThreadedQueue().submit(() -> {
            Vault v = null;
            for (Vault vault : new ArrayList<>(VaultManager.getVaults())) {
                if (!vault.getStorage().equals(event.getInventory())) continue;
                v = vault;
                break;
            }
            if (v == null) return;
            MESSAGEUTILS.sendLang(event.getPlayer(), "vault.closed", Map.of("%num%", "" + v.getId()));
            SoundUtils.playSound((Player) event.getPlayer(), MESSAGES.getString("sounds.close"));
        });
    }
}
