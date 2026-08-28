package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;

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
            final Vault vault = vaultPlayer.getVault(number);
            if (vault == null) {
                MESSAGEUTILS.sendLang(sender, "view.not-found", replacements);
                return;
            }
            VaultManager.removeVault(vault);
            AxVaults.getDatabase().deleteVault(player.getUniqueId(), number);
            MESSAGEUTILS.sendLang(sender, "delete", replacements);
        });
    }
}
