package com.artillexstudios.axvaults.guis;

import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.block.implementation.Section;
import com.artillexstudios.axapi.reflection.ClassUtils;
import com.artillexstudios.axapi.utils.ItemBuilder;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.vaults.VaultManager;
import dev.triumphteam.gui.guis.Gui;
import dev.triumphteam.gui.guis.GuiItem;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.function.Consumer;

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

        String title = MESSAGES.getString("guis.selector.title");
        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            title = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(player, title);
        }

        final PaginatedGui gui = Gui.paginated()
                .title(StringUtils.format(title))
                .rows(rows)
                .pageSize(pageSize)
                .disableAllInteractions()
                .create();

        for (int i = 0; i < pageSize * (page + 1); i++) {
            getItemOfVault(player, i + 1, gui, guiItem -> {
                if (guiItem == null) return;
                gui.addItem(guiItem);
            });
        }

        boolean hidePagination = CONFIG.getBoolean("hide-pagination", false) && gui.getPagesNum() <= 1;

        if (!hidePagination) {
            final Section prev;
            if ((prev = MESSAGES.getSection("gui-items.previous-page")) != null) {
                final GuiItem item1 = new GuiItem(new ItemBuilder(prev).get());
                item1.setAction(event -> gui.previous());
                gui.setItem(rows, 3, item1);
            }

            final Section next;
            if ((next = MESSAGES.getSection("gui-items.next-page")) != null) {
                final GuiItem item2 = new GuiItem(new ItemBuilder(next).get());
                item2.setAction(event -> {
                    gui.next();

                    for (int i = 0; i < pageSize; i++) {
                        getItemOfVault(player, (gui.getCurrentPageNum() * pageSize) + i + 1, gui, guiItem -> {
                            if (guiItem == null) return;
                            gui.addItem(guiItem);
                        });
                    }
                });
                gui.setItem(rows, 7, item2);
            }
        }

        final Section close;
        if ((close = MESSAGES.getSection("gui-items.close")) != null) {
            final GuiItem item3 = new GuiItem(new ItemBuilder(close).get());
            item3.setAction(event -> event.getWhoClicked().closeInventory());
            gui.setItem(rows, 5, item3);
        }

        gui.open(player, page);
    }


    private void getItemOfVault(@NotNull Player player, int num, @NotNull PaginatedGui gui, Consumer<GuiItem> consumer) {
        int maxVaults = CONFIG.getInt("max-vault-amount");
        if (maxVaults != -1 && num > maxVaults) {
            consumer.accept(null);
            return;
        }

        final HashMap<String, String> replacements = new HashMap<>();
        replacements.put("%num%", "" + num);

        VaultManager.getVaultOfPlayer(player, num, vault -> {
            if (vault != null) {
                replacements.put("%used%", "" + vault.getSlotsFilled());
                replacements.put("%max%", "" + vault.getStorage().getSize());

                final ItemBuilder builder = new ItemBuilder(MESSAGES.getSection("guis.selector.item-owned"));
                builder.setLore(MESSAGES.getStringList("guis.selector.item-owned.lore"), replacements);
                builder.setName(MESSAGES.getString("guis.selector.item-owned.name"), replacements);

                final ItemStack it = builder.get();
                if (it.hasItemMeta()) {
                    final ItemMeta meta = it.getItemMeta();
                    meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
                    it.setItemMeta(meta);
                }

                it.setType(vault.getIcon());
                switch (CONFIG.getInt("selector-item-amount-mode", 1)) {
                    case 1 -> it.setAmount(num % 64 == 0 ? 64 : num % 64);
                    case 3 -> it.setAmount(Math.max(1, vault.getSlotsFilled()));
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
                consumer.accept(guiItem);
            } else {
                if (!CONFIG.getBoolean("show-locked-vaults", true)) {
                    consumer.accept(null);
                    return;
                }

                final ItemBuilder builder = new ItemBuilder(MESSAGES.getSection("guis.selector.item-locked"));
                builder.setLore(MESSAGES.getStringList("guis.selector.item-locked.lore"), replacements);
                builder.setName(MESSAGES.getString("guis.selector.item-locked.name"), replacements);

                final ItemStack it = builder.get();
                if (CONFIG.getInt("selector-item-amount-mode", 1) == 1)
                    it.setAmount(num % 64 == 0 ? 64 : num % 64);

                consumer.accept(new GuiItem(it));
            }
        });
    }
}
