package com.artillexstudios.axvaults.utils;

import org.bukkit.inventory.ItemStack;
import org.bukkit.util.io.BukkitObjectInputStream;
import org.bukkit.util.io.BukkitObjectOutputStream;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;

@Deprecated(forRemoval = true)
public class SerializationUtils {

    public static byte[] invToBits(ItemStack[] stack) {
        try {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            BukkitObjectOutputStream boos = new BukkitObjectOutputStream(baos);
            boos.writeObject(stack);

            boos.flush();
            boos.close();

            return baos.toByteArray();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }

    @Nullable
    public static ItemStack[] invFromBits(@NotNull InputStream stream) {
        try {
            try (BukkitObjectInputStream dataInput = new BukkitObjectInputStream(stream)) {
                return (ItemStack[]) dataInput.readObject();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
