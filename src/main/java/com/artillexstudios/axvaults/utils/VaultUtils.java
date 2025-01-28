package com.artillexstudios.axvaults.utils;

import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axvaults.vaults.Vault;

import java.util.concurrent.CompletableFuture;

public class VaultUtils {

    public static CompletableFuture<byte[]> serialize(Vault vault) {
        CompletableFuture<byte[]> cf = new CompletableFuture<>();

        Runnable runnable = () -> {
            byte[] bytes = Serializers.ITEM_ARRAY.serialize(vault.getStorage().getContents());
            cf.complete(bytes);
        };

        ThreadUtils.runSync(runnable);

        return cf;
    }
}
