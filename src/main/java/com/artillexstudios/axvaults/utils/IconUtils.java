package com.artillexstudios.axvaults.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class IconUtils {

    @NotNull
    public static List<IconItem> getAllowedIconItems() {
        List<IconItem> icons = getConfiguredIconItems();
        if (!icons.isEmpty()) return icons;

        for (Material material : Material.values()) {
            icons.add(new IconItem(material, null));
        }

        return icons;
    }

    @Nullable
    public static Integer getCustomModelData(@NotNull Material material) {
        for (IconItem icon : getConfiguredIconItems()) {
            if (!icon.material().equals(material)) continue;
            return icon.customModelData();
        }
        return null;
    }

    public static void applyCustomModelData(@NotNull ItemStack item, @Nullable Integer customModelData) {
        if (customModelData == null) return;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;
        meta.setCustomModelData(customModelData);
        item.setItemMeta(meta);
    }

    @NotNull
    private static List<IconItem> getConfiguredIconItems() {
        List<IconItem> icons = new ArrayList<>();

        for (String material : CONFIG.getStringList("allowed-vault-icons")) {
            Material parsed = parseMaterial(material);
            if (parsed == null) continue;
            icons.add(new IconItem(parsed, null));
        }

        List<Map<String, Object>> mapped = CONFIG.getMapList("allowed-vault-icons");
        if (mapped != null) {
            for (Map<String, Object> map : mapped) {
                Object rawMaterial = map.get("material");
                if (!(rawMaterial instanceof String material)) continue;

                Material parsed = parseMaterial(material);
                if (parsed == null) continue;

                Integer customModelData = null;
                Object rawCmd = map.get("custom-model-data");
                if (rawCmd instanceof Number number) {
                    customModelData = number.intValue();
                }

                icons.add(new IconItem(parsed, customModelData));
            }
        }

        return icons;
    }

    @Nullable
    private static Material parseMaterial(@Nullable String material) {
        if (material == null || material.isBlank()) return null;
        Material parsed = Material.matchMaterial(material, true);
        if (parsed != null) return parsed;
        return Material.matchMaterial(material.toUpperCase(Locale.ENGLISH));
    }

    public record IconItem(@NotNull Material material, @Nullable Integer customModelData) {
    }
}
