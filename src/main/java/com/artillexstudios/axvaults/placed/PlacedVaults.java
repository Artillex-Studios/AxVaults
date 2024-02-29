package com.artillexstudios.axvaults.placed;

import com.artillexstudios.axvaults.AxVaults;
import org.bukkit.Location;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

public class PlacedVaults {
    private static final HashMap<Location, Integer> vaults = new HashMap<>();

    public static void addVault(@NotNull Location location, @Nullable Integer number) {
        vaults.put(location, number);
        // todo: add hologram?
    }

    public static void removeVault(@NotNull Location location) {
        AxVaults.getThreadedQueue().submit(() -> AxVaults.getDatabase().removeVault(location));
        vaults.remove(location);
        // todo: remove hologram?
    }

    public static HashMap<Location, Integer> getVaults() {
        return vaults;
    }
}
