package com.artillexstudios.axvaults.database.impl;

import com.artillexstudios.axapi.serializers.Serializers;
import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axvaults.converters.ConverterItemReplacer;
import com.artillexstudios.axvaults.database.Database;
import com.artillexstudios.axvaults.database.VaultItemReplaceResult;
import com.artillexstudios.axvaults.placed.PlacedVaults;
import com.artillexstudios.axvaults.utils.ThreadUtils;
import com.artillexstudios.axvaults.vaults.Vault;
import com.artillexstudios.axvaults.vaults.VaultManager;
import com.artillexstudios.axvaults.vaults.VaultPlayer;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.UUID;

import static com.artillexstudios.axvaults.AxVaults.CONFIG;

public class MySQL implements Database {
    private HikariDataSource dataSource;

    public MySQL() {
        Bukkit.getConsoleSender().sendMessage(StringUtils.formatToString("&#FF0000[AxVaults] MySQL is NOT fully supported! It will continue to work and you can ignore this warning, however there might be issues."));
    }

    @Override
    public String getType() {
        return "MySQL";
    }

    @Override
    public void setup() {
        final HikariConfig hConfig = new HikariConfig();

        hConfig.setPoolName("axvaults-pool");

        hConfig.setMaximumPoolSize(CONFIG.getInt("database.pool.maximum-pool-size"));
        hConfig.setMinimumIdle(CONFIG.getInt("database.pool.minimum-idle"));
        hConfig.setMaxLifetime(CONFIG.getInt("database.pool.maximum-lifetime"));
        hConfig.setKeepaliveTime(CONFIG.getInt("database.pool.keepalive-time"));
        hConfig.setConnectionTimeout(CONFIG.getInt("database.pool.connection-timeout"));

        hConfig.setDriverClassName("com.mysql.cj.jdbc.Driver");
        hConfig.setJdbcUrl("jdbc:mysql://" + CONFIG.getString("database.address") + ":" + CONFIG.getString("database.port") + "/" + CONFIG.getString("database.database"));
        hConfig.addDataSourceProperty("user", CONFIG.getString("database.username"));
        hConfig.addDataSourceProperty("password", CONFIG.getString("database.password"));

        this.dataSource = new HikariDataSource(hConfig);

        String CREATE_TABLE = """
            CREATE TABLE IF NOT EXISTS `axvaults_data`(
              `id` INT(128) NOT NULL,
              `uuid` VARCHAR(36) NOT NULL,
              `storage` LONGBLOB,
              `icon` VARCHAR(128),
              `iconCustomModelData` INT(128)
            );
        """;

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
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

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE2)) {
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        String CREATE_TABLE3 = """
            CREATE TABLE IF NOT EXISTS axvaults_messages (
              id INT NOT NULL AUTO_INCREMENT,
              event TINYINT,
              vault_id INT NOT NULL,
              uuid VARCHAR(36) NOT NULL,
              date BIGINT NOT NULL,
              PRIMARY KEY (id)
            );
        """;

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE3)) {
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
            try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)){
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
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
                    Integer iconCustomModelData = rs.getString(5) == null ? null : Integer.valueOf(rs.getString(5));
                    new Vault(vaultPlayer, id, icon, iconCustomModelData, items);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public boolean isVault(@NotNull Location location) {
        final String sql = "SELECT * FROM axvaults_blocks WHERE location = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
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
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, Serializers.LOCATION.serialize(location));
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deleteVault(@NotNull UUID uuid, int num) {
        final String sql = "DELETE FROM axvaults_data WHERE uuid = ? AND id = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, uuid.toString());
            stmt.setInt(2, num);
            stmt.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }


    @Override
    public VaultItemReplaceResult replaceItemsInVaults() {
        int processedVaults = 0;
        int updatedVaults = 0;
        int replacedItems = 0;

        final String sql = "SELECT * FROM axvaults_data;";
        final String updateSql = "UPDATE axvaults_data SET storage = ? WHERE uuid = ? AND id = ?;";

        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                processedVaults++;

                final UUID uuid = UUID.fromString(rs.getString(2));
                final int id = rs.getInt(1);

                final ItemStack[] items;
                try {
                    items = Serializers.ITEM_ARRAY.deserialize(rs.getBytes(3));
                } catch (Exception ex) {
                    ex.printStackTrace();
                    continue;
                }

                final ConverterItemReplacer.ApplyResult applyResult = ConverterItemReplacer.applyWithStats(items);
                if (applyResult.replacedItems() < 1) continue;

                final byte[] serialized;
                try {
                    serialized = Serializers.ITEM_ARRAY.serialize(applyResult.contents());
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
                replacedItems += applyResult.replacedItems();

                final VaultPlayer vaultPlayer = VaultManager.getPlayers().get(uuid);
                if (vaultPlayer == null) continue;

                final Vault vault = vaultPlayer.getVault(id);
                if (vault == null) continue;

                ThreadUtils.runSync(() -> vault.setContents(applyResult.contents()));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return new VaultItemReplaceResult(processedVaults, updatedVaults, replacedItems);
    }

    @Override
    public void load() {
        final String sql = "SELECT * FROM axvaults_blocks;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {

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

    private void sendMessage(@NotNull ChangeType changeType, int id, UUID uuid) {
        if (CONFIG.getString("multi-server-support", "none").equalsIgnoreCase("none")) return;
        
        final String sql = "INSERT INTO axvaults_messages(event, vault_id, uuid, date) VALUES (?, ?, ?, ?);";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            stmt.setShort(1, (short) changeType.ordinal());
            stmt.setInt(2, id);
            stmt.setString(3, uuid.toString());
            stmt.setLong(4, System.currentTimeMillis());
            stmt.executeUpdate();
            try (ResultSet rs = stmt.getGeneratedKeys()) {
                if (rs.next()) sentFromHere.put(rs.getInt(1), System.currentTimeMillis() + 10_000L);
            }

        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private final ArrayList<Integer> acknowledged = new ArrayList<>();
    private final HashMap<Integer, Long> sentFromHere = new HashMap<>();
    public void checkForChanges() { // id, event, vault_id, uuid, date
        if (CONFIG.getString("multi-server-support", "none").equalsIgnoreCase("none")) return;

        final String sql = "SELECT * FROM axvaults_messages;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    if (sentFromHere.containsKey(rs.getInt(1))) return;
                    if (acknowledged.contains(rs.getInt(1))) continue;
                    acknowledged.add(rs.getInt(1));
                    final int num = rs.getInt(3);
                    final UUID uuid = UUID.fromString(rs.getString(4));

                    switch (ChangeType.entries[rs.getInt(2)]) {
                        case UPDATE -> {
                            final VaultPlayer vp = VaultManager.getPlayers().get(uuid);
                            if (vp == null) {
                                return;
                            }
                            final Vault vault = vp.getVault(num);
                            if (vault == null) {
                                return;
                            }
                            updateVault(vault);
                        }
                    }
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void removeOldChanges() {
        final String sql = "DELETE FROM axvaults_messages WHERE ? - 7500 > date;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setLong(1, System.currentTimeMillis());
            stmt.executeUpdate();
            acknowledged.clear();
            sentFromHere.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    private void updateVault(@NotNull Vault vault) {
        final String sql = "SELECT * FROM axvaults_data WHERE uuid = ? AND id = ?;";
        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
            stmt.setString(1, vault.getUUID().toString());
            stmt.setInt(2, vault.getId());

            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    final ItemStack[] items = Serializers.ITEM_ARRAY.deserialize(rs.getBytes(3));
                    ThreadUtils.runSync(() -> vault.setContents(items));
                    vault.setIcon(rs.getString(4) == null ? null : Material.valueOf(rs.getString(4)), rs.getString(5) == null ? null : Integer.valueOf(rs.getString(5)));
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void disable() {
        try {
            dataSource.close();
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private enum ChangeType {
        DELETE, UPDATE, INSERT;

        public static final ChangeType[] entries = ChangeType.values();
    }
}
