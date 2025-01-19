package com.artillexstudios.axvaults.commands;

import com.artillexstudios.axvaults.guis.VaultSelector;
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
    public void axvault(@NotNull Player sender, @Optional Integer number) {
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

        VaultManager.getVaultOfPlayer(sender, number, vault -> {
            if (vault == null) {
                MESSAGEUTILS.sendLang(sender, "vault.not-unlocked", replacements);
                return;
            }

            vault.open(sender);
            MESSAGEUTILS.sendLang(sender, "vault.opened", replacements);
        });
    }

    public void nextVault(@NotNull Player sender) {
        int nextVaultNum = getNextVaultNumber(sender);
        if (nextVaultNum != -1) {
            open(sender, nextVaultNum, false);
        } else {
            MESSAGEUTILS.sendLang(sender, "vault.no-next-vault");
        }
    }

    private int getNextVaultNumber(@NotNull Player player) {
        int maxVaults = CONFIG.getInt("max-vault-amount");
        for (int i = 1; i <= maxVaults; i++) {
            if (!VaultManager.getVaultOfPlayer(player, i, vault -> {})) {
                return i;
            }
        }
        return -1;
    }
}
