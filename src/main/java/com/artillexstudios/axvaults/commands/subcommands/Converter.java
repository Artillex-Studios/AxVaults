package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.converters.PlayerVaultsXConverter;
import org.bukkit.command.CommandSender;

import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum Converter {
    INSTANCE;

    public void execute(CommandSender sender) {
        CompletableFuture.runAsync(() -> {
            new PlayerVaultsXConverter().run();
        });
        MESSAGEUTILS.sendLang(sender, "converter.started");
    }
}
