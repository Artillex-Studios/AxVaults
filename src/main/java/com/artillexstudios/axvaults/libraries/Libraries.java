package com.artillexstudios.axvaults.libraries;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axapi.libraries.Library;
import com.artillexstudios.axapi.libraries.Relocation;
import com.artillexstudios.axapi.utils.file.FileUtils;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum Libraries {

    HIKARICP("com{}zaxxer:HikariCP:7.1.0", relocation("com{}zaxxer{}hikari", "com.artillexstudios.axvaults.libs.hikari")),

    SQLITE_JDBC("org{}xerial:sqlite-jdbc:3.49.1.0"),

    H2_JDBC("com{}h2database:h2:2.1.214"),

    MYSQL_CONNECTOR("com{}mysql:mysql-connector-j:9.2.0", relocation("com{}mysql", "com.artillexstudios.axvaults.libs.mysql"));

    private final List<Relocation> relocations = new ArrayList<>();
    private final Library library;

    public static void load(AxPlugin plugin, DependencyManagerWrapper manager) {
        // remove legacy libs
        File libs = new File(plugin.getDataFolder(), "libs");
        if (libs.exists()) {
            FileUtils.deleteNested(libs.toPath());
        }

        for (Libraries lib : Libraries.values()) {
            for (Relocation relocation : lib.relocations()) {
                manager.relocate(relocation);
            }
            FetchResult result = lib.fetchLibrary();
            if (result.exception()) break;
            manager.dependency(result.library());
        }
    }

    public record FetchResult(Library library, boolean exception) {}

    @NotNull
    public FetchResult fetchLibrary() {
        return new FetchResult(this.library, false);
    }

    private static Relocation relocation(String from, String to) {
        return new Relocation(from.replace("{}", "."), to);
    }

    public List<Relocation> relocations() {
        return this.relocations;
    }

    Libraries(String lib, Relocation... relocation) {
        this(lib);
        this.relocations.addAll(Arrays.stream(relocation).toList());
    }

    Libraries(String lib) {
        String[] split = lib.replace("{}", ".").split(":");
        this.library = new Library(split[0], split[1], split[2], null, List.of());
    }
}
