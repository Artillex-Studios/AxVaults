package com.artillexstudios.axvaults.database;

import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface Database {

    String getType();

    void setup();

    CompletableFuture<Void> saveVault(@NotNull Vault vault);

    void loadVaults(@NotNull VaultPlayer vaultPlayer);

    boolean isVault(@NotNull Location location);

    void setVault(@NotNull Location location, @Nullable Integer num);

    void removeVault(@NotNull Location location);

    void deleteVault(@NotNull UUID uuid, int num);

    void load();

    void disable();
}
