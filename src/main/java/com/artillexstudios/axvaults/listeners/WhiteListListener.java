package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axvaults.utils.IntRange;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;
import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public class WhiteListListener implements Listener {

    @EventHandler(priority = EventPriority.LOW)
    public void onClick(InventoryClickEvent event) {
        if (CONFIG.getSection("whitelisted-items") == null) return;
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
        for (String s : CONFIG.getSection("whitelisted-items").getRoutesAsStrings(false)) {
            if (CONFIG.getString("whitelisted-items." + s + ".material") != null
                && !it.getType().toString().equalsIgnoreCase(CONFIG.getString("whitelisted-items." + s + ".material"))
            ) {
                continue;
            }

            if (CONFIG.getString("whitelisted-items." + s + ".custom-model-data") != null
                && (it.getItemMeta() == null
                || !it.getItemMeta().hasCustomModelData()
                || !IntRange.parseIntRange(CONFIG.getString("whitelisted-items." + s + ".custom-model-data")).contains(it.getItemMeta().getCustomModelData()))
            ) {
                continue;
            }

            return;
        }

        event.setCancelled(true);
        MESSAGEUTILS.sendLang(player, "banned-item");
    }
}
