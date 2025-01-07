package com.artillexstudios.axvaults.utils;

import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;
import org.jetbrains.annotations.NotNull;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class PermissionUtils {

    public static boolean hasPermission(@NotNull Player player, int vault) {
        if (CONFIG.getInt("permission-mode", 0) == 0) return player.hasPermission("axvaults.vault." + vault);
        if (player.isOp()) return true;

        int max = 0;
        for (PermissionAttachmentInfo effectivePermission : player.getEffectivePermissions()) {
            if (!effectivePermission.getValue()) continue;
            if (effectivePermission.getPermission().equals("*")) return true;

            if (!effectivePermission.getPermission().startsWith("axvaults.vault.")) continue;

            int value = Integer.parseInt(effectivePermission.getPermission().substring(effectivePermission.getPermission().lastIndexOf('.') + 1));

            if (value > max) max = value;
        }

        return vault <= max;
    }
}
