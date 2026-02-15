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
import java.util.logging.Logger;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class IconUtils {

    //static Logger logger = Logger.getAnonymousLogger();

    @NotNull
    public static List<IconItem> getAllowedIconItems() {

        List<IconItem> icons = new ArrayList<>();

        List<Map<String, Object>> listIcons = CONFIG.getMapList("allowed-vault-icons");

        // Default behaviour
        if (listIcons == null || listIcons.isEmpty()) {
            for (Material material : Material.values())
                icons.add(new IconItem(material, null));

            return icons;
        }

        // Else read config lines
        for (Map<String, Object> map : listIcons) {
            //logger.info("Found new entry " + map);

            Object rawMaterial = map.get("material");
            if (!(rawMaterial instanceof String materialName)) continue;

            Material material = parseMaterial(materialName);
            if (material == null) continue; // Skip this config line

            Integer customModelData = null;
            Object rawCmd = map.get("custom-model-data");
            if (rawCmd instanceof Number number)
                customModelData = number.intValue();

            icons.add(new IconItem(material, customModelData));
        }

        return icons;
    }

    @Nullable
    public static Integer getCustomModelData(@NotNull Material material) {
        for (IconItem icon : getAllowedIconItems()) {
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
