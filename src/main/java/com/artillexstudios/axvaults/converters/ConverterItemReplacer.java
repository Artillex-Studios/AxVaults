package com.artillexstudios.axvaults.converters;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class ConverterItemReplacer {

    public static ItemStack[] apply(ItemStack[] contents) {
        if (contents == null || contents.length == 0) return contents;

        final List<ReplacementRule> replacementRules = loadRules();
        if (replacementRules.isEmpty()) return contents;

        for (int i = 0; i < contents.length; i++) {
            final ItemStack itemStack = contents[i];
            if (itemStack == null || itemStack.getType() == Material.AIR) continue;

            final String materialId = getMaterialId(itemStack.getType());
            for (ReplacementRule replacementRule : replacementRules) {
                if (!replacementRule.matches(materialId)) continue;

                contents[i] = new ItemStack(replacementRule.material(), replacementRule.amount());
                break;
            }
        }

        return contents;
    }

    private static List<ReplacementRule> loadRules() {
        final List<Map<String, Object>> mapList = CONFIG.getMapList("converter-item-replacements");
        if (mapList == null || mapList.isEmpty()) return List.of();

        final List<ReplacementRule> rules = new ArrayList<>();
        for (Map<String, Object> map : mapList) {
            final List<String> contains = getContainsMatchers(map.get("contains"));
            if (contains.isEmpty()) continue;

            final Material replacementMaterial = parseMaterial(map.get("replacement-material"));
            if (replacementMaterial == null || replacementMaterial == Material.AIR) continue;

            final int amount = getAmount(map.get("replacement-amount"));
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

    private static Material parseMaterial(Object rawMaterial) {
        if (!(rawMaterial instanceof String materialName) || materialName.isBlank()) return null;

        Material parsed = Material.matchMaterial(materialName, true);
        if (parsed != null) return parsed;
        return Material.matchMaterial(materialName.toUpperCase(Locale.ENGLISH));
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
        final String namespaced = material.getKey().toString().toLowerCase(Locale.ENGLISH);
        final String legacyName = material.name().toLowerCase(Locale.ENGLISH);
        return namespaced + ":" + legacyName;
    }

    private record ReplacementRule(List<String> contains, Material material, int amount) {

        private boolean matches(String materialId) {
            for (String matcher : contains) {
                if (!materialId.contains(matcher)) return false;
            }

            return true;
        }
    }
}
