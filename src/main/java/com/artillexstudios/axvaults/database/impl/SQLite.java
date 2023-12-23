package com.artillexstudios.axvaults.database.impl;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.database.Database;
import com.artillexstudios.axvaults.utils.SerializationUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.UUID;

public class SQLite implements Database {
    private Connection conn;

    @Override
    public String getType() {
        return "SQLite";
    }

    @Override
    public void setup() {

        try {
            Class.forName("org.sqlite.JDBC");
            this.conn = DriverManager.getConnection(String.format("jdbc:sqlite:%s/data.db", AxVaults.getInstance().getDataFolder()));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `axvaults_data`" +
                "( `id` INT(128) NOT NULL," +
                "`uuid` VARCHAR(36) NOT NULL," +
                "`storage` LONGBLOB," +
                "`icon` VARCHAR(128)," +
                "PRIMARY KEY (`id`) );";

        try (PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void saveVault(@NotNull Vault vault) {
        if (vault.getSlotsFilled() == 0) return;
        final String sql = "INSERT INTO axvaults_data(id, uuid, storage, icon) VALUES (?, ?, ?, ?) ON CONFLICT DO UPDATE SET storage = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, vault.getId());
            stmt.setString(2, vault.getUUID().toString());
            final byte[] bytes = SerializationUtils.invToBits(vault.getStorage().getContents());
            stmt.setBytes(3, bytes);
            stmt.setString(4, vault.getRealIcon() == null ? null : vault.getRealIcon().name());
            stmt.setBytes(5, bytes);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void loadVaults(@NotNull UUID uuid) {
        final String sql = "SELECT * FROM axvaults_data WHERE uuid = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    final ItemStack[] items = SerializationUtils.invFromBits(rs.getBinaryStream(3));
                    final Vault vault = new Vault(uuid, rs.getInt(1), items, rs.getString(4) == null ? null : Material.valueOf(rs.getString(4)));
                    VaultManager.addVault(vault);
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
