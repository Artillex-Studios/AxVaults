package com.artillexstudios.axvaults.guis;

import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;
import static com.artillexstudios.axvaults.AxVaults.MESSAGES;
import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public class VaultSelector {

    public void open(@NotNull Player player) {
        open(player, 1);
    }

    public void open(@NotNull Player player, int page) {
        int rows = CONFIG.getInt("vault-selector-rows", 6);
        int pageSize = rows * 9 - 9;
        final PaginatedGui gui = Gui.paginated()
                .title(StringUtils.format(MESSAGES.getString("guis.selector.title")))
                .rows(rows)
                .pageSize(pageSize)
                .disableAllInteractions()
                .create();

        for (int i = 0; i < pageSize * (page + 1); i++) {
            final GuiItem guiItem = getItemOfVault(player, i + 1, gui);
            if (guiItem == null) continue;
            gui.addItem(guiItem);
        }

        final GuiItem item1 = new GuiItem(new ItemBuilder(MESSAGES.getSection("gui-items.previous-page")).get());
        item1.setAction(event -> gui.previous());
        gui.setItem(rows, 3, item1);

        final GuiItem item2 = new GuiItem(new ItemBuilder(MESSAGES.getSection("gui-items.next-page")).get());
        item2.setAction(event -> {
            gui.next();

            for (int i = 0; i < pageSize; i++) {
                final GuiItem guiItem = getItemOfVault(player, (gui.getCurrentPageNum() * pageSize) + i + 1, gui);
                if (guiItem == null) continue;
                gui.addItem(guiItem);
            }
        });
        gui.setItem(rows, 7, item2);

        final GuiItem item3 = new GuiItem(new ItemBuilder(MESSAGES.getSection("gui-items.close")).get());
        item3.setAction(event -> event.getWhoClicked().closeInventory());
        gui.setItem(rows, 5, item3);

        gui.open(player, page);
    }

    @Nullable
    private GuiItem getItemOfVault(@NotNull Player player, int num, @NotNull PaginatedGui gui) {
        int maxVaults = CONFIG.getInt("max-vault-amount");
        if (maxVaults != -1 && num > maxVaults) return null;

        final HashMap<String, String> replacements = new HashMap<>();
        replacements.put("%num%", "" + num);

        final Vault vault = VaultManager.getVaultOfPlayer(player, num);
        if (vault != null) {
            replacements.put("%used%", "" + vault.getSlotsFilled());
            replacements.put("%max%", "" + vault.getStorage().getSize());

            final ItemBuilder builder = new ItemBuilder(MESSAGES.getSection("guis.selector.item-owned"));
            builder.setLore(MESSAGES.getStringList("guis.selector.item-owned.lore"), replacements);
            builder.setName(MESSAGES.getString("guis.selector.item-owned.name"), replacements);
            builder.applyItemFlags(Collections.singletonList(ItemFlag.HIDE_ATTRIBUTES));

            final ItemStack it = builder.get();

            it.setType(vault.getIcon());
            switch (CONFIG.getInt("selector-item-amount-mode", 1)) {
                case 1:
                    it.setAmount(num % 64 == 0 ? 64 : num % 64);
                    break;
                case 3:
                    it.setAmount(Math.max(1, vault.getSlotsFilled()));
                    break;
                default:
                    break;
            }

            final GuiItem guiItem = new GuiItem(it);
            guiItem.setAction(event -> {
                if (event.isShiftClick()) {
                    if (!player.hasPermission("axvaults.itempicker")) {
                        MESSAGEUTILS.sendLang(event.getWhoClicked(), "no-permission");
                        return;
                    }
                    new ItemPicker().open(player, vault, gui.getCurrentPageNum(), 1);
                    return;
                }

                MESSAGEUTILS.sendLang(event.getWhoClicked(), "vault.opened", replacements);
                vault.open(player);
            });
            return guiItem;
        } else {
            if (!CONFIG.getBoolean("show-locked-vaults", true)) return null;

            final ItemBuilder builder = new ItemBuilder(MESSAGES.getSection("guis.selector.item-locked"));
            builder.setLore(MESSAGES.getStringList("guis.selector.item-locked.lore"), replacements);
            builder.setName(MESSAGES.getString("guis.selector.item-locked.name"), replacements);

            final ItemStack it = builder.get();
            if (CONFIG.getInt("selector-item-amount-mode", 1) == 1)
                it.setAmount(num % 64 == 0 ? 64 : num % 64);

            return new GuiItem(it);
        }
    }
}
