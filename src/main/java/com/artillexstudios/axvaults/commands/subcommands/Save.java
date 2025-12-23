package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.utils.VaultUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.command.CommandSender;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum Save {
    INSTANCE;

    public void execute(CommandSender sender) {
        long time = System.currentTimeMillis();
        AxVaults.getThreadedQueue().submit(() -> {
            List<CompletableFuture<Void>> futures = new ArrayList<>();
            for (Vault vault : VaultManager.getVaults()) {
                VaultUtils.save(vault);
            }
            CompletableFuture.allOf(futures.toArray(CompletableFuture[]::new)).thenRun(() -> {
                MESSAGEUTILS.sendLang(sender, "save.manual", Map.of("%time%", "" + (System.currentTimeMillis() - time)));
            });
        });
    }
}
