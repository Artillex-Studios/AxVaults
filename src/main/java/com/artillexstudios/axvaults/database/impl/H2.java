package com.artillexstudios.axvaults.database.impl;

import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.database.Database;
import com.artillexstudios.axvaults.utils.SerializationUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.h2.jdbc.JdbcConnection;
import org.jetbrains.annotations.NotNull;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Properties;
import java.util.UUID;

public class H2 implements Database {
    private Connection conn;

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

        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS `axvaults_data`( `id` INT(128) NOT NULL, `uuid` VARCHAR(36) NOT NULL, `storage` LONGBLOB, `icon` VARCHAR(128) );";

        try (PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void saveVault(@NotNull Vault vault) {
        final String sql = "SELECT * FROM axvaults_data WHERE uuid = ? AND id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vault.getUUID().toString());
            stmt.setInt(2, vault.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    final String sql2 = "UPDATE axvaults_data SET storage = ?, icon = ? WHERE uuid = ? AND id = ?;";
                    try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
                        final byte[] bytes = SerializationUtils.invToBits(vault.getStorage().getContents());
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
                        final byte[] bytes = SerializationUtils.invToBits(vault.getStorage().getContents());
                        stmt2.setBytes(3, bytes);
                        stmt2.setString(4, vault.getRealIcon() == null ? null : vault.getRealIcon().name());
                        stmt2.executeUpdate();
                    }
                }
            }
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
