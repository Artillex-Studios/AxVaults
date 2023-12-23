package com.artillexstudios.axvaults.vaults;

import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.UUID;

public class VaultManager {
    private static final HashMap<UUID, VaultPlayer> players = new HashMap<>();

    public static void addPlayer(@NotNull UUID uuid) {
        players.put(uuid, new VaultPlayer(uuid));
    }

    public static VaultPlayer getPlayer(@NotNull UUID uuid) {
        if (players.containsKey(uuid)) return players.get(uuid);
        final VaultPlayer vaultPlayer = new VaultPlayer(uuid);
        players.put(uuid, vaultPlayer);
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
        if (!players.containsKey(vault.getUUID())) return;
        players.get(vault.getUUID()).addVault(vault);
    }

    public static int getVaultsOfPlayer(@NotNull Player player) {
        if (!players.containsKey(player.getUniqueId())) return 0;
        return players.get(player.getUniqueId()).getVaultMap().values().size();
    }
}
