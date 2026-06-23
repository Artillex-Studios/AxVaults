package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.utils.ThreadUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum Delete {
    INSTANCE;

    public void execute(CommandSender sender, OfflinePlayer player, int number) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("%player%", player.getName());
        replacements.put("%num%", "" + number);

        VaultManager.getPlayer(player).thenAccept(vaultPlayer -> {
            final Runnable delete = () -> {
                final Vault vault = vaultPlayer.getVault(number);
                if (vault == null) {
                    ThreadUtils.runAtSender(sender, () -> MESSAGEUTILS.sendLang(sender, "view.not-found", replacements));
                    return;
                }

                VaultManager.removeVault(vault);
                AxVaults.getThreadedQueue().submit(() -> AxVaults.getDatabase().deleteVault(player.getUniqueId(), number));
                ThreadUtils.runAtSender(sender, () -> MESSAGEUTILS.sendLang(sender, "delete", replacements));
            };

            final Player onlinePlayer = player.getPlayer();
            if (onlinePlayer != null) {
                ThreadUtils.runAtEntity(onlinePlayer, delete);
                return;
            }

            ThreadUtils.runAtSender(sender, delete);
        });
    }
}
