package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.converters.VaultItemReplacer;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum ReplaceItems {
    INSTANCE;

    public void execute(CommandSender sender) {
        CompletableFuture.runAsync(() -> {
            new VaultItemReplacer().run();
        });
        MESSAGEUTILS.sendLang(sender, "replace-items.started");
    }
}
