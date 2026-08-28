package com.artillexstudios.axvaults.utils;

import com.artillexstudios.axapi.items.WrappedItemStack;
import com.artillexstudios.axapi.libs.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class BlacklistUtils {

    public static boolean isBlacklisted(@Nullable ItemStack it) {
        if (it == null || it.getType() == Material.AIR) return false;
        if (checkLegacy(it)) return true;
        try {
            List<Map<String, Object>> list = CONFIG.getMapList("blacklist-items");
            if (list == null || list.isEmpty()) return false;
            WrappedItemStack wrap = WrappedItemStack.wrap(it);
            for (Map<String, Object> map : list) {
                ItemMatcher matcher = new ItemMatcher(wrap, map);
                boolean result = matcher.isMatching();
                if (result) return true;
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return false;
    }

    private static boolean checkLegacy(ItemStack it) {
        final Section section = CONFIG.getSection("blacklisted-items");
        if (section == null) return false;
        for (String s : section.getRoutesAsStrings(false)) {
            if (CONFIG.getString("blacklisted-items." + s + ".material") != null) {
                if (!it.getType().toString().equalsIgnoreCase(CONFIG.getString("blacklisted-items." + s + ".material"))) continue;
                return true;
            }

            if (CONFIG.getString("blacklisted-items." + s + ".name-contains") != null) {
                if (it.getItemMeta() == null) continue;
                if (!it.getItemMeta().getDisplayName().contains(CONFIG.getString("blacklisted-items." + s + ".name-contains"))) continue;
                return true;
            }
        }
        return false;
    }
}
