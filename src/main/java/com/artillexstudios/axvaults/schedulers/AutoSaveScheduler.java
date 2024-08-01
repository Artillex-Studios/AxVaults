package com.artillexstudios.axvaults.schedulers;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class AutoSaveScheduler {
    private static ScheduledFuture<?> future = null;

    public static void start() {
        int time = CONFIG.getInt("auto-save-minutes");
        if (future != null) future.cancel(true);

        future = Executors.newSingleThreadScheduledExecutor().scheduleAtFixedRate(() -> {
            try {
                for (VaultPlayer vaultPlayer : VaultManager.getPlayers().values()) {
                    for (Vault vault : vaultPlayer.getVaultMap().values()) {
                        AxVaults.getDatabase().saveVault(vault);
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, time, time, TimeUnit.MINUTES);
    }

    public static void stop() {
        if (future == null) return;
        future.cancel(true);
    }
}
