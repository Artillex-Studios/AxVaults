package com.artillexstudios.axvaults.commands.subcommands;

import com.artillexstudios.axvaults.schedulers.AutoSaveScheduler;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.List;

import static com.artillexstudios.axvaults.AxVaults.MESSAGES;
import static com.artillexstudios.axvaults.AxVaults.MESSAGEUTILS;

public enum Stats {
    INSTANCE;

    public void execute(CommandSender sender) {
        HashMap<String, String> replacements = new HashMap<>();
        replacements.put("%players%", String.valueOf(VaultManager.getPlayers().size()));
        replacements.put("%vaults%", String.valueOf(VaultManager.getVaults().size()));
        // alternative vault count (%vaults% and %vaults2% should be the same - if not, something is broken)
        int vaults2 = VaultManager.getPlayers().values().stream().mapToInt(value -> value.getVaultMap().size()).sum();
        replacements.put("%vaults2%", "" + vaults2);
        long lastSave = AutoSaveScheduler.getLastSaveLength();
        long savedVaults = AutoSaveScheduler.getSavedVaults();
        replacements.put("%auto-save%", lastSave == -1 ? "---" : "" + lastSave);
        replacements.put("%saved-vaults%", savedVaults == -1 ? "---" : "" + savedVaults);
        List<String> statsMessage = MESSAGES.getStringList("stats");

        for (String s : statsMessage) {
            MESSAGEUTILS.sendFormatted(sender, s, replacements);
        }
    }
}
