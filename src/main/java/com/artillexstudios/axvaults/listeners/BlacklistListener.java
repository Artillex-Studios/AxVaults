package com.artillexstudios.axvaults.listeners;

import com.artillexstudios.axapi.utils.PaperUtils;
import com.artillexstudios.axvaults.utils.BlacklistUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public class BlacklistListener implements Listener {

    @EventHandler
    public void onClick(@NotNull InventoryClickEvent event) {
        if (!(PaperUtils.getHolder(event.getInventory(), false) instanceof Vault))
            return;

        if (BlacklistUtils.isBlacklisted(this.getItem(event))) {

            // Only blacklist the storing, we can get newly blacklisted items back
            if (!isStoringIntoVault(event))
                return;

            event.setCancelled(true);
            MESSAGEUTILS.sendLang(event.getWhoClicked(), "banned-item");
        }
    }

    private boolean isStoringIntoVault(@NotNull InventoryClickEvent event) {
        Inventory clickedInventory = event.getClickedInventory();
        if (clickedInventory == null)
            return false;

        Inventory topInventory = event.getView().getTopInventory();
        InventoryAction action = event.getAction();

        if (action == InventoryAction.MOVE_TO_OTHER_INVENTORY)
            return clickedInventory != topInventory;

        if (clickedInventory == topInventory) {
            return action == InventoryAction.PLACE_ALL
                    || action == InventoryAction.PLACE_ONE
                    || action == InventoryAction.PLACE_SOME
                    || action == InventoryAction.SWAP_WITH_CURSOR
                    || action == InventoryAction.HOTBAR_SWAP
                    || action == InventoryAction.HOTBAR_MOVE_AND_READD;
        } else {
            return action == InventoryAction.PICKUP_ALL
                    || action == InventoryAction.PICKUP_HALF
                    || action == InventoryAction.PICKUP_SOME
                    || action == InventoryAction.PICKUP_ONE;
        }
    }

    @Nullable
    private ItemStack getItem(InventoryClickEvent event) {
        if (event.getClickedInventory() != null) {
            if (event.getClick() == ClickType.SWAP_OFFHAND && event.getClickedInventory().getType() != InventoryType.PLAYER) {
                return event.getWhoClicked().getInventory().getItemInOffHand();
            }
            if (event.getClick() == ClickType.NUMBER_KEY) {
                // when using a number key, the game will move it from the another inventory, so use the opposite of the clicked inventory
                Inventory inventory = event.getClickedInventory().getType() == InventoryType.PLAYER ? event.getView().getTopInventory() : event.getView().getBottomInventory();
                return inventory.getItem(event.getHotbarButton());
            }
        }
        return event.getCurrentItem();
    }
}
