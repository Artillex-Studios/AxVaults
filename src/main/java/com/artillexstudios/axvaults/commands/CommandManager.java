package com.artillexstudios.axvaults.commands;

import com.artillexstudios.axapi.nms.NMSHandlers;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import revxrsal.commands.bukkit.BukkitCommandActor;
import revxrsal.commands.bukkit.BukkitCommandHandler;
import revxrsal.commands.bukkit.exception.InvalidPlayerException;
import revxrsal.commands.orphan.Orphans;

import java.util.ArrayList;
import java.util.Locale;
import java.util.stream.Collectors;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class CommandManager {
    private static BukkitCommandHandler handler = null;

    public static void load() {
        handler = BukkitCommandHandler.create(AxVaults.getInstance());

        handler.getAutoCompleter().registerSuggestion("vaults", (args, sender, command) -> {
            final Player player = Bukkit.getPlayer(sender.getUniqueId());
            if (!player.hasPermission("axvaults.openremote")) return new ArrayList<>();

            final ArrayList<String> numbers = new ArrayList<>();
            for (int i = 0; i < VaultManager.getVaultsOfPlayer(player); i++) {
                numbers.add("" + (i + 1));
            }
            return numbers;
        });

        handler.registerValueResolver(0, OfflinePlayer.class, context -> {
            String value = context.pop();
            if (value.equalsIgnoreCase("self") || value.equalsIgnoreCase("me")) return ((BukkitCommandActor) context.actor()).requirePlayer();
            OfflinePlayer player = NMSHandlers.getNmsHandler().getCachedOfflinePlayer(value);
            if (player == null && !(player = Bukkit.getOfflinePlayer(value)).hasPlayedBefore()) throw new InvalidPlayerException(context.parameter(), value);
            return player;
        });

        handler.getAutoCompleter().registerParameterSuggestions(OfflinePlayer.class, (args, sender, command) -> {
            return Bukkit.getOnlinePlayers().stream().map(HumanEntity::getName).collect(Collectors.toSet());
        });

        handler.getTranslator().add(new com.artillexstudios.axvaults.utils.CommandMessages());
        handler.setLocale(new Locale("en", "US"));

        reload();
    }

    public static void reload() {
        handler.unregisterAllCommands();
        handler.register(Orphans.path(CONFIG.getStringList("player-command-aliases").toArray(String[]::new)).handler(new PlayerCommand()));
        handler.register(Orphans.path(CONFIG.getStringList("admin-command-aliases").toArray(String[]::new)).handler(new AdminCommand()));
        handler.registerBrigadier();
    }
}
