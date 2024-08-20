package com.artillexstudios.axvaults.vaults;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axvaults.AxVaults;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class VaultManager {
    private static final ConcurrentHashMap<UUID, VaultPlayer> players = new ConcurrentHashMap<>();
    private static final ConcurrentLinkedQueue<Vault> vaults = new ConcurrentLinkedQueue<>();

    public static void loadPlayer(@NotNull UUID uuid) {
        getPlayer(uuid, vaultPlayer -> {});
    }

    public static void getPlayer(@NotNull UUID uuid, Consumer<VaultPlayer> consumer) {
        VaultPlayer vaultPlayer = players.get(uuid);
        if (vaultPlayer != null) {
            consumer.accept(vaultPlayer);
            return;
        }
        vaultPlayer = new VaultPlayer(uuid);
        players.put(uuid, vaultPlayer);

        VaultPlayer finalVaultPlayer = vaultPlayer;
        CompletableFuture<Void> future = new CompletableFuture<>();
        AxVaults.getThreadedQueue().submit(() -> {
            finalVaultPlayer.load();
            future.complete(null);
        });
        future.thenRun(() -> Scheduler.get().run(scheduledTask -> consumer.accept(finalVaultPlayer)));
    }

    public static void removePlayer(@NotNull Player player, boolean save) {
        final VaultPlayer vaultPlayer = players.remove(player.getUniqueId());
        if (!save || vaultPlayer == null) return;
        vaultPlayer.save();
    }

    public static void getVaultOfPlayer(@NotNull Player player, int num, Consumer<Vault> consumer) {
        getPlayer(player.getUniqueId(), vaultPlayer -> {
            consumer.accept(vaultPlayer.getVault(num));
        });
    }

    public static ConcurrentHashMap<UUID, VaultPlayer> getPlayers() {
        return players;
    }

    public static void removeVault(@NotNull Vault vault) {
        final VaultPlayer player = players.get(vault.getUUID());
        if (player == null) return;
        player.removeVault(vault);
        if (player.getVaultMap().isEmpty()) {
            players.remove(player.getUUID());
        }
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

    public static ConcurrentLinkedQueue<Vault> getVaults() {
        return vaults;
    }
}
