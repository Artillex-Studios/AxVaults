package com.artillexstudios.axvaults.vaults;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.hooks.HookManager;
import com.artillexstudios.axvaults.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.artillexstudios.axvaults.AxVaults.MESSAGES;

public class Vault {
    private final VaultPlayer vaultPlayer;
    private Inventory storage;
    private final int id;
    private Material icon;
    private long lastOpen = System.currentTimeMillis();
    private final AtomicBoolean changed = new AtomicBoolean(false);

    public Vault(VaultPlayer vaultPlayer, int id, Material icon, @Nullable ItemStack[] contents) {
        this.vaultPlayer = vaultPlayer;
        this.id = id;

        this.storage = Bukkit.createInventory(null, vaultPlayer.getRows() * 9, getTitle());
        if (contents != null) setContents(contents);
        vaultPlayer.getVaultMap().put(id, this);

        this.icon = icon;
    }

    @ApiStatus.Internal
    public void setContents(ItemStack[] items) {
        if (storage.getSize() < items.length) {
            for (int i = 0; i < storage.getSize(); i++) {
                storage.setItem(i, items[i]);
            }
            Player player = Bukkit.getPlayer(vaultPlayer.getUUID());
            for (int i = storage.getSize(); i < items.length; i++) {
                HashMap<Integer, ItemStack> remaining = storage.addItem(items[i]);
                if (player != null) remaining.forEach((k, v) -> player.getLocation().getWorld().dropItem(player.getLocation(), v));
            }
            return;
        }
        storage.setContents(items);
    }

    public Inventory getStorage() {
        return storage;
    }

    public VaultPlayer getVaultPlayer() {
        return vaultPlayer;
    }

    public UUID getUUID() {
        return vaultPlayer.getUUID();
    }

    public int getId() {
        return id;
    }

    public long getLastOpen() {
        return lastOpen;
    }

    public AtomicBoolean hasChanged() {
        return changed;
    }

    public void setIcon(Material icon) {
        changed.set(true);
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
        changed.set(true);
        if (vaultPlayer.getRows() != storage.getSize()) {
            reload();
        }
        player.openInventory(storage);
        SoundUtils.playSound(player, MESSAGES.getString("sounds.open"));
        lastOpen = System.currentTimeMillis();
        // todo: on close reopen selector (only when it was used)
    }

    public boolean isOpened() {
        return !storage.getViewers().isEmpty();
    }

    public void reload() {
        Inventory newStorage = Bukkit.createInventory(null, vaultPlayer.getRows() * 9, getTitle());
        ItemStack[] contents = storage.getContents();

        List<HumanEntity> viewers = new ArrayList<>(storage.getViewers());
        Iterator<HumanEntity> viewerIterator = viewers.iterator();

        while (viewerIterator.hasNext()) {
            viewerIterator.next().openInventory(newStorage);
            viewerIterator.remove();
        }

        storage.clear();
        storage = newStorage;
        setContents(contents);
    }

    private String getTitle() {
        String title = MESSAGES.getString("guis.vault.title").replace("%num%", "" + id);
        title = HookManager.getPlaceholderParser().setPlaceholders(Bukkit.getOfflinePlayer(vaultPlayer.getUUID()), title);
        return StringUtils.formatToString(title);
    }

    @Override
    public String toString() {
        return "Vault{" +
                "vaultPlayer=" + vaultPlayer.getUUID() +
                ", changed=" + changed +
                ", lastOpen=" + lastOpen +
                ", icon=" + icon +
                ", id=" + id +
                ", storage=" + getSlotsFilled() + "/" + storage.getSize() +
                '}';
    }
}
