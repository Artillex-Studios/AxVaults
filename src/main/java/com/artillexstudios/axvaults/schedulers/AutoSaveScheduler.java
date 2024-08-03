package com.artillexstudios.axvaults.schedulers;

import com.artillexstudios.axapi.scheduler.Scheduler;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import org.bukkit.Bukkit;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class AutoSaveScheduler {
    private static ScheduledExecutorService service = null;

    public static void start() {
        int time = CONFIG.getInt("auto-save-minutes");
        if (service != null) service.shutdown();

        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            try {
                for (VaultPlayer vaultPlayer : VaultManager.getPlayers().values()) {
                    for (Vault vault : vaultPlayer.getVaultMap().values()) {
                        AxVaults.getDatabase().saveVault(vault);
                        if (vault.isOpened()) continue;
                        if (Bukkit.getPlayer(vault.getUUID()) != null) continue;
                        if (System.currentTimeMillis() - vault.getLastOpen() <= (time - 1) * 1_000L) continue;
                        Scheduler.get().run(scheduledTask -> VaultManager.removeVault(vault));
                    }
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, time, time, TimeUnit.MINUTES); // TODO: MINUTES
    }

    public static void stop() {
        if (service == null) return;
        service.shutdown();
    }
}
