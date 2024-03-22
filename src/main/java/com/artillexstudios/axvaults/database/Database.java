package com.artillexstudios.axvaults.database;

import com.artillexstudios.axvaults.vaults.Vault;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public interface Database {

    String getType();

    void setup();

    void saveVault(@NotNull Vault vault);

    void loadVaults(@NotNull UUID uuid);

    boolean isVault(@NotNull Location location);

    void setVault(@NotNull Location location, @Nullable Integer num);

    void removeVault(@NotNull Location location);

    void deleteVault(@NotNull UUID uuid, int num);

    void load();

    void disable();
}
