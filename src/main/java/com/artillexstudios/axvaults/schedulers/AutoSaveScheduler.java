package com.artillexstudios.axvaults.schedulers;

import com.artillexstudios.axapi.executor.ExceptionReportingScheduledThreadPool;
import com.artillexstudios.axapi.utils.mutable.MutableInteger;
import com.artillexstudios.axvaults.utils.VaultUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class AutoSaveScheduler {
    private static ExceptionReportingScheduledThreadPool pool = null;
    private static long lastSave = -1;
    private static long savedVaults = -1;

    public static void start() {
        int time = CONFIG.getInt("auto-save-minutes");
        if (pool != null) pool.shutdown();

        pool = new ExceptionReportingScheduledThreadPool(1);
        pool.scheduleAtFixedRate(() -> {
            long saveStart = System.currentTimeMillis();
            MutableInteger saved = new MutableInteger();

            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Vault vault : VaultManager.getVaults()) {
                if (vault.hasChanged().get()) { // only save if the vault has been touched since the last save
                    futures.add(VaultUtils.save(vault));
                    saved.increment();
                }
                if (vault.isOpened()) continue;
                vault.hasChanged().set(false); // if the player is not currently editing it, set changed to false
                if (Bukkit.getPlayer(vault.getUUID()) != null) continue;
                if (System.currentTimeMillis() - vault.getLastOpen() <= (time - 1) * 1_000L) continue;
                VaultManager.removeVault(vault);
            }

            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                lastSave = System.currentTimeMillis() - saveStart;
                savedVaults = saved.get();
            });
        }, time, time, TimeUnit.MINUTES);
    }

    public static void stop() {
        if (pool == null) return;
        pool.shutdown();
    }

    public static long getLastSaveLength() {
        return lastSave;
    }

    public static long getSavedVaults() {
        return savedVaults;
    }
}
