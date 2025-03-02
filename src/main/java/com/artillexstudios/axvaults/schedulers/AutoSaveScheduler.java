package com.artillexstudios.axvaults.schedulers;

import com.artillexstudios.axapi.utils.mutable.MutableInteger;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class AutoSaveScheduler {
    private static ScheduledExecutorService service = null;
    private static long lastSave = -1;
    private static long savedVaults = -1;

    public static void start() {
        int time = CONFIG.getInt("auto-save-minutes");
        if (service != null) service.shutdown();

        service = Executors.newSingleThreadScheduledExecutor();
        service.scheduleAtFixedRate(() -> {
            try {
                long saveStart = System.currentTimeMillis();
                MutableInteger saved = new MutableInteger();

                List<CompletableFuture<Void>> futures = new ArrayList<>();
                synchronized (VaultManager.getVaults()) {
                    Iterator<Vault> iterator = VaultManager.getVaults().iterator();
                    while (iterator.hasNext()) {
                        final Vault vault = iterator.next();
                        if (vault.getChangedValue().get()) { // only save if the vault has been touched since the last save
                            futures.add(AxVaults.getDatabase().saveVault(vault));
                            saved.set(saved.get() + 1);
                        }
                        if (vault.isOpened()) continue;
                        vault.getChangedValue().set(false); // if the player is not currently editing it, set changed to false
                        if (Bukkit.getPlayer(vault.getUUID()) != null) continue;
                        if (System.currentTimeMillis() - vault.getLastOpen() <= (time - 1) * 1_000L) continue;
                        VaultManager.removeVault(vault);
                        iterator.remove();
                    }
                }

                CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                    lastSave = System.currentTimeMillis() - saveStart;
                    savedVaults = saved.get();
                });
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }, time, time, TimeUnit.MINUTES);
    }

    public static void stop() {
        if (service == null) return;
        service.shutdown();
    }

    public static long getLastSaveLength() {
        return lastSave;
    }

    public static long getSavedVaults() {
        return savedVaults;
    }
}
