package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum View {
    INSTANCE;

    public void execute(Player sender, OfflinePlayer player, @Nullable Integer number) {
        Map<String, String> replacements = new HashMap<>();
        replacements.put("%player%", player.getName());

        if (number == null) {
            VaultManager.getPlayer(player).thenAccept(vaultPlayer -> {
                replacements.put("%vaults%", vaultPlayer.getVaultMap().values().stream().filter(vault -> vault.getSlotsFilled() != 0).map(vault -> "" + vault.getId()).collect(Collectors.joining(", ")));
                MESSAGEUTILS.sendLang(sender, "view.info", replacements);
            });
            return;
        }

        replacements.put("%num%", "" + number);

        VaultManager.getPlayer(player).thenAccept(vaultPlayer -> {
            final Vault vault = vaultPlayer.getVault(number);
            if (vault == null) {
                MESSAGEUTILS.sendLang(sender, "view.not-found", replacements);
                return;
            }
            vault.open(sender);
            MESSAGEUTILS.sendLang(sender, "view.open", replacements);
        });
    }
}
