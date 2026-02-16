package com.artillexstudios.axvaults.converters;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvaults.utils.VaultUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import org.bukkit.Bukkit;
import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.File;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Base64;
import java.util.UUID;

public class EnderVaultsConverter {

    private Class<?> nbtTagListClass;
    private Class<?> nbtItemStackClass;
    private Class<?> nbtTagCompoundClass;
    private Class<?> nbtReadLimiterClass;
    final String version = "1.20.1";

    private Method readNbt;

    public boolean init() {
        Class<?> nbtToolsClass;
        try {

            nbtToolsClass = Class.forName("net.minecraft.server." + version + ".NBTCompressedStreamTools");

            nbtReadLimiterClass = Class.forName("net.minecraft.server." + version + ".NBTReadLimiter");
            nbtTagListClass = Class.forName("net.minecraft.server." + version + ".NBTTagList");
            nbtItemStackClass = Class.forName("net.minecraft.server." + version + ".ItemStack");
            nbtTagCompoundClass = Class.forName("net.minecraft.server." + version + ".NBTTagCompound");

        } catch (ClassNotFoundException e) {
            LogUtils.error("&#FF0000[AxVaults] EnderVaults converter: Unable to find classes needed for NBT. Are you sure we support this Minecraft version?", e);
            return false;
        }

        try {
            readNbt = nbtToolsClass.getDeclaredMethod("a", DataInput.class, Integer.TYPE, nbtReadLimiterClass);
            readNbt.setAccessible(true);
        } catch (NoSuchMethodException e) {
            LogUtils.error("&#FF0000[AxVaults] EnderVaults converter: Unable to find writeNbt or readNbt method. Are you sure we support this Minecraft version?", e);
            return false;
        }

        return true;
    }

    public void run() {
        final File path = new File(Bukkit.getWorldContainer(), "plugins/EnderVaults/data");

        boolean hasInitCorrectly = init();
        if (!hasInitCorrectly) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] Failed converting! EnderVaults init failed!"));
            return;
        }

        if (!path.exists()) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] Failed converting! EnderVaults folder not found!"));
            return;
        }

        int vaults = 0;
        int players = 0;
        final File[] playerFolders = path.listFiles();
        if (playerFolders == null) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] Failed converting! Could not access EnderVaults data folder!"));
            return;
        }

        for (File playerFolder : playerFolders) {
            if (!playerFolder.isDirectory()) continue;

            final UUID uuid;
            try {
                uuid = UUID.fromString(playerFolder.getName());
            } catch (Exception ex) {
                continue;
            }

            boolean hasConvertedVault = false;
            final VaultPlayer vaultPlayer = VaultManager.getPlayer(Bukkit.getOfflinePlayer(uuid)).join();
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults] EnderVaultsConverter: processing player " + Bukkit.getOfflinePlayer(uuid).getName()));

            final File[] vaultFiles = playerFolder.listFiles();
            if (vaultFiles == null) continue;

            for (File vaultFile : vaultFiles) {
                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults]EnderVaultsConverter: processing file " + vaultFile.getName()));
                if (!vaultFile.isFile() || !vaultFile.getName().endsWith(".yml")) continue;

                final Config data = new Config(vaultFile);

                int number = 1;
                try {
                    number = Integer.parseInt(data.getString("metadata.order"));
                } catch (Exception ignored) {}

                if (number < 1) number = 1;

                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults]EnderVaultsConverter: processing contents"));

                final ItemStack[] contents = deserialize(data.getString("contents"));
                if (contents == null) {
                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] contents missing for " + uuid + "/" + vaultFile.getName()));
                    continue;
                }

                final Vault vault = new Vault(vaultPlayer, number, null, null, contents);
                VaultUtils.save(vault);

                hasConvertedVault = true;
                vaults++;
            }

            if (hasConvertedVault)
                players++;
        }
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#33FF33[AxVaults] Finished converting " + vaults + " vaults of " + players + " players!"));
    }

    private Object[] decode(String encoded) throws IllegalAccessException, InstantiationException, NoSuchMethodException, InvocationTargetException {
        ByteArrayInputStream byteArrayInputStream = new ByteArrayInputStream(Base64.getDecoder().decode(encoded));
        DataInputStream dataInputStream = new DataInputStream(byteArrayInputStream);

        Object nbtReadLimiter = nbtReadLimiterClass.getConstructor(long.class).newInstance(Long.MAX_VALUE);
        Object readInvoke = readNbt.invoke(null, dataInputStream, 0, nbtReadLimiter);

        Object nbtTagList = nbtTagListClass.cast(readInvoke);
        Method nbtTagListSizeMethod = nbtTagListClass.getMethod("size");
        Method nbtTagListGetMethod = nbtTagListClass.getMethod("get", int.class);
        int nbtTagListSize = (int) nbtTagListSizeMethod.invoke(nbtTagList);

        Method nbtTagCompoundIsEmptyMethod = nbtTagCompoundClass.getMethod("isEmpty");
        Method nbtItemStackCreateMethod = nbtItemStackClass.getMethod("createStack", nbtTagCompoundClass);
        Object items = Array.newInstance(nbtItemStackClass, nbtTagListSize);

        for (int i = 0; i < nbtTagListSize; ++i) {
            Object nbtTagCompound = nbtTagListGetMethod.invoke(nbtTagList, i);
            boolean isEmpty = (boolean) nbtTagCompoundIsEmptyMethod.invoke(nbtTagCompound);
            if (!isEmpty) {
                Array.set(items, i, nbtItemStackCreateMethod.invoke(null, nbtTagCompound));
            }
        }

        return (Object[]) items;
    }

    private ItemStack[] deserialize(String base64) {
        try {

            Object[] nmsItemStacks = this.decode(base64);
            ItemStack[] inventoryContents = new ItemStack[nmsItemStacks.length];
            for (int i = 0; i < nmsItemStacks.length; i++) {
                try {
                    inventoryContents[i] = toBukkitItem(nmsItemStacks[i]);
                } catch (ClassNotFoundException | NoSuchMethodException | InvocationTargetException | IllegalAccessException e) {
                    LogUtils.error("&#FF0000[AxVaults] EnderVaults converter: Unable to decode item.", e);
                }
            }

            return inventoryContents;

        } catch (IllegalAccessException | InstantiationException | NoSuchMethodException | InvocationTargetException e) {
            LogUtils.error("&#FF0000[AxVaults] EnderVaults converter: Unable to decode bukkit vault.", e);
        }

        return null;
    }

    private ItemStack toBukkitItem(Object itemStack) throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        Method asBukkitCopy = Class.forName("org.bukkit.craftbukkit." + version + ".inventory.CraftItemStack").getMethod("asBukkitCopy", nbtItemStackClass);
        return (ItemStack) asBukkitCopy.invoke(null, itemStack);
    }
}
