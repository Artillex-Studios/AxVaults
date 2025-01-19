package com.artillexstudios.axvaults.commands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.converters.PlayerVaultsXConverter;
import com.artillexstudios.axvaults.guis.VaultSelector;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.annotation.Range;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;
import revxrsal.commands.orphan.OrphanCommand;

import java.util.Collections;
import java.util.HashMap;
import java.util.stream.Collectors;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;
import static com.artillexstudios.axvaults.AxVaults.MESSAGES;
import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public class AdminCommand implements OrphanCommand {

    @CommandPermission("axvaults.admin")
    @DefaultFor({"~", "~ help"})
    public void help(@NotNull CommandSender sender) {
        for (String m : MESSAGES.getStringList("help")) {
            sender.sendMessage(StringUtils.formatToString(m));
        }
        sender.sendMessage(StringUtils.formatToString("&e/axvaults nextvault &7- Go to the next vault"));
    }

    @CommandPermission("axvaults.admin.reload")
    @Subcommand("reload")
    public void reload(@NotNull CommandSender sender) {
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00[AxVaults] &#AAFFAAReloading configuration..."));
        if (!CONFIG.reload()) {
            MESSAGEUTILS.sendLang(sender, "reload.failed", Collections.singletonMap("%file%", "config.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00╠ &#00FF00Reloaded &fconfig.yml&#00FF00!"));

        if (!MESSAGES.reload()) {
            MESSAGEUTILS.sendLang(sender, "reload.failed", Collections.singletonMap("%file%", "messages.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00╠ &#00FF00Reloaded &fmessages.yml&#00FF00!"));

        VaultManager.reload();
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00╠ &#00FF00Reloaded &fvaults&#00FF00!"));

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00╚ &#00FF00Successful reload!"));
        MESSAGEUTILS.sendLang(sender, "reload.success");
    }

    @CommandPermission("axvaults.admin.forceopen")
    @Subcommand("forceopen")
    public void forceOpen(@NotNull CommandSender sender, @NotNull Player player, @Optional @Range(min = 1) Integer number) {
        if (number != null) {
            final HashMap<String, String> replacements = new HashMap<>();
            replacements.put("%num%", "" + number);

            VaultManager.getVaultOfPlayer(player, number, vault -> {
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

    @CommandPermission("axvaults.admin.view")
    @Subcommand("view")
    public void view(@NotNull Player sender, @NotNull OfflinePlayer player, @Optional @Range(min = 1) Integer number) {
        final HashMap<String, String> replacements = new HashMap<>();
        replacements.put("%player%", player.getName());

        if (number == null) {
            VaultManager.getPlayer(player.getUniqueId(), vaultPlayer -> {
                replacements.put("%vaults%", vaultPlayer.getVaultMap().values().stream().filter(vault -> vault.getSlotsFilled() != 0).map(vault -> "" + vault.getId()).collect(Collectors.joining(", ")));
                MESSAGEUTILS.sendLang(sender, "view.info", replacements);
            });
            return;
        }

        replacements.put("%num%", "" + number);

        VaultManager.getPlayer(player.getUniqueId(), vaultPlayer -> {
            final Vault vault = vaultPlayer.getVault(number);
            if (vault == null) {
                MESSAGEUTILS.sendLang(sender, "view.not-found", replacements);
                return;
            }
            vault.open(sender);
            MESSAGEUTILS.sendLang(sender, "view.open", replacements);
        });
    }

    @CommandPermission("axvaults.admin.delete")
    @Subcommand("delete")
    public void delete(@NotNull Player sender, @NotNull OfflinePlayer player, int number) {
        final HashMap<String, String> replacements = new HashMap<>();
        replacements.put("%player%", player.getName());
        replacements.put("%num%", "" + number);

        VaultManager.getPlayer(player.getUniqueId(), vaultPlayer -> {
            final Vault vault = vaultPlayer.getVault(number);
            if (vault == null) {
                MESSAGEUTILS.sendLang(sender, "view.not-found", replacements);
                return;
            }
            VaultManager.getVaults().remove(vault);
            VaultManager.removeVault(vault);
            AxVaults.getDatabase().deleteVault(player.getUniqueId(), number);
            MESSAGEUTILS.sendLang(sender, "delete", replacements);
        });
    }

    @CommandPermission("axvaults.admin.set")
    @Subcommand("set")
    public void set(@NotNull Player sender, @Optional Integer number) {
        final Block block = sender.getTargetBlockExact(5);

        if (block == null) {
            MESSAGEUTILS.sendLang(sender, "set.no-block");
            return;
        }

        AxVaults.getThreadedQueue().submit(() -> {
            if (AxVaults.getDatabase().isVault(block.getLocation())) {
                MESSAGEUTILS.sendLang(sender, "set.already");
                return;
            }

            AxVaults.getDatabase().setVault(block.getLocation(), number);
            MESSAGEUTILS.sendLang(sender, "set.success");
        });
    }

    @CommandPermission("axvaults.admin.converter")
    @Subcommand("converter PlayerVaultsX")
    public void converter(@NotNull Player sender) {
        new PlayerVaultsXConverter().run();
        MESSAGEUTILS.sendLang(sender, "converter.started");
    }

    @CommandPermission("axvaults.admin.nextvault")
    @Subcommand("nextvault")
    public void nextVault(@NotNull CommandSender sender, @NotNull Player player) {
        int nextVaultNum = getNextVaultNumber(player);
        if (nextVaultNum != -1) {
            new PlayerCommand().open(player, nextVaultNum, true);
            MESSAGEUTILS.sendLang(sender, "next-vault.success", Collections.singletonMap("%player%", player.getName()));
        } else {
            MESSAGEUTILS.sendLang(sender, "next-vault.no-next-vault", Collections.singletonMap("%player%", player.getName()));
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
