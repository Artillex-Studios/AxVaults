package com.artillexstudios.axvaults.converters;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.yaml.snakeyaml.external.biz.base64Coder.Base64Coder;

import java.io.File;
import java.lang.reflect.Method;
import java.util.UUID;

public class PlayerVaultsXConverter {

    public void run() {
        final File path = new File(Bukkit.getWorldContainer(), "plugins/PlayerVaults/newvaults");
        if (path.exists()) {
            int vaults = 0;
            int players = 0;
            for (File file : path.listFiles()) {
                if (!file.getName().endsWith(".yml")) continue;
                final Config data = new Config(file);
                UUID uuid;
                try {
                    uuid = UUID.fromString(file.getName().replace(".yml", ""));
                } catch (Exception ex) {
                    continue;
                }
                players++;

                for (String route : data.getBackingDocument().getRoutesAsStrings(false)) {
                    final int num = Integer.parseInt(route.replace("vault", ""));
                    VaultPlayer vaultPlayer = VaultManager.getPlayer(Bukkit.getOfflinePlayer(uuid)).join();
                    final Vault vault = new Vault(vaultPlayer, num, null, getItems(data.getString(route)));
                    AxVaults.getDatabase().saveVault(vault);
                    vaults++;
                }
            }
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults] Finished converting " + vaults + " vaults of " + players + " players!"));
            return;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] Failed converting! Folder not found!"));
    }

    private ItemStack[] getItems(String base64) {
        try {
            byte[] bytes = Base64Coder.decodeLines(base64);
            final Class<?> cl = Class.forName("com.drtshock.playervaults.vaultmanagement.CardboardBoxSerialization");
            final Method method = cl.getDeclaredMethod("readInventory", byte[].class);
            method.setAccessible(true);
            return (ItemStack[]) method.invoke(null, bytes);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
