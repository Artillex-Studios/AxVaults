package com.artillexstudios.axvaults.libraries;

import org.jetbrains.annotations.NotNull;
import revxrsal.zapper.DependencyManager;
import revxrsal.zapper.relocation.Relocation;
import revxrsal.zapper.repository.Repository;

public enum Libraries {

    HIKARICP("com{}zaxxer:HikariCP:5.1.0", relocation("com{}zaxxer{}hikari", "com.artillexstudios.axvaults.libs.hikari")),

    SQLITE_JDBC("org{}xerial:sqlite-jdbc:3.42.0.0"),

    H2_JDBC("com{}h2database:h2:2.1.214"),

    MYSQL_CONNECTOR("com{}mysql:mysql-connector-j:8.0.33");

    private final String dependency;
    private Relocation relocation;

    Libraries(String dependency) {
        this.dependency = dependency.replace("{}", ".");
    }

    Libraries(String dependency, @NotNull Relocation relocation) {
        this(dependency);
        this.relocation = relocation;
    }

    public void load(Libraries lib, DependencyManager dependencyManager) {
        dependencyManager.dependency(lib.dependency);
        if (lib.relocation != null) dependencyManager.relocate(lib.relocation);
    }

    private static Relocation relocation(String from, String to) {
        return new Relocation(from.replace("{}", "."), to);
    }

    public static void load(DependencyManager dependencyManager) {
        dependencyManager.repository(Repository.mavenCentral());
        dependencyManager.repository(Repository.jitpack());
        dependencyManager.repository(Repository.paper());

        for (Libraries lib : Libraries.values()) {
            lib.load(lib, dependencyManager);
        }

        dependencyManager.load();
    }
}
