package com.artillexstudios.axvaults.commands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.guis.VaultSelector;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.Command;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Subcommand;
import revxrsal.commands.bukkit.annotation.CommandPermission;

import java.util.Collections;
import java.util.HashMap;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;
import static com.artillexstudios.axvaults.AxVaults.MESSAGES;
import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

@Command({"axvaultsadmin", "axvaultadmin", "vaultadmin", "vaultsadmin"})
@CommandPermission("axvaults.admin")
public class AdminCommand {

    @DefaultFor({"~", "~ help"})
    public void help(@NotNull CommandSender sender) {
        for (String m : MESSAGES.getStringList("help")) {
            sender.sendMessage(StringUtils.formatToString(m));
        }
    }

    @Subcommand("reload")
    public void reload(CommandSender sender) {
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00[AxVaults] &#AAFFAAReloading configuration..."));
        if (!CONFIG.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Collections.singletonMap("%file%", "config.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00╠ &#00FF00Reloaded &fconfig.yml&#00FF00!"));

        if (!MESSAGES.reload()) {
            MESSAGEUTILS.sendFormatted(sender, "reload.failed", Collections.singletonMap("%file%", "messages.yml"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00╠ &#00FF00Reloaded &fmessages.yml&#00FF00!"));
        VaultManager.reload();
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00╠ &#00FF00Reloaded &fvaults&#00FF00!"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00╚ &#00FF00Successful reload!"));
        MESSAGEUTILS.sendLang(sender, "reload.success");
    }

    @Subcommand("forceopen")
    public void forceopen(CommandSender sender, @NotNull Player player) {
        new VaultSelector().open(player);
        MESSAGEUTILS.sendLang(sender, "force-open", Collections.singletonMap("%player%", player.getName()));
    }

    @Subcommand("view") // this command right now causes a small memory leak
    public void view(Player sender, @NotNull OfflinePlayer player, int number) {
        final HashMap<String, String> replacements = new HashMap<>();
        replacements.put("%player%", player.getName());
        replacements.put("%num%", "" + number);

        final VaultPlayer vaultPlayer = VaultManager.getPlayer(player.getUniqueId());
        final Vault vault = vaultPlayer.getVault(number);
        if (vault == null) {
            MESSAGEUTILS.sendLang(sender, "view-not-found", replacements);
            return;
        }
        vault.open(sender);
        MESSAGEUTILS.sendLang(sender, "view", replacements);
    }
}
