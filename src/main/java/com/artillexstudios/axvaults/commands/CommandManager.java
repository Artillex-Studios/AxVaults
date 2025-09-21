package com.artillexstudios.axvaults.commands;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.utils.CommandMessages;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.orphan.Orphans;

import java.util.ArrayList;
import java.util.Locale;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class CommandManager {
    private static BukkitCommandHandler handler = null;

    public static void load() {
        handler = BukkitCommandHandler.create(AxVaults.getInstance());

        handler.getAutoCompleter().registerSuggestion("vaults", (args, sender, command) -> {
            final Player player = Bukkit.getPlayer(sender.getUniqueId());
            if (!player.hasPermission("axvaults.openremote")) return new ArrayList<>();

            final ArrayList<String> numbers = new ArrayList<>();
            for (Integer i : VaultManager.getPlayers().get(player.getUniqueId()).getVaultMap().keySet()) {
                numbers.add("" + i);
            }
            return numbers;
        });

        handler.getTranslator().add(new CommandMessages());
        handler.setLocale(Locale.of("en", "US"));

        reload();
    }

    public static void reload() {
        handler.unregisterAllCommands();
        handler.register(Orphans.path(CONFIG.getStringList("player-command-aliases").toArray(String[]::new)).handler(new PlayerCommand()));
        handler.register(Orphans.path(CONFIG.getStringList("admin-command-aliases").toArray(String[]::new)).handler(new AdminCommand()));
        handler.registerBrigadier();
    }
}
