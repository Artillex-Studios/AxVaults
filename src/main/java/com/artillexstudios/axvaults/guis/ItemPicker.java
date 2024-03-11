package com.artillexstudios.axvaults.guis;

import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.Objects;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;
import static com.artillexstudios.axvaults.AxVaults.MESSAGES;

public class ItemPicker {

    public void open(@NotNull Player player, @NotNull Vault vault) {
        open(player, vault, 1, 1);
    }

    public void open(@NotNull Player player, @NotNull Vault vault, int oldPage, int cPage) {
        int rows = CONFIG.getInt("vault-selector-rows", 6);
        int pageSize = rows * 9 - 9;
        final PaginatedGui gui = Gui.paginated()
                .title(StringUtils.format(MESSAGES.getString("guis.item-picker.title")))
                .rows(rows)
                .pageSize(pageSize)
                .disableAllInteractions()
                .create();

        for (Material material : Material.values()) {
            if (material.equals(Material.AIR) || material.equals(Material.CAVE_AIR) || material.equals(Material.VOID_AIR)) continue;
            final GuiItem guiItem = new GuiItem(new ItemBuilder(material).applyItemFlags(Collections.singletonList(ItemFlag.HIDE_ATTRIBUTES)).glow(Objects.equals(vault.getIcon(), material)).get());
            guiItem.setAction(event -> {
                if (vault.getIcon().equals(material)) vault.setIcon(null);
                else vault.setIcon(material);
                open(player, vault, oldPage, gui.getCurrentPageNum());
            });
            gui.addItem(guiItem);
        }

        final GuiItem item1 = new GuiItem(new ItemBuilder(MESSAGES.getSection("gui-items.previous-page")).get());
        item1.setAction(event -> gui.previous());
        gui.setItem(rows, 3, item1);

        final GuiItem item2 = new GuiItem(new ItemBuilder(MESSAGES.getSection("gui-items.next-page")).get());
        item2.setAction(event -> gui.next());
        gui.setItem(rows, 7, item2);

        final GuiItem item3 = new GuiItem(new ItemBuilder(MESSAGES.getSection("gui-items.back")).get());
        item3.setAction(event -> new VaultSelector().open(player, oldPage));
        gui.setItem(rows, 5, item3);

        gui.open(player, cPage);
    }
}
