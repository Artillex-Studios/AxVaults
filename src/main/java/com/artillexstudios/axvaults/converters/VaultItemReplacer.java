package com.artillexstudios.axvaults.converters;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.utils.ThreadUtils;
import com.artillexstudios.axvaults.utils.VaultUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;


import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class VaultItemReplacer {

    public void run() {
        AtomicInteger updatedVaults = new AtomicInteger();
        int playerUUIDs = 0;

        final List<ReplacementRule> replacementRules = loadRules();
        if (replacementRules.isEmpty()) return;

        for (UUID uuid : AxVaults.getDatabase().getVaultOwners()) {
            playerUUIDs++;

            final OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            VaultManager.getPlayer(offlinePlayer).thenAccept(vaultPlayer -> {

                final List<Vault> vaults = new ArrayList<>(vaultPlayer.getVaultMap().values());
                for (Vault vault : vaults) {
                    final ItemStack[] initialItems = vault.getStorage().getContents();

                    if (isVeryEmpty(initialItems)) continue;

                    final ItemStack[] replacedItems = apply(initialItems, replacementRules, offlinePlayer.getName());

                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] ItemReplacer: updating vault for " + offlinePlayer.getName()));

                    ThreadUtils.runSync(() -> {
                        vault.setContents(replacedItems);

                        VaultUtils.save(vault);
                        updatedVaults.getAndIncrement();
                    });
                }
            });
        }

        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] Finished replacing items! Updated Vaults: " + updatedVaults + " for " + playerUUIDs + " players!"));
    }

    public static ItemStack[] apply(ItemStack[] contents, List<ReplacementRule> replacementRules, String playerName) {

        if (contents == null || contents.length == 0)
            return contents;

        if (replacementRules.isEmpty())
            return contents;

        int replacedItems = 0;

        for (int i = 0; i < contents.length; i++) {
            final ItemStack itemStack = contents[i];
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;

            final String materialId = getMaterialId(itemStack.getType());

            for (ReplacementRule replacementRule : replacementRules) {

                if (!replacementRule.matches(materialId)) continue;

                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults] Replacing " + contents[i] + " by " + replacementRule.material + " x" + replacementRule.amount));

                contents[i] = new ItemStack(replacementRule.material(), replacementRule.amount());
                replacedItems++;

                break;
            }
        }

        if (replacedItems > 0)
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults] Replaced " + replacedItems + " items for player " + playerName));

        return contents;
    }

    public static boolean isVeryEmpty(ItemStack[] itemStacks) {
        for (ItemStack itemStack : itemStacks) {
            if (itemStack != null && itemStack.getType() != Material.AIR)
                return false;
        }

        return true;
    }

    public static List<ReplacementRule> loadRules() {
        final List<Map<String, Object>> mapList = CONFIG.getMapList("item-replacements");

        if (mapList == null || mapList.isEmpty()) return List.of();

        final List<ReplacementRule> rules = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            final List<String> contains = getContainsMatchers(map.get("contains"));
            if (contains.isEmpty()) continue;

            final Material replacementMaterial = parseMaterial(map.get("replacement-material").toString());
            if (replacementMaterial == null || replacementMaterial == Material.AIR) continue;

            final int amount = getAmount(map.get("replacement-amount"));

            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults] Replacement rule: " + contains + " => " + replacementMaterial + ": " + amount));

            rules.add(new ReplacementRule(contains, replacementMaterial, amount));
        }

        return rules;
    }

    private static List<String> getContainsMatchers(Object rawContains) {
        final List<String> contains = new ArrayList<>();

        if (rawContains instanceof String matcher && !matcher.isBlank()) {
            contains.add(matcher.toLowerCase(Locale.ENGLISH));
            return contains;
        }

        if (rawContains instanceof List<?> matcherList) {
            for (Object matcher : matcherList) {
                if (!(matcher instanceof String text) || text.isBlank()) continue;
                contains.add(text.toLowerCase(Locale.ENGLISH));
            }
        }

        return contains;
    }

    @Nullable
    public static Material parseMaterial(@Nullable String material) {
        if (material == null || material.isBlank()) return null;
        Material parsed = Material.matchMaterial(material, true);
        if (parsed != null) return parsed;
        return Material.matchMaterial(material.toUpperCase(Locale.ENGLISH));
    }

    private static int getAmount(Object rawAmount) {
        if (rawAmount instanceof Number number) {
            final int amount = number.intValue();
            if (amount < 1) return 1;
            return Math.min(amount, 64);
        }

        return 1;
    }

    private static String getMaterialId(Material material) {
        return material.getKey().toString().toLowerCase(Locale.ENGLISH);
    }

    public record ReplacementRule(List<String> contains, Material material, int amount) {

        private boolean matches(String materialId) {
            for (String matcher : contains) {
                if (!materialId.contains(matcher)) return false;
            }

            return true;
        }
    }
}
