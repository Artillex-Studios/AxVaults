package com.artillexstudios.axvaults.converters;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.utils.ThreadUtils;
import com.artillexstudios.axvaults.utils.VaultUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class VaultItemReplacer {

    public int replaceItemsInVaults() {
        int updatedVaults = 0;

        final List<ItemReplacer.ReplacementRule> replacementRules = ItemReplacer.loadRules();
        if (replacementRules.isEmpty()) return updatedVaults;

        for (UUID uuid : AxVaults.getDatabase().getVaultOwners()) {
            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            final VaultPlayer vaultPlayer = VaultManager.getPlayer(offlinePlayer).join();

            final List<Vault> vaults = new ArrayList<>(vaultPlayer.getVaultMap().values());
            for (Vault vault : vaults) {
                final AtomicReference<ItemStack[]> initialItems = new AtomicReference<>();
                ThreadUtils.runSync(() -> initialItems.set(vault.getStorage().getContents().clone()));

                final ItemStack[] replacedItems = ItemReplacer.apply(initialItems.get().clone(), replacementRules, offlinePlayer.getName());
                if (Arrays.equals(initialItems.get(), replacedItems)) continue;

                ThreadUtils.runSync(() -> vault.setContents(replacedItems));
                VaultUtils.save(vault).join();
                updatedVaults++;
            }
        }

        return updatedVaults;
    }
}
