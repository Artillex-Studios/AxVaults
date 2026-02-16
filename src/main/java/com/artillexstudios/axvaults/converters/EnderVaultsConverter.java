package com.artillexstudios.axvaults.converters;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.utils.VaultUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.util.Base64;
import java.util.UUID;

public class EnderVaultsConverter {

    public void run() {
        final File path = new File(Bukkit.getWorldContainer(), "plugins/EnderVaults/data");
        if (!path.exists()) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] Failed converting! EnderVaults folder not found!"));
            return;
        }

        int vaults = 0;
        int players = 0;
        final File[] playerFolders = path.listFiles();
        if (playerFolders == null) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] Failed converting! Could not access EnderVaults data folder!"));
            return;
        }

        for (File playerFolder : playerFolders) {
            if (!playerFolder.isDirectory()) continue;

            final UUID uuid;
            try {
                uuid = UUID.fromString(playerFolder.getName());
            } catch (Exception ex) {
                continue;
            }

            boolean hasConvertedVault = false;
            final VaultPlayer vaultPlayer = VaultManager.getPlayer(Bukkit.getOfflinePlayer(uuid)).join();
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults] EnderVaultsConverter: processing player " + Bukkit.getOfflinePlayer(uuid).getName()));

            final File[] vaultFiles = playerFolder.listFiles();
            if (vaultFiles == null) continue;

            for (File vaultFile : vaultFiles) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults]EnderVaultsConverter: processing file " + vaultFile.getName()));
                if (!vaultFile.isFile() || !vaultFile.getName().endsWith(".yml")) continue;

                final Config data = new Config(vaultFile);

                int number = 1;
                try {
                    number = Integer.parseInt(data.getString("metadata.order"));
                } catch (Exception ignored) {}

                if (number < 1) number = 1;

                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults]EnderVaultsConverter: processing contents"));

                final ItemStack[] contents = deserialize(data.getString("contents"));
                if (contents == null) {
                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] contents missing for " + uuid + "/" + vaultFile.getName()));
                    continue;
                }

                final Vault vault = new Vault(vaultPlayer, number, null, null, contents);
                VaultUtils.save(vault);

                hasConvertedVault = true;
                vaults++;
            }

            if (hasConvertedVault)
                players++;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults] Finished converting " + vaults + " vaults of " + players + " players!"));
    }

    private ItemStack[] deserialize(String base64) {
        try {
            ByteArrayInputStream inputStream =
                    new ByteArrayInputStream(Base64.getDecoder().decode(base64));

            BukkitObjectInputStream dataInput =
                    new BukkitObjectInputStream(inputStream);

            int size = dataInput.readInt();
            ItemStack[] items = new ItemStack[size];

            for (int i = 0; i < size; i++) {
                items[i] = (ItemStack) dataInput.readObject();
            }

            dataInput.close();
            return items;

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
}
