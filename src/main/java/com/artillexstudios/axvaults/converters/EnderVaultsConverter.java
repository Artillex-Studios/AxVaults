package com.artillexstudios.axvaults.converters;

import com.artillexstudios.axapi.config.Config;
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

        File path = new File(Bukkit.getWorldContainer(), "plugins/EnderVaults/data");

        if (!path.exists()) {
            Bukkit.getConsoleSender().sendMessage("§c[AxVaults] EnderVaults folder not found!");
            return;
        }

        int players = 0;
        int vaults = 0;

        for (File file : path.listFiles()) {

            if (!file.isFile()) continue;

            UUID uuid;
            try {
                uuid = UUID.fromString(file.getName());
            } catch (Exception ex) {
                continue;
            }

            Config config = new Config(file);

            if (!config.contains("contents")) continue;

            players++;

            int number = 1;
            if (config.contains("metadata.order")) {
                try {
                    number = Integer.parseInt(config.getString("metadata.order"));
                } catch (Exception ignored) {}
            }

            String base64 = config.getString("contents");
            ItemStack[] contents = deserialize(base64);

            if (contents == null) continue;

            VaultPlayer vaultPlayer = VaultManager
                    .getPlayer(Bukkit.getOfflinePlayer(uuid))
                    .join();

            Vault vault = new Vault(vaultPlayer, number, null, contents);
            VaultUtils.save(vault);

            vaults++;
        }

        Bukkit.getConsoleSender().sendMessage(
                "§a[AxVaults] Converted " + vaults + " vault(s) of " + players + " player(s)!"
        );
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
