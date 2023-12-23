package com.artillexstudios.axvaults.database;

import com.artillexstudios.axvaults.vaults.Vault;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public interface Database {

    String getType();

    void setup();

    void saveVault(@NotNull Vault vault);

    void loadVaults(@NotNull UUID uuid);

    void disable();
}
