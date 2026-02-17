package com.artillexstudios.axvaults.database.impl;

import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.AxVaults;
import com.artillexstudios.axvaults.converters.ItemReplacer;
import com.artillexstudios.axvaults.database.Database;
import com.artillexstudios.axvaults.placed.PlacedVaults;
import com.artillexstudios.axvaults.utils.ThreadUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
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
import java.util.*;

import static com.artillexstudios.axvaults.converters.ItemReplacer.loadRules;

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
                      `icon` VARCHAR(128),
                      `iconCustomModelData` INT(128)
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
    public void saveVault(Vault vault, Object result) {
        // delete empty vaults

        if (result instanceof Boolean bool && bool) {
            String sql = "DELETE FROM axvaults_data WHERE uuid = ? AND id = ?;";
            try (PreparedStatement stmt = conn.prepareStatement(sql)){
                stmt.setString(1, vault.getUUID().toString());
                stmt.setInt(2, vault.getId());
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
            }
            return;
        }

        if (result == null) {
            Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] Failed to save vault #%s of %s!".formatted(vault.getId(), vault.getUUID().toString())));
            return;
        }

        byte[] bytes = (byte[]) result;
        String sql = "SELECT * FROM axvaults_data WHERE uuid = ? AND id = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vault.getUUID().toString());
            stmt.setInt(2, vault.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    sql = "UPDATE axvaults_data SET storage = ?, icon = ?, iconCustomModelData = ? WHERE uuid = ? AND id = ?;";
                    try (PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                        stmt2.setBytes(1, bytes);
                        stmt2.setString(2, vault.getRealIcon() == null ? null : vault.getRealIcon().name());
                        stmt2.setString(3, vault.getIconCustomModelData() == null ? null : vault.getIconCustomModelData().toString());
                        stmt2.setString(4, vault.getUUID().toString());
                        stmt2.setInt(5, vault.getId());
                        stmt2.executeUpdate();
                    }
                } else {
                    sql = "INSERT INTO axvaults_data(id, uuid, storage, icon, iconCustomModelData) VALUES (?, ?, ?, ?, ?);";
                    try (PreparedStatement stmt2 = conn.prepareStatement(sql)) {
                        stmt2.setInt(1, vault.getId());
                        stmt2.setString(2, vault.getUUID().toString());
                        stmt2.setBytes(3, bytes);
                        stmt2.setString(4, vault.getRealIcon() == null ? null : vault.getRealIcon().name());
                        stmt2.setString(5, vault.getIconCustomModelData() == null ? null : vault.getIconCustomModelData().toString());
                        stmt2.executeUpdate();
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void loadVaults(@NotNull VaultPlayer vaultPlayer) {
        final String sql = "SELECT * FROM axvaults_data WHERE uuid = ?;";
        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vaultPlayer.getUUID().toString());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    int id = rs.getInt(1);
                    ItemStack[] items;
                    try {
                        items = Serializers.ITEM_ARRAY.deserialize(rs.getBytes(3));
                    } catch (Exception ex) {
                        ex.printStackTrace();
                        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] Failed to load vault #%s of %s!".formatted(id, vaultPlayer.getUUID().toString())));
                        continue;
                    }
                    Material icon = rs.getString(4) == null ? null : Material.valueOf(rs.getString(4));
                    Integer iconCustomModel = rs.getString(5) == null ? null : Integer.valueOf(rs.getString(5));
                    ThreadUtils.runSync(() -> new Vault(vaultPlayer, id, icon, iconCustomModel, items));
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
    public int replaceItemsInVaults() {
        int updatedVaults = 0;

        final String sql = "SELECT * FROM axvaults_data;";
        final String updateSql = "UPDATE axvaults_data SET storage = ? WHERE uuid = ? AND id = ?;";

        final List<ItemReplacer.ReplacementRule> replacementRules = loadRules();

        try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {

                final UUID uuid = UUID.fromString(rs.getString(2));
                final int id = rs.getInt(1);

                final ItemStack[] items;
                try {
                    items = Serializers.ITEM_ARRAY.deserialize(rs.getBytes(3));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }

                final ItemStack[] applyResult = ItemReplacer.apply(items, replacementRules, Bukkit.getOfflinePlayer(uuid).getName());

                if (Arrays.equals(items, applyResult)) continue;

                final byte[] serialized;
                try {
                    serialized = Serializers.ITEM_ARRAY.serialize(applyResult);
                } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }

                try (PreparedStatement updateStmt = conn.prepareStatement(updateSql)) {
                    updateStmt.setBytes(1, serialized);
                    updateStmt.setString(2, uuid.toString());
                    updateStmt.setInt(3, id);
                    updateStmt.executeUpdate();
                }

                updatedVaults++;

                final VaultPlayer vaultPlayer = VaultManager.getPlayers().get(uuid);
                if (vaultPlayer == null) continue;

                final Vault vault = vaultPlayer.getVault(id);
                if (vault == null) continue;

                ThreadUtils.runSync(() -> vault.setContents(applyResult));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return updatedVaults;
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
