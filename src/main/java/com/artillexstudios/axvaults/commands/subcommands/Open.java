package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.guis.VaultSelector;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum Open {
    INSTANCE;

    public void execute(Player sender, @Nullable Integer number, boolean force) {
        if (number == null) {
            if (!force && !sender.hasPermission("axvaults.selector")) {
                MESSAGEUTILS.sendLang(sender, "no-permission");
                return;
            }
            VaultManager.getPlayer(sender).thenAccept(vaultPlayer -> {
                new VaultSelector(sender, vaultPlayer).open();
            });
            return;
        }

        if (number < 1) return;

        final HashMap<String, String> replacements = new HashMap<>();
        replacements.put("%num%", "" + number);

        if (!force && !sender.hasPermission("axvaults.openremote")) {
            MESSAGEUTILS.sendLang(sender, "no-permission");
            return;
        }

        VaultManager.getPlayer(sender).thenAccept(vaultPlayer -> {
            Vault vault = vaultPlayer.getVault(number);
            if (vault == null) {
                MESSAGEUTILS.sendLang(sender, "vault.not-unlocked", replacements);
                return;
            }

            vault.open(sender);
            MESSAGEUTILS.sendLang(sender, "vault.opened", replacements);
        });
    }
}
