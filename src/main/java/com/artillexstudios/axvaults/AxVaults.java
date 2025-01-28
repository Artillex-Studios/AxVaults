package com.artillexstudios.axvaults;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.data.ThreadedQueue;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.dvs.versioning.BasicVersioning;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.dumper.DumperSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.general.GeneralSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.loader.LoaderSettings;
import com.artillexstudios.axapi.libs.boostedyaml.boostedyaml.settings.updater.UpdaterSettings;
import com.artillexstudios.axapi.libs.libby.BukkitLibraryManager;
import com.artillexstudios.axapi.metrics.AxMetrics;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axvaults.commands.CommandManager;
import com.artillexstudios.axvaults.database.Database;
import com.artillexstudios.axvaults.database.impl.H2;
import com.artillexstudios.axvaults.database.impl.MySQL;
import com.artillexstudios.axvaults.database.impl.SQLite;
import com.artillexstudios.axvaults.database.messaging.SQLMessaging;
import com.artillexstudios.axvaults.libraries.Libraries;
import com.artillexstudios.axvaults.listeners.BlackListListener;
import com.artillexstudios.axvaults.listeners.BlockBreakListener;
import com.artillexstudios.axvaults.listeners.InventoryCloseListener;
import com.artillexstudios.axvaults.listeners.PlayerInteractListener;
import com.artillexstudios.axvaults.listeners.PlayerListeners;
import com.artillexstudios.axvaults.schedulers.AutoSaveScheduler;
import com.artillexstudios.axvaults.utils.UpdateNotifier;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import org.bstats.bukkit.Metrics;
import org.bukkit.Bukkit;
import revxrsal.commands.bukkit.BukkitCommandHandler;

import java.io.File;

public final class AxVaults extends AxPlugin {
    public static Config CONFIG;
    public static Config MESSAGES;
    public static MessageUtils MESSAGEUTILS;
    private static AxPlugin instance;
    private static ThreadedQueue<Runnable> threadedQueue;
    private static Database database;
    public static BukkitAudiences BUKKITAUDIENCES;
    public static BukkitCommandHandler COMMANDHANDLER;
    private static AxMetrics metrics;

    public static ThreadedQueue<Runnable> getThreadedQueue() {
        return threadedQueue;
    }

    public static AxPlugin getInstance() {
        return instance;
    }

    public static Database getDatabase() {
        return database;
    }

    public void load() {
        BukkitLibraryManager libraryManager = new BukkitLibraryManager(this, "lib");
        libraryManager.addMavenCentral();

        for (Libraries lib : Libraries.values()) {
            libraryManager.loadLibrary(lib.getLibrary());
        }
    }

    public void enable() {
        instance = this;

        int pluginId = 20541;
        new Metrics(this, pluginId);

        CONFIG = new Config(new File(getDataFolder(), "config.yml"), getResource("config.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());
        MESSAGES = new Config(new File(getDataFolder(), "messages.yml"), getResource("messages.yml"), GeneralSettings.builder().setUseDefaults(false).build(), LoaderSettings.builder().setAutoUpdate(true).build(), DumperSettings.DEFAULT, UpdaterSettings.builder().setVersioning(new BasicVersioning("version")).build());

        MESSAGEUTILS = new MessageUtils(MESSAGES.getBackingDocument(), "prefix", CONFIG.getBackingDocument());
        BUKKITAUDIENCES = BukkitAudiences.create(this);

        threadedQueue = new ThreadedQueue<>("AxVaults-Datastore-thread");

        database = switch (CONFIG.getString("database.type").toLowerCase()) {
            case "sqlite" -> new SQLite();
            case "mysql" -> new MySQL();
            default -> new H2();
        };

        database.setup();
        database.load();

        getServer().getPluginManager().registerEvents(new PlayerListeners(), this);
        getServer().getPluginManager().registerEvents(new BlackListListener(), this);
        getServer().getPluginManager().registerEvents(new PlayerInteractListener(), this);
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
        getServer().getPluginManager().registerEvents(new InventoryCloseListener(), this);

        CommandManager.load();

        AutoSaveScheduler.start();
        SQLMessaging.start();

        metrics = new AxMetrics(3);
        metrics.start();

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#55ff00[AxVaults] Loaded plugin!"));

        if (CONFIG.getBoolean("update-notifier.enabled", true)) new UpdateNotifier(this, 5417);
    }

    public void disable() {
        metrics.cancel();
        for (Vault vault : VaultManager.getVaults()) {
            AxVaults.getDatabase().saveVault(vault);
        }
        AutoSaveScheduler.stop();
        SQLMessaging.stop();
        database.disable();
        threadedQueue.stop();
    }

    public void updateFlags(FeatureFlags flags) {
        flags.USE_LEGACY_HEX_FORMATTER.set(true);
//        FeatureFlags.PACKET_ENTITY_TRACKER_ENABLED.set(true);
//        FeatureFlags.HOLOGRAM_UPDATE_TICKS.set(10L);
    }
}
