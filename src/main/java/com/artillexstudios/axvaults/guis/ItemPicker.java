package com.artillexstudios.axvaults.guis;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.reflection.ClassUtils;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.utils.SoundUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;
import static com.artillexstudios.axvaults.AxVaults.MESSAGES;

public class ItemPicker {

    public void open(@NotNull Player player, @NotNull Vault vault) {
        open(player, vault, 1, 1);
    }

    public void open(@NotNull Player player, @NotNull Vault vault, int oldPage, int cPage) {
        int rows = CONFIG.getInt("item-picker-rows", 6);
        int pageSize = rows * 9 - 9;

        String title = MESSAGES.getString("guis.item-picker.title");
        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            title = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, title);
        }

        final PaginatedGui gui = Gui.paginated()
                .title(StringUtils.format(title))
                .rows(rows)
                .pageSize(pageSize)
                .disableAllInteractions()
                .create();

        for (Material material : Material.values()) {
            ItemStack it = null;
            try {
                it = new ItemBuilder(material).glow(Objects.equals(vault.getIcon(), material)).get();
            } catch (Exception ignored) {}
            if (it == null) continue;
            final ItemMeta meta = it.hasItemMeta() ? it.getItemMeta() : Bukkit.getItemFactory().getItemMeta(it.getType());
            if (meta == null) continue;

            meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
            it.setItemMeta(meta);

            final GuiItem guiItem = new GuiItem(it);
            guiItem.setAction(event -> {
                if (vault.getIcon().equals(material)) vault.setIcon(null);
                else vault.setIcon(material);
                SoundUtils.playSound(player, MESSAGES.getString("sounds.select-icon"));
                if (CONFIG.getBoolean("selector-stay-open", true))
                    open(player, vault, oldPage, gui.getCurrentPageNum());
                else
                    new VaultSelector().open(player, oldPage);
            });
            gui.addItem(guiItem);
        }

        final Section prev;
        if ((prev = MESSAGES.getSection("gui-items.previous-page")) != null) {
            final GuiItem item1 = new GuiItem(new ItemBuilder(prev).get());
            item1.setAction(event -> gui.previous());
            gui.setItem(rows, 3, item1);
        }

        final Section next;
        if ((next = MESSAGES.getSection("gui-items.next-page")) != null) {
            final GuiItem item2 = new GuiItem(new ItemBuilder(next).get());
            item2.setAction(event -> gui.next());
            gui.setItem(rows, 7, item2);
        }

        final Section back;
        if ((back = MESSAGES.getSection("gui-items.back")) != null) {
            final GuiItem item3 = new GuiItem(new ItemBuilder(back).get());
            item3.setAction(event -> new VaultSelector().open(player, oldPage));
            gui.setItem(rows, 5, item3);
        }

        gui.open(player, cPage);
    }
}
