package com.artillexstudios.axvaults.commands;

import com.artillexstudios.axvaults.guis.VaultSelector;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;

import java.util.HashMap;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

@Command({"axvault", "vault", "pv", "axpv", "axvaults", "vaults"})
public class PlayerCommand {

    @DefaultFor({"~"})
    @AutoComplete("@vaults")
    public void axvault(@NotNull Player sender, @Optional Integer number) {
        open(sender, number, false);
    }

    public void open(@NotNull Player sender, @Optional Integer number, boolean force) {
        if (number == null) {
            if (!force && !sender.hasPermission("axvaults.selector")) {
                MESSAGEUTILS.sendLang(sender, "no-permission");
                return;
            }
            new VaultSelector().open(sender);
            return;
        }

        if (number < 1) return;

        final HashMap<String, String> replacements = new HashMap<>();
        replacements.put("%num%", "" + number);

        if (!force && !sender.hasPermission("axvaults.openremote")) {
            MESSAGEUTILS.sendLang(sender, "no-permission");
            return;
        }

        final Vault vault = VaultManager.getVaultOfPlayer(sender, number);
        if (vault == null) {
            MESSAGEUTILS.sendLang(sender, "vault.not-unlocked", replacements);
            return;
        }

        vault.open(sender);
        MESSAGEUTILS.sendLang(sender, "vault.opened", replacements);
    }
}
