package com.artillexstudios.axvaults.utils;

import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axvaults.vaults.Vault;

import java.util.concurrent.CompletableFuture;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class VaultUtils {
    private static boolean asyncItemSerializer;
    private static boolean deleteEmptyVaults;

    public static void reload() {
        asyncItemSerializer = CONFIG.getBoolean("async-item-serializer", false);
        deleteEmptyVaults = CONFIG.getBoolean("delete-empty-vaults", true);
    }

    public static CompletableFuture<Object> serialize(Vault vault) {
        CompletableFuture<Object> cf = new CompletableFuture<>();

        Runnable runnable = () -> {
            if (deleteEmptyVaults && vault.getStorage().isEmpty()) {
                cf.complete(true); // delete
                return;
            }

            try {
                cf.complete(Serializers.ITEM_ARRAY.serialize(vault.getStorage().getContents())); // success
            } catch (Exception ex) {
                ex.printStackTrace();
                cf.complete(null); // error
            }
        };

        if (asyncItemSerializer) runnable.run();
        else ThreadUtils.runSync(runnable);

        return cf;
    }

    public static boolean isAsyncItemSerializer() {
        return asyncItemSerializer;
    }

    public static boolean isDeleteEmptyVaults() {
        return deleteEmptyVaults;
    }
}
