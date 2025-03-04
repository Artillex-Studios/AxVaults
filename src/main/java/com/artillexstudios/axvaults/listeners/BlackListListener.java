package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axvaults.utils.IntRange;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;
import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public class BlackListListener implements Listener {

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent event) {
        if (CONFIG.getSection("blacklisted-items") == null) return;
        boolean isVault = false;
        for (Vault vault : VaultManager.getVaults()) {
            if (vault.getStorage().equals(event.getView().getTopInventory())) {
                isVault = true;
                break;
            }
        }
        if (!isVault) return;

        final Player player = (Player) event.getWhoClicked();
        final ItemStack it = event.getClick() == ClickType.NUMBER_KEY ? player.getInventory().getItem(event.getHotbarButton()) : event.getCurrentItem();
        if (it == null) return;
        for (String s : CONFIG.getSection("blacklisted-items").getRoutesAsStrings(false)) {
            boolean banned = false;

            if (CONFIG.getString("blacklisted-items." + s + ".material") != null) {
                if (!it.getType().toString().equalsIgnoreCase(CONFIG.getString("blacklisted-items." + s + ".material"))) continue;
                banned = true;
            }

            if (CONFIG.getString("blacklisted-items." + s + ".custom-model-data") != null) {
                if (it.getItemMeta() == null
                    || !it.getItemMeta().hasCustomModelData()
                    || !IntRange.parseIntRange(CONFIG.getString("blacklisted-items." + s + ".custom-model-data")).contains(it.getItemMeta().getCustomModelData())
                ) {
                    continue;
                }

                banned = true;
            }

            if (CONFIG.getString("blacklisted-items." + s + ".name-contains") != null) {
                if (it.getItemMeta() == null) continue;
                if (!it.getItemMeta().getDisplayName().contains(CONFIG.getString("blacklisted-items." + s + ".name-contains"))) continue;
                banned = true;
            }

            if (banned) {
                event.setCancelled(true);
                MESSAGEUTILS.sendLang(player, "banned-item");
                return;
            }
        }
    }
}
