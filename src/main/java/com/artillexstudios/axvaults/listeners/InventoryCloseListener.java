package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axapi.utils.PaperUtils;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.database.impl.MySQL;
import com.artillexstudios.axvaults.utils.SoundUtils;
import com.artillexstudios.axvaults.utils.VaultUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.jetbrains.annotations.NotNull;

import java.util.Map;

import static com.artillexstudios.axvaults.AxVaults.MESSAGES;
import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public class InventoryCloseListener implements Listener {

    @EventHandler
    public void onClose(@NotNull InventoryCloseEvent event) {
        if (!(PaperUtils.getHolder(event.getInventory(), false) instanceof Vault vault)) return;

        AxVaults.getThreadedQueue().submit(() -> {
            MESSAGEUTILS.sendLang(event.getPlayer(), "vault.closed", Map.of("%num%", "" + vault.getId()));
            SoundUtils.playSound((Player) event.getPlayer(), MESSAGES.getString("sounds.close"));
        });
    }
}
