package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;

public enum Debug {
    INSTANCE;

    public void execute(CommandSender sender) {
        sender.sendMessage(StringUtils.formatToString("&#FF0000Printed information in console!"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#00FF00[AxVaults] Debug:\n"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#AAFFAACached users:"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#DDDDDD" + String.join("\n&#DDDDDD", VaultManager.getPlayers().values().stream().map(VaultPlayer::toString).toList())));
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#AAFFAACached vaults:"));
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#DDDDDD" + String.join("\n&#DDDDDD", VaultManager.getVaults().stream().map(Vault::toString).toList())));
    }
}
