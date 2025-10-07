package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.utils.VaultUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

import java.util.Collections;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;
import static com.artillexstudios.axvaults.AxVaults.MESSAGES;
import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum Reload {
    INSTANCE;

    public void execute(CommandSender sender) {
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

        VaultUtils.reload();
        VaultManager.getVaults().forEach(Vault::reload);
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00╠ &#00FF00Reloaded &fvaults&#00FF00!"));

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55FF00╚ &#00FF00Successful reload!"));
        MESSAGEUTILS.sendLang(sender, "reload.success");
    }
}
