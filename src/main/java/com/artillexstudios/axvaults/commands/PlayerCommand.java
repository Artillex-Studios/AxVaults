package com.artillexstudios.axvaults.commands;

import com.artillexstudios.axvaults.commands.subcommands.Open;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import revxrsal.commands.annotation.AutoComplete;
import revxrsal.commands.annotation.DefaultFor;
import revxrsal.commands.annotation.Optional;
import revxrsal.commands.orphan.OrphanCommand;

public class PlayerCommand implements OrphanCommand {

    @DefaultFor({"~"})
    @AutoComplete("@vaults")
    public void vault(@NotNull Player sender, @Optional Integer number) {
        Open.INSTANCE.execute(sender, number, false);
    }
}
