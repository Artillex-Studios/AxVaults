package com.artillexstudios.axvaults.converters;

import com.artillexstudios.axapi.config.Config;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvaults.utils.ThreadUtils;
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
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static com.artillexstudios.axvaults.converters.VaultItemReplacer.isVeryEmpty;

public class EnderVaultsConverter {

    private Class<?> nbtTagListClass;
    private Class<?> nbtItemStackClass;
    private Class<?> nbtTagCompoundClass;
    private Class<?> nbtReadLimiterClass;

    private Method readNbt;
    private Method nbtTagListSizeMethod;
    private Method nbtTagListGetMethod;
    private Method nbtTagCompoundIsEmptyMethod;
    private Method nbtTagCompoundGetStringMethod;
    private Method nbtTagCompoundSetStringMethod;
    private Method nbtItemStackCreateMethod;
    private Method asBukkitCopy;
    private Constructor<?> nbtReadLimiterConstructor;

    public boolean init() {
        Class<?> nbtToolsClass;
        String cbVersion = Bukkit.getServer().getClass().getPackage().getName();
        cbVersion = cbVersion.substring(cbVersion.lastIndexOf('.') + 1);

        // Inspired from EnderVaults github
        try {
            nbtToolsClass = findClass(
                    "net.minecraft.nbt.NbtIo",
                    "net.minecraft.server." + cbVersion + ".NBTCompressedStreamTools"
            );

            nbtReadLimiterClass = findClass(
                    "net.minecraft.nbt.NbtAccounter",
                    "net.minecraft.server." + cbVersion + ".NBTReadLimiter"
            );
            nbtTagListClass = findClass(
                    "net.minecraft.nbt.ListTag",
                    "net.minecraft.server." + cbVersion + ".NBTTagList"
            );
            nbtItemStackClass = findClass(
                    "net.minecraft.world.item.ItemStack",
                    "net.minecraft.server." + cbVersion + ".ItemStack"
            );
            nbtTagCompoundClass = findClass(
                    "net.minecraft.nbt.CompoundTag",
                    "net.minecraft.server." + cbVersion + ".NBTTagCompound"
            );

        } catch (ClassNotFoundException e) {
            LogUtils.error("&#FF0000[AxVaults] EnderVaults converter: Unable to find classes needed for NBT. Are you sure we support this Minecraft version?", e);
            return false;
        }

        try {
            readNbt = findMethod(nbtToolsClass,
                    new String[]{"a", "readUnnamedTag", "read"},
                    DataInput.class,
                    Integer.TYPE,
                    nbtReadLimiterClass
            );
            if (readNbt == null) {
                readNbt = findMethod(nbtToolsClass,
                        new String[]{"a", "readUnnamedTag", "read"},
                        DataInput.class,
                        nbtReadLimiterClass
                );
            }
            if (readNbt == null) throw new NoSuchMethodException("Unable to find matching read method");
            readNbt.setAccessible(true);

            nbtTagListSizeMethod = findMethod(nbtTagListClass, new String[]{"size"});
            nbtTagListGetMethod = findMethod(nbtTagListClass, new String[]{"get", "k", "getCompound"}, int.class);
            nbtTagCompoundIsEmptyMethod = findMethod(nbtTagCompoundClass, new String[]{"isEmpty", "g"});
            nbtTagCompoundGetStringMethod = findMethod(nbtTagCompoundClass, new String[]{"getString", "l"}, String.class);
            nbtTagCompoundSetStringMethod = findMethod(nbtTagCompoundClass, new String[]{"putString", "a", "setString"}, String.class, String.class);
            if (nbtTagListSizeMethod == null || nbtTagListGetMethod == null || nbtTagCompoundIsEmptyMethod == null) {
                throw new NoSuchMethodException("Unable to find required NBT list/compound methods");
            }
            nbtItemStackCreateMethod = findStaticMethodWithParam(nbtItemStackClass, nbtItemStackClass, nbtTagCompoundClass);
            nbtReadLimiterConstructor = nbtReadLimiterClass.getConstructor(long.class);

            Class<?> craftItemStackClass = findClass(
                    "org.bukkit.craftbukkit." + cbVersion + ".inventory.CraftItemStack",
                    "org.bukkit.craftbukkit.inventory.CraftItemStack"
            );
            asBukkitCopy = craftItemStackClass.getMethod("asBukkitCopy", nbtItemStackClass);
        } catch (NoSuchMethodException e) {
            LogUtils.error("&#FF0000[AxVaults] EnderVaults converter: Unable to find writeNbt or readNbt method. Are you sure we support this Minecraft version?", e);
            return false;
        } catch (ClassNotFoundException e) {
            LogUtils.error("&#FF0000[AxVaults] EnderVaults converter: Unable to find CraftItemStack class. Are you sure we support this Minecraft version?", e);
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
                if (!vaultFile.isFile() || !vaultFile.getName().endsWith(".yml")) continue;

                final Config data = new Config(vaultFile);

                int number = 1;
                try {
                    number = Integer.parseInt(data.getString("metadata.order"));
                } catch (Exception ignored) {}

                if (number < 1) number = 1;
                final int vaultNumber = number;

                final ItemStack[] contents = deserialize(data.getString("contents"), Bukkit.getOfflinePlayer(uuid).getName());

                if (contents == null) {
                    Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] contents missing for " + uuid + "/" + vaultFile.getName()));
                    continue;
                }

                if (isVeryEmpty(contents)) continue; // Empty vault

                Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] EnderVaultsConverter: updating vault for " + Bukkit.getOfflinePlayer(uuid).getName()));

                ThreadUtils.runSync(() -> {
                    Vault vault = vaultPlayer.getVaultMap().get(vaultNumber);
                    if (vault == null) {
                        vault = new Vault(vaultPlayer, vaultNumber, null, null, contents);
                    } else {
                        vault.setContents(contents);
                    }

                    VaultUtils.save(vault);
                });

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

        Object nbtReadLimiter = nbtReadLimiterConstructor.newInstance(Long.MAX_VALUE);
        Object readInvoke;
        if (readNbt.getParameterCount() == 3) {
            readInvoke = readNbt.invoke(null, dataInputStream, 0, nbtReadLimiter);
        } else {
            readInvoke = readNbt.invoke(null, dataInputStream, nbtReadLimiter);
        }

        Object nbtTagList = nbtTagListClass.cast(readInvoke);
        int nbtTagListSize = (int) nbtTagListSizeMethod.invoke(nbtTagList);
        Object items = Array.newInstance(nbtItemStackClass, nbtTagListSize);

        for (int i = 0; i < nbtTagListSize; ++i) {
            Object nbtTagCompound = nbtTagListGetMethod.invoke(nbtTagList, i);
            boolean isEmpty = (boolean) nbtTagCompoundIsEmptyMethod.invoke(nbtTagCompound);
            if (!isEmpty) {
                remapSpartanWeaponryMaterialId(nbtTagCompound);
                Array.set(items, i, nbtItemStackCreateMethod.invoke(null, nbtTagCompound));
            }
        }

        return (Object[]) items;
    }

    private ItemStack[] deserialize(String base64, String playerName) {
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
        return (ItemStack) asBukkitCopy.invoke(null, itemStack);
    }

    // In old versions of SpartanWeaponry mod, item ids where "spartanweaponry:diamond_saber" but it changes to "spartanweaponry:saber_diamond"
    private void remapSpartanWeaponryMaterialId(Object nbtTagCompound) throws InvocationTargetException, IllegalAccessException {
        if (nbtTagCompoundGetStringMethod == null || nbtTagCompoundSetStringMethod == null) return;

        String materialId = (String) nbtTagCompoundGetStringMethod.invoke(nbtTagCompound, "id");
        if (materialId == null || !materialId.startsWith("spartanweaponry:")) return;

        final int separatorIndex = materialId.lastIndexOf('_');
        final int namespaceSeparatorIndex = materialId.indexOf(':');
        if (separatorIndex <= namespaceSeparatorIndex + 1 || separatorIndex >= materialId.length() - 1) return;

        String name = materialId.substring(namespaceSeparatorIndex + 1, separatorIndex);
        String material = materialId.substring(separatorIndex + 1);
        String remappedId = "spartanweaponry:" + material + "_" + name;
        nbtTagCompoundSetStringMethod.invoke(nbtTagCompound, "id", remappedId);
    }

    private Class<?> findClass(String... classNames) throws ClassNotFoundException {
        for (String className : classNames) {
            try {
                return Class.forName(className);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException("None of the classes were found: " + String.join(", ", classNames));
    }

    private Method findMethod(Class<?> owner, String[] names, Class<?>... parameterTypes) {
        for (String name : names) {
            try {
                Method method = owner.getMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }

            try {
                Method method = owner.getDeclaredMethod(name, parameterTypes);
                method.setAccessible(true);
                return method;
            } catch (NoSuchMethodException ignored) {
            }
        }
        return null;
    }

    private Method findStaticMethodWithParam(Class<?> owner, Class<?> returnType, Class<?> parameterType) throws NoSuchMethodException {
        for (Method method : owner.getDeclaredMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (method.getParameterCount() != 1) continue;
            if (!parameterType.isAssignableFrom(method.getParameterTypes()[0])) continue;
            if (!returnType.isAssignableFrom(method.getReturnType())) continue;
            method.setAccessible(true);
            return method;
        }

        for (Method method : owner.getMethods()) {
            if (!Modifier.isStatic(method.getModifiers())) continue;
            if (method.getParameterCount() != 1) continue;
            if (!parameterType.isAssignableFrom(method.getParameterTypes()[0])) continue;
            if (!returnType.isAssignableFrom(method.getReturnType())) continue;
            method.setAccessible(true);
            return method;
        }
        throw new NoSuchMethodException("Unable to find static method on " + owner.getName() + " with param " + parameterType.getName());
    }
}
