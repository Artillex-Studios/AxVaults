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
        VaultManager.getPlayer(player).thenAccept(vaultPlayer -> {
            if (number != null) {
                HashMap<String, String> replacements = new HashMap<>();
                replacements.put("%num%", "" + number);
                replacements.put("%player%", player.getName());

                Vault vault = vaultPlayer.getVault(number);
                if (vault == null) {
                    MESSAGEUTILS.sendLang(player, "vault.not-unlocked", replacements);
                    return;
                }

                vault.open(player);
                MESSAGEUTILS.sendLang(player, "vault.opened", replacements);
                MESSAGEUTILS.sendLang(sender, "force-open-vault", replacements);
                return;
            }
            new VaultSelector(player, vaultPlayer).open();
            MESSAGEUTILS.sendLang(sender, "force-open", Collections.singletonMap("%player%", player.getName()));
        });

    }
}
