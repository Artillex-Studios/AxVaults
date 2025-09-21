package com.artillexstudios.axvaults.database.impl;

import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.database.Database;
import com.artillexstudios.axvaults.placed.PlacedVaults;
import com.artillexstudios.axvaults.utils.VaultUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.h2.jdbc.JdbcConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;

public class H2 implements Database {
    private JdbcConnection conn;

    @Override
    public String getType() {
        return "H2";
    }

    @Override
    public void setup() {
        try {
            conn = new JdbcConnection("jdbc:h2:./" + AxVaults.getInstance().getDataFolder() + "/data;mode=MySQL", new Properties(), null, null, false);
            conn.setAutoCommit(true);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        String CREATE_TABLE = """
                    CREATE TABLE IF NOT EXISTS `axvaults_data`(
                      `id` INT(128) NOT NULL,
                      `uuid` VARCHAR(36) NOT NULL,
                      `storage` LONGBLOB,
                      `icon` VARCHAR(128)
                    );
                """;

        try (PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String CREATE_TABLE2 = """
                    CREATE TABLE IF NOT EXISTS `axvaults_blocks` (
                      `location` VARCHAR(255) NOT NULL,
                      `number` INT,
                      PRIMARY KEY (`location`)
                    );
                """;

        try (PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE2)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public CompletableFuture<Void> saveVault(@NotNull Vault vault) {
        Consumer<byte[]> consumer = bytes -> {
            final String sql = "SELECT * FROM axvaults_data WHERE uuid = ? AND id = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, vault.getUUID().toString());
                stmt.setInt(2, vault.getId());

                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        final String sql2 = "UPDATE axvaults_data SET storage = ?, icon = ? WHERE uuid = ? AND id = ?;";
                        try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                            stmt2.setBytes(1, bytes);
                            stmt2.setString(2, vault.getRealIcon() == null ? null : vault.getRealIcon().name());
                            stmt2.setString(3, vault.getUUID().toString());
                            stmt2.setInt(4, vault.getId());
                            stmt2.executeUpdate();
                        }
                    } else {
                        final String sql2 = "INSERT INTO axvaults_data(id, uuid, storage, icon) VALUES (?, ?, ?, ?);";
                        try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                            stmt2.setInt(1, vault.getId());
                            stmt2.setString(2, vault.getUUID().toString());
                            stmt2.setBytes(3, bytes);
                            stmt2.setString(4, vault.getRealIcon() == null ? null : vault.getRealIcon().name());
                            stmt2.executeUpdate();
                        }
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
        };

        // if the server is shutting down, this can't be called async
        CompletableFuture<Void> local;
        if (Bukkit.isPrimaryThread()) {
            local = VaultUtils.serialize(vault).thenAccept(consumer);
        } else {
            local = VaultUtils.serialize(vault).thenAcceptAsync(consumer);
        }

        CompletableFuture<Void> cf = new CompletableFuture<>();
        local.exceptionally(throwable -> {
            LogUtils.error("An exception occurred while saving vaults!", throwable);
            return null;
        }).thenRun(() -> cf.complete(null));
        return cf;
    }

    @Override
    public void loadVaults(@NotNull VaultPlayer vaultPlayer) {
        final String sql = "SELECT * FROM axvaults_data WHERE uuid = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vaultPlayer.getUUID().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    ItemStack[] items = Serializers.ITEM_ARRAY.deserialize(rs.getBytes(3));
                    int id = rs.getInt(1);
                    Material icon = rs.getString(4) == null ? null : Material.valueOf(rs.getString(4));
                    new Vault(vaultPlayer, id, icon, items);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isVault(@NotNull Location location) {
        final String sql = "SELECT * FROM axvaults_blocks WHERE location = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, Serializers.LOCATION.serialize(location));

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) return true;
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return false;
    }

    @Override
    public void setVault(@NotNull Location location, @Nullable Integer num) {
        final String sql = "INSERT INTO `axvaults_blocks`(`location`, `number`) VALUES (?, ?)";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, Serializers.LOCATION.serialize(location));
            if (num == null) stmt.setString(2, null);
            else stmt.setInt(2, num);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        PlacedVaults.addVault(location, num);
    }

    @Override
    public void removeVault(@NotNull Location location) {
        final String sql = "DELETE FROM axvaults_blocks WHERE location = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, Serializers.LOCATION.serialize(location));
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deleteVault(@NotNull UUID uuid, int num) {
        final String sql = "DELETE FROM axvaults_data WHERE uuid = ? AND id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, num);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void load() {
        final String sql = "SELECT * FROM axvaults_blocks;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    final String vault = rs.getString(2);
                    final Integer vaultInt = vault == null ? null : Integer.parseInt(vault);
                    PlacedVaults.addVault(Serializers.LOCATION.deserialize(rs.getString(1)), vaultInt);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void disable() {
        try {
            conn.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
