//package com.artillexstudios.axvaults.database.impl;
//
//import com.artillexstudios.axapi.serializers.Serializers;
//import com.artillexstudios.axvaults.database.Database;
//import com.artillexstudios.axvaults.placed.PlacedVaults;
//import com.artillexstudios.axvaults.vaults.Vault;
//import com.artillexstudios.axvaults.vaults.VaultManager;
//import com.artillexstudios.axvaults.vaults.VaultPlayer;
//import com.zaxxer.hikari.HikariConfig;
//import com.zaxxer.hikari.HikariDataSource;
//import org.bukkit.Location;
//import org.bukkit.Material;
//import org.bukkit.inventory.ItemStack;
//import org.jetbrains.annotations.NotNull;
//import org.jetbrains.annotations.Nullable;
//
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.ResultSet;
//import java.sql.SQLException;
//import java.sql.Statement;
//import java.util.ArrayList;
//import java.util.HashMap;
//import java.util.UUID;
//
//import static com.artillexstudios.axvaults.AxVaults.CONFIG;
//
//public class PostgeSQL implements Database {
//    private HikariDataSource dataSource;
//
//    @Override
//    public String getType() {
//        return "PostgeSQL";
//    }
//
//    @Override
//    public void setup() {
//        final HikariConfig hConfig = new HikariConfig();
//
//        hConfig.setPoolName("axvaults-pool");
//
//        hConfig.setMaximumPoolSize(CONFIG.getInt("database.pool.maximum-pool-size"));
//        hConfig.setMinimumIdle(CONFIG.getInt("database.pool.minimum-idle"));
//        hConfig.setMaxLifetime(CONFIG.getInt("database.pool.maximum-lifetime"));
//        hConfig.setKeepaliveTime(CONFIG.getInt("database.pool.keepalive-time"));
//        hConfig.setConnectionTimeout(CONFIG.getInt("database.pool.connection-timeout"));
//
//        hConfig.setDriverClassName("org.postgresql.Driver");
//        hConfig.setJdbcUrl("jdbc:postgresql://" + CONFIG.getString("database.address") + ":" + CONFIG.getString("database.port") + "/" + CONFIG.getString("database.database") + "?ssl=false");
//        hConfig.addDataSourceProperty("user", CONFIG.getString("database.username"));
//        hConfig.addDataSourceProperty("password", CONFIG.getString("database.password"));
//
//        this.dataSource = new HikariDataSource(hConfig);
//
//        final String CREATE_TABLE = "CREATE TABLE IF NOT EXISTS axvaults_data( id INT(128) NOT NULL, uuid VARCHAR(36) NOT NULL, storage LONGBLOB, icon VARCHAR(128) );";
//
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE)) {
//            stmt.executeUpdate();
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//
//        final String CREATE_TABLE2 = "CREATE TABLE IF NOT EXISTS axvaults_blocks ( location VARCHAR(255) NOT NULL, number INT, PRIMARY KEY (location) );";
//
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE2)) {
//            stmt.executeUpdate();
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//
//        final String CREATE_TABLE3 = "CREATE TABLE IF NOT EXISTS axvaults_messages ( id INT NOT NULL AUTO_INCREMENT, event TINYINT, vault_id INT NOT NULL, uuid VARCHAR(36) NOT NULL, date BIGINT NOT NULL, PRIMARY KEY (id) );";
//
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(CREATE_TABLE3)) {
//            stmt.executeUpdate();
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    @Override
//    public void saveVault(@NotNull Vault vault) {
//        final String sql = "SELECT * FROM axvaults_data WHERE uuid = ? AND id = ?;";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, vault.getUUID().toString());
//            stmt.setInt(2, vault.getId());
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) {
//                    final String sql2 = "UPDATE axvaults_data SET storage = ?, icon = ? WHERE uuid = ? AND id = ?;";
//                    try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
//                        final byte[] bytes = Serializers.ITEM_ARRAY.serialize(vault.getStorage().getContents());
//                        stmt2.setBytes(1, bytes);
//                        stmt2.setString(2, vault.getRealIcon() == null ? null : vault.getRealIcon().name());
//                        stmt2.setString(3, vault.getUUID().toString());
//                        stmt2.setInt(4, vault.getId());
//                        stmt2.executeUpdate();
//                        sendMessage(ChangeType.UPDATE, vault.getId(), vault.getUUID());
//                    }
//                } else {
//                    final String sql2 = "INSERT INTO axvaults_data(id, uuid, storage, icon) VALUES (?, ?, ?, ?);";
//                    try (PreparedStatement stmt2 = conn.prepareStatement(sql2)) {
//                        stmt2.setInt(1, vault.getId());
//                        stmt2.setString(2, vault.getUUID().toString());
//                        final byte[] bytes = Serializers.ITEM_ARRAY.serialize(vault.getStorage().getContents());
//                        stmt2.setBytes(3, bytes);
//                        stmt2.setString(4, vault.getRealIcon() == null ? null : vault.getRealIcon().name());
//                        stmt2.executeUpdate();
//                        sendMessage(ChangeType.UPDATE, vault.getId(), vault.getUUID());
//                    }
//                }
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    @Override
//    public void loadVaults(@NotNull UUID uuid) {
//        final String sql = "SELECT * FROM axvaults_data WHERE uuid = ?;";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, uuid.toString());
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    final ItemStack[] items = Serializers.ITEM_ARRAY.deserialize(rs.getBytes(3));
//                    final Vault vault = new Vault(uuid, rs.getInt(1), rs.getString(4) == null ? null : Material.valueOf(rs.getString(4)));
//                    vault.setContents(items, unused -> {
//                    });
//                }
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    @Override
//    public boolean isVault(@NotNull Location location) {
//        final String sql = "SELECT * FROM axvaults_blocks WHERE location = ?;";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, Serializers.LOCATION.serialize(location));
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                if (rs.next()) return true;
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//
//        return false;
//    }
//
//    @Override
//    public void setVault(@NotNull Location location, @Nullable Integer num) {
//        final String sql = "INSERT INTO axvaults_blocks(location, number) VALUES (?, ?)";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, Serializers.LOCATION.serialize(location));
//            if (num == null) stmt.setString(2, null);
//            else stmt.setInt(2, num);
//            stmt.executeUpdate();
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//
//        PlacedVaults.addVault(location, num);
//    }
//
//    @Override
//    public void removeVault(@NotNull Location location) {
//        final String sql = "DELETE FROM axvaults_blocks WHERE location = ?;";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, Serializers.LOCATION.serialize(location));
//            stmt.executeUpdate();
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    @Override
//    public void deleteVault(@NotNull UUID uuid, int num) {
//        final String sql = "DELETE FROM axvaults_data WHERE uuid = ? AND id = ?;";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, uuid.toString());
//            stmt.setInt(2, num);
//            stmt.executeUpdate();
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    @Override
//    public void load() {
//        final String sql = "SELECT * FROM axvaults_blocks;";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    final String vault = rs.getString(2);
//                    final Integer vaultInt = vault == null ? null : Integer.parseInt(vault);
//                    PlacedVaults.addVault(Serializers.LOCATION.deserialize(rs.getString(1)), vaultInt);
//                }
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private void sendMessage(@NotNull ChangeType changeType, int id, UUID uuid) {
//        final String sql = "INSERT INTO axvaults_messages(event, vault_id, uuid, date) VALUES (?, ?, ?, ?);";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
//            stmt.setShort(1, (short) changeType.ordinal());
//            stmt.setInt(2, id);
//            stmt.setString(3, uuid.toString());
//            stmt.setLong(4, System.currentTimeMillis());
//            stmt.executeUpdate();
//            try (ResultSet rs = stmt.getGeneratedKeys()) {
//                if (rs.next()) sentFromHere.put(rs.getInt(1), System.currentTimeMillis() + 10_000L);
//            }
//
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private final ArrayList<Integer> acknowledged = new ArrayList<>();
//    private final HashMap<Integer, Long> sentFromHere = new HashMap<>();
//    public void checkForChanges() { // id, event, vault_id, uuid, date
//        if (CONFIG.getString("multi-server-support", "sql").equalsIgnoreCase("none")) return;
//
//        final String sql = "SELECT * FROM axvaults_messages;";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    if (sentFromHere.containsKey(rs.getInt(1))) return;
//                    if (acknowledged.contains(rs.getInt(1))) continue;
//                    acknowledged.add(rs.getInt(1));
//                    final int num = rs.getInt(3);
//                    final UUID uuid = UUID.fromString(rs.getString(4));
//
//                    switch (ChangeType.entries[rs.getInt(2)]) {
//                        case UPDATE -> {
//                            final VaultPlayer vp = VaultManager.getPlayers().get(uuid);
//                            if (vp == null) {
//                                return;
//                            }
//                            final Vault vault = vp.getVault(num);
//                            if (vault == null) {
//                                return;
//                            }
//                            updateVault(vault);
//                        }
//                    }
//                }
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    public void removeOldChanges() {
//        final String sql = "DELETE FROM axvaults_messages WHERE ? - 7500 > date;";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setLong(1, System.currentTimeMillis());
//            stmt.executeUpdate();
//            acknowledged.clear();
//            sentFromHere.entrySet().removeIf(entry -> entry.getValue() < System.currentTimeMillis());
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private void updateVault(@NotNull Vault vault) {
//        final String sql = "SELECT * FROM axvaults_data WHERE uuid = ? AND id = ?;";
//        try (Connection conn = dataSource.getConnection(); PreparedStatement stmt = conn.prepareStatement(sql)) {
//            stmt.setString(1, vault.getUUID().toString());
//            stmt.setInt(2, vault.getId());
//
//            try (ResultSet rs = stmt.executeQuery()) {
//                while (rs.next()) {
//                    final ItemStack[] items = Serializers.ITEM_ARRAY.deserialize(rs.getBytes(3));
//                    vault.setContents(items, unused -> {});
//                    vault.setIcon(rs.getString(4) == null ? null : Material.valueOf(rs.getString(4)));
//                }
//            }
//        } catch (SQLException ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    @Override
//    public void disable() {
//        try {
//            dataSource.close();
//        } catch (Exception ex) {
//            ex.printStackTrace();
//        }
//    }
//
//    private enum ChangeType {
//        DELETE, UPDATE, INSERT;
//
//        public static final ChangeType[] entries = ChangeType.values();
//    }
//}
