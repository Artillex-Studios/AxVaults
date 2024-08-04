package com.artillexstudios.axvaults.schedulers;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;

import java.util.Iterator;
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
                final Iterator<Vault> iterator = VaultManager.getVaults().iterator();
                while (iterator.hasNext()) {
                    final Vault vault = iterator.next();
                    AxVaults.getDatabase().saveVault(vault);
                    if (vault.isOpened()) continue;
                    if (Bukkit.getPlayer(vault.getUUID()) != null) continue;
                    if (System.currentTimeMillis() - vault.getLastOpen() <= (time - 1) * 1_000L) continue;
                    VaultManager.removeVault(vault);
                    iterator.remove();
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, time, time, TimeUnit.MINUTES);
    }

    public static void stop() {
        if (service == null) return;
        service.shutdown();
    }
}
