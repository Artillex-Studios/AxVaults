package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.database.VaultItemReplaceResult;
import org.bukkit.command.CommandSender;

import java.util.Map;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum ReplaceItems {
    INSTANCE;

    public void execute(CommandSender sender) {
        MESSAGEUTILS.sendLang(sender, "replace-items.started");

        AxVaults.getThreadedQueue().submit(() -> {
            final long startedAt = System.currentTimeMillis();
            final VaultItemReplaceResult result = AxVaults.getDatabase().replaceItemsInVaults();

            MESSAGEUTILS.sendLang(sender, "replace-items.finished", Map.of(
                    "%processed%", String.valueOf(result.processedVaults()),
                    "%updated%", String.valueOf(result.updatedVaults()),
                    "%replaced%", String.valueOf(result.replacedItems()),
                    "%time%", String.valueOf(System.currentTimeMillis() - startedAt)
            ));
        });
    }
}
