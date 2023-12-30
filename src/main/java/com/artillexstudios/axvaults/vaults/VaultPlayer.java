package com.artillexstudios.axvaults.vaults;

import com.artillexstudios.axvaults.AxVaults;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

public class VaultPlayer {
    private final UUID uuid;
    private final HashMap<Integer, Vault> vaultMap = new HashMap<>();

    public VaultPlayer(UUID uuid) {
        this.uuid = uuid;

        AxVaults.getThreadedQueue().submit(() -> AxVaults.getDatabase().loadVaults(uuid));
    }

    public HashMap<Integer, Vault> getVaultMap() {
        return vaultMap;
    }

    @Nullable
    public Vault getVault(int num) {
        final Player player = Bukkit.getPlayer(uuid);
        if (player != null) {
            if (!player.hasPermission("axvaults.vault." + num)) return null;
            if (!vaultMap.containsKey(num)) return addVault(new Vault(uuid, num, null));
        }
        if (!vaultMap.containsKey(num)) return null;
        return vaultMap.get(num);
    }

    @NotNull
    public Vault addVault(@NotNull Vault vault) {
        if (vaultMap.containsKey(vault.getId())) return vaultMap.get(vault.getId());
        vaultMap.put(vault.getId(), vault);
        return vault;
    }

    public void save() {
        AxVaults.getThreadedQueue().submit(() -> {
            for (Vault vault : vaultMap.values()) {
                AxVaults.getDatabase().saveVault(vault);
            }
        });

        for (Vault vault : vaultMap.values()) {
            close(vault.getStorage());
        }
    }

    private void close(@NotNull Inventory inventory) {
        final List<HumanEntity> viewers = new ArrayList<>(inventory.getViewers());
        final Iterator<HumanEntity> viewerIterator = viewers.iterator();

        while (viewerIterator.hasNext()) {
            viewerIterator.next().closeInventory();
            viewerIterator.remove();
        }
    }
}
