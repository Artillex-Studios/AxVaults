package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axapi.utils.PaperUtils;
import com.artillexstudios.axvaults.utils.BlacklistUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public class BlacklistListener implements Listener {

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent event) {
        if (!(PaperUtils.getHolder(event.getInventory(), false) instanceof Vault)) {
            return;
        }

        if (isBlacklisted(event)) {
            event.setCancelled(true);
            MESSAGEUTILS.sendLang(event.getWhoClicked(), "banned-item");
        }
    }

    private boolean isBlacklisted(InventoryClickEvent event) {
        ItemStack hotbarItem = null;
        if (event.getClick() == ClickType.NUMBER_KEY) {
            hotbarItem = event.getWhoClicked().getInventory().getItem(event.getHotbarButton());
        } else if (event.getClick() == ClickType.SWAP_OFFHAND) {
            hotbarItem = event.getWhoClicked().getInventory().getItemInOffHand();
        }
        return BlacklistUtils.isBlacklisted(event.getCurrentItem()) || BlacklistUtils.isBlacklisted(hotbarItem);
    }
}