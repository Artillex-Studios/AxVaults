package com.artillexstudios.axvaults.vaults;

import com.artillexstudios.axapi.reflection.ClassUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.utils.SoundUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

import static com.artillexstudios.axvaults.AxVaults.MESSAGES;

public class Vault {
    private final UUID uuid;
    private Inventory storage;
    private final int id;
    private Material icon;
    private VaultPlayer vaultPlayer;
    private long lastOpen = System.currentTimeMillis();
    private CompletableFuture<Void> future = new CompletableFuture<>();

    public Vault(UUID uuid, int num, Material icon) {
        this.uuid = uuid;
        this.id = num;

        VaultManager.getPlayer(uuid, vaultPlayer -> {
            this.vaultPlayer = vaultPlayer;
            String title = MESSAGES.getString("guis.vault.title").replace("%num%", "" + num);
            if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
                title = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), title);
            }
            this.storage = Bukkit.createInventory(null, vaultPlayer.getRows() * 9, StringUtils.formatToString(title));
            future.complete(null);
            future = null;
            vaultPlayer.getVaultMap().put(num, this);
        });

        this.icon = icon;
        VaultManager.getVaults().add(this);
    }

    public void setContents(ItemStack[] items, Consumer<Void> consumer) {
        if (future == null) {
            setContents(items);
            consumer.accept(null);
            return;
        }
        future.thenRun(() -> {
            setContents(items);
            consumer.accept(null);
        });
    }

    private void setContents(ItemStack[] items) {
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

    public UUID getUUID() {
        return uuid;
    }

    public int getId() {
        return id;
    }

    public long getLastOpen() {
        return lastOpen;
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
        String title = MESSAGES.getString("guis.vault.title").replace("%num%", "" + id);
        if (ClassUtils.INSTANCE.classExists("me.clip.placeholderapi.PlaceholderAPI")) {
            title = me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(Bukkit.getOfflinePlayer(uuid), title);
        }

        final Inventory newStorage = Bukkit.createInventory(null, vaultPlayer.getRows() * 9, StringUtils.formatToString(title));
        final ItemStack[] contents = storage.getContents();

        final List<HumanEntity> viewers = new ArrayList<>(storage.getViewers());
        final Iterator<HumanEntity> viewerIterator = viewers.iterator();

        while (viewerIterator.hasNext()) {
            viewerIterator.next().openInventory(newStorage);
            viewerIterator.remove();
        }

        storage = newStorage;
        storage.clear();
        setContents(contents);
    }

    public void handleNextVaultArrow(Player player) {
        int nextVaultNum = getNextVaultNumber(player);
        if (nextVaultNum != -1) {
            VaultManager.getVaultOfPlayer(player, nextVaultNum, vault -> {
                if (vault != null) {
                    vault.open(player);
                }
            });
        } else {
            player.sendMessage(MESSAGES.getString("vault.no-next-vault"));
        }
    }

    private int getNextVaultNumber(@NotNull Player player) {
        int maxVaults = CONFIG.getInt("max-vault-amount");
        for (int i = 1; i <= maxVaults; i++) {
            if (!VaultManager.getVaultOfPlayer(player, i, vault -> {})) {
                return i;
            }
        }
        return -1;
    }
}
