package com.artillexstudios.axvaults.converters;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.utils.ThreadUtils;
import com.artillexstudios.axvaults.utils.VaultUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.lang.reflect.Method;
import java.util.List;
import java.util.UUID;

import static com.artillexstudios.axvaults.converters.VaultItemReplacer.*;

public class PlayerVaultsXConverter {

    public void run() {
        final File path = new File(Bukkit.getWorldContainer(), "plugins/PlayerVaults/newvaults");
        if (path.exists()) {
            int vaults = 0;
            int players = 0;

            final List<VaultItemReplacer.ReplacementRule> replacementRules = loadRules();
            if (replacementRules.isEmpty()) return;

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

                VaultPlayer vaultPlayer = VaultManager.getPlayer(Bukkit.getOfflinePlayer(uuid)).join();

                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults] PlayerVaultsXConverter: processing player " + Bukkit.getOfflinePlayer(uuid).getName()));

                for (String route : data.getBackingDocument().getRoutesAsStrings(false)) {
                    final int num = Integer.parseInt(route.replace("vault", ""));

                    final ItemStack[] initialItems = getItems(data.getString(route));

                    if (initialItems == null || isVeryEmpty(initialItems)) continue;

                    final ItemStack[] contents = apply(initialItems, replacementRules, Bukkit.getOfflinePlayer(uuid).getName());

                    ThreadUtils.runSync(() -> {
                        Vault vault = vaultPlayer.getVaultMap().get(num);
                        if (vault == null) {
                            vault = new Vault(vaultPlayer, num, null, null, contents);
                        } else {
                            vault.setContents(contents);
                        }

                        VaultUtils.save(vault);
                    });
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
            final Class<?> cl = Class.forName("com.drtshock.playervaults.vaultmanagement.CardboardBoxSerialization");
            final Method method = cl.getDeclaredMethod("fromStorage", String.class, String.class);
            method.setAccessible(true);
            return (ItemStack[]) method.invoke(null, base64, "AxVaults");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
