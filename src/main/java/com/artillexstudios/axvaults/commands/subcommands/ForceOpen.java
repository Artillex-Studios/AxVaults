package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.guis.VaultSelector;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.HashMap;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum ForceOpen {
    INSTANCE;

    public void execute(CommandSender sender, Player player, @Nullable Integer number) {
        if (number != null) {
            final HashMap<String, String> replacements = new HashMap<>();
            replacements.put("%num%", "" + number);

            VaultManager.getPlayer(player).thenAccept(vaultPlayer -> {
                Vault vault = vaultPlayer.getVault(number);
                if (vault == null) {
                    MESSAGEUTILS.sendLang(player, "vault.not-unlocked", replacements);
                    return;
                }

                vault.open(player);
                MESSAGEUTILS.sendLang(player, "vault.opened", replacements);
            });
            replacements.put("%player%", player.getName());
            MESSAGEUTILS.sendLang(sender, "force-open-vault", replacements);
            return;
        }
        new VaultSelector().open(player);
        MESSAGEUTILS.sendLang(sender, "force-open", Collections.singletonMap("%player%", player.getName()));
    }
}
