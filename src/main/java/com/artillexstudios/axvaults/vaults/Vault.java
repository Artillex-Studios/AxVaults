package com.artillexstudios.axvaults.vaults;

import com.artillexstudios.axapi.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;
import static com.artillexstudios.axvaults.AxVaults.MESSAGES;

public class Vault {
    private final UUID uuid;
    private final Inventory storage;
    private final int id;
    private Material icon;

    public Vault(UUID uuid, int num, Material icon) {
        this.uuid = uuid;
        this.id = num;
        this.storage = Bukkit.createInventory(null, CONFIG.getInt("vault-storage-rows", 54) * 9, StringUtils.formatToString(MESSAGES.getString("guis.vault.title").replace("%num%", "" + num)));
        this.icon = icon;
    }

    public Vault(UUID uuid, int num, ItemStack[] items, Material icon) {
        this.uuid = uuid;
        this.id = num;
        this.storage = Bukkit.createInventory(null, CONFIG.getInt("vault-storage-rows", 54) * 9, StringUtils.formatToString(MESSAGES.getString("guis.vault.title").replace("%num%", "" + num)));
        this.icon = icon;
        storage.setContents(items);
    }

    public Inventory getStorage() {
        return storage;
    }

    public UUID getUUID() {
        return uuid;
    }

    public int getId() {
        return id;
    }

    public void setIcon(Material icon) {
        this.icon = icon;
    }

    public Material getIcon() {
        if (icon == null) return Material.valueOf(MESSAGES.getString("guis.selector.item-owned.material", "BARREL"));
        return icon;
    }

    @Nullable
    public Material getRealIcon() {
        return icon;
    }

    public int getSlotsFilled() {
        int am = 0;
        for (ItemStack it : storage.getContents()) {
            if (it == null) continue;
            am++;
        }
        return am;
    }

    public void open(@NotNull Player player) {
        player.openInventory(storage);
    }
}
