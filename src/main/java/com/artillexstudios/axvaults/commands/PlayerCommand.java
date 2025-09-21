package com.artillexstudios.axvaults.commands;

import com.artillexstudios.axvaults.guis.VaultSelector;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.orphan.OrphanCommand;

import java.util.HashMap;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public class PlayerCommand implements OrphanCommand {

    @DefaultFor({"~"})
    @AutoComplete("@vaults")
    public void vault(@NotNull Player sender, @Optional Integer number) {
        open(sender, number, false);
    }

    public void open(@NotNull Player sender, @Optional @Range(min = 1) Integer number, boolean force) {
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
