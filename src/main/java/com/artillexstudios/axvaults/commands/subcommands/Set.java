package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.AxVaults;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum Set {
    INSTANCE;

    public void execute(Player sender, @Nullable Integer number) {
        Block block = sender.getTargetBlockExact(5);
        if (block == null) {
            MESSAGEUTILS.sendLang(sender, "set.no-block");
            return;
        }

        AxVaults.getThreadedQueue().submit(() -> {
            if (AxVaults.getDatabase().isVault(block.getLocation())) {
                MESSAGEUTILS.sendLang(sender, "set.already");
                return;
            }

            AxVaults.getDatabase().setVault(block.getLocation(), number);
            MESSAGEUTILS.sendLang(sender, "set.success");
        });
    }
}
