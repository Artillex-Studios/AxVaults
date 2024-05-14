package com.artillexstudios.axvaults.vaults;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;
import java.util.WeakHashMap;

public class VaultManager {
    private static final HashMap<UUID, VaultPlayer> players = new HashMap<>();
    private static final WeakHashMap<Vault, Void> vaults = new WeakHashMap<>();

    public static void addPlayer(@NotNull UUID uuid) {
        final VaultPlayer vaultPlayer = new VaultPlayer(uuid);
        players.put(uuid, vaultPlayer);
    }

    public static VaultPlayer getPlayer(@NotNull UUID uuid) {
        if (players.containsKey(uuid)) return players.get(uuid);
        final VaultPlayer vaultPlayer = new VaultPlayer(uuid);
        players.put(uuid, vaultPlayer);
        vaultPlayer.loadSync();
        return vaultPlayer;
    }

    public static void removePlayer(@NotNull Player player) {
        final VaultPlayer vaultPlayer = players.remove(player.getUniqueId());
        vaultPlayer.save();
    }

    @Nullable
    public static Vault getVaultOfPlayer(@NotNull Player player, int num) {
        final VaultPlayer vaultPlayer = players.get(player.getUniqueId());
        return vaultPlayer.getVault(num);
    }

    public static HashMap<UUID, VaultPlayer> getPlayers() {
        return players;
    }

    public static void addVault(@NotNull Vault vault) {
        players.get(vault.getUUID()).addVault(vault);
    }

    public static void removeVault(@NotNull Vault vault) {
        players.get(vault.getUUID()).removeVault(vault);
    }

    public static int getVaultsOfPlayer(@NotNull Player player) {
        if (!players.containsKey(player.getUniqueId())) return 0;
        return players.get(player.getUniqueId()).getVaultMap().values().size();
    }

    public static void reload() {
        for (VaultPlayer vaultPlayer : players.values()) {
            for (Vault vault : vaultPlayer.getVaultMap().values()) {
                vault.reload();
            }
        }
    }

    public static WeakHashMap<Vault, Void> getVaults() {
        return vaults;
    }
}
