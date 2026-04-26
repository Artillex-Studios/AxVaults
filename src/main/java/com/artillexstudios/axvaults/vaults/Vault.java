package com.artillexstudios.axvaults.vaults;

import com.artillexstudios.axapi.utils.ContainerUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.hooks.HookManager;
import com.artillexstudios.axvaults.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import static com.artillexstudios.axvaults.AxVaults.MESSAGES;

public class Vault implements InventoryHolder {
    private final VaultPlayer vaultPlayer;
    private Inventory storage;
    private final int id;
    private Material icon;
    private long lastOpen = System.currentTimeMillis();
    private final AtomicBoolean changed = new AtomicBoolean(false);
    private final List<ItemStack> overflow = new ArrayList<>();

    public Vault(VaultPlayer vaultPlayer, int id, Material icon, @Nullable ItemStack[] contents) {
        this.vaultPlayer = vaultPlayer;
        this.id = id;

        this.storage = Bukkit.createInventory(this, vaultPlayer.getRows() * 9, getTitle());
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
            // if the items don't fit in the storage, add remaining to the overflow list
            for (int i = storage.getSize(); i < items.length; i++) {
                if (items[i] == null) continue;
                overflow.add(items[i]);
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
        if (AxVaults.isStopping()) {
            // prevent opening vaults when the plugin is shutting down
            return;
        }

        changed.set(true);
        // recalculate vault if the row count has changed
        if (vaultPlayer.getRows() * 9 != storage.getSize()) {
            reload();
        }

        // give back overflow items that couldn't fit in the vault
        if (!overflow.isEmpty()) {
            dropOverFlow(player);
        }

        player.openInventory(storage);
        SoundUtils.playSound(player, MESSAGES.getString("sounds.open"));
        lastOpen = System.currentTimeMillis();
    }

    private void dropOverFlow(@NotNull Player player) {
        changed.set(true);
        for (Iterator<ItemStack> it = overflow.iterator(); it.hasNext(); ) {
            ItemStack itemStack = it.next();
            it.remove();
            // first try to re-add items to the vault, if it fails, give them back to the player
            ContainerUtils.INSTANCE.addOrDrop(storage, List.of(itemStack), player.getLocation());
        }
    }

    public boolean isOpened() {
        return !storage.getViewers().isEmpty();
    }

    public void reload() {
        Inventory newStorage = Bukkit.createInventory(this, vaultPlayer.getRows() * 9, getTitle());
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

    @NotNull
    @Override
    public Inventory getInventory() {
        return this.storage;
    }
}
