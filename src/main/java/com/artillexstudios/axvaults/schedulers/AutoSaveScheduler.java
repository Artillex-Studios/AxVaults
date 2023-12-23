package com.artillexstudios.axvaults.schedulers;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;

import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class AutoSaveScheduler {

    public void start() {
        Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            for (VaultPlayer vaultPlayer : VaultManager.getPlayers().values()) {
                for (Vault vault : vaultPlayer.getVaultMap().values()) {
                    AxVaults.getDatabase().saveVault(vault);
                }
            }
        }, CONFIG.getLong("auto-save-minutes"), CONFIG.getLong("auto-save-minutes"), TimeUnit.MINUTES);
    }
}
