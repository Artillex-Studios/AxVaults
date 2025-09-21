package com.artillexstudios.axvaults.vaults;

import com.artillexstudios.axvaults.AxVaults;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.Unmodifiable;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class VaultManager {
    private static final ConcurrentHashMap<UUID, VaultPlayer> players = new ConcurrentHashMap<>();

    public static ConcurrentHashMap<UUID, VaultPlayer> getPlayers() {
        return players;
    }

    public static CompletableFuture<VaultPlayer> getPlayer(@NotNull OfflinePlayer offlinePlayer) {
        CompletableFuture<VaultPlayer> cf = new CompletableFuture<>();

        VaultPlayer vaultPlayer = players.computeIfAbsent(offlinePlayer.getUniqueId(), VaultPlayer::new);
        if (vaultPlayer.isLoaded()) {
            return CompletableFuture.completedFuture(vaultPlayer);
        }

        AxVaults.getThreadedQueue().submit(() -> vaultPlayer.load(cf));
        return cf;
    }

    public static void cleanup(VaultPlayer vaultPlayer) {
        if (!vaultPlayer.getVaultMap().isEmpty()) return;
        if (Bukkit.getPlayer(vaultPlayer.getUUID()) != null) return;
        players.remove(vaultPlayer.getUUID());
    }

    @Unmodifiable
    public static List<Vault> getVaults() {
        ArrayList<Vault> vaults = new ArrayList<>();
        for (VaultPlayer vaultPlayer : players.values()) {
            vaults.addAll(vaultPlayer.getVaultMap().values());
        }
        return Collections.unmodifiableList(vaults);
    }

    @Nullable
    public static Vault getVault(Inventory inventory) {
        for (VaultPlayer vaultPlayer : players.values()) {
            for (Vault vault : vaultPlayer.getVaultMap().values()) {
                if (inventory.equals(vault.getStorage())) return vault;
            }
        }
        return null;
    }

    public static boolean removeVault(@NotNull Vault vault) {
        VaultPlayer vaultPlayer = vault.getVaultPlayer();
        boolean success = vaultPlayer.removeVault(vault) != null;
        if (success) cleanup(vaultPlayer);
        return success;
    }
}
