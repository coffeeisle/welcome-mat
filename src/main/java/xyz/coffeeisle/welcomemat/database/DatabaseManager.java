package xyz.coffeeisle.welcomemat.database;

import xyz.coffeeisle.welcomemat.WelcomeMat;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private final WelcomeMat plugin;
    private Connection connection;
    private boolean initialized = false;
    public static final String ANIMATION_FOLLOW_PACK = "PACK";
    public static final String ANIMATION_RANDOM = "RANDOM";

    public DatabaseManager(WelcomeMat plugin) {
        this.plugin = plugin;
    }

    public boolean initialize() {
        try {
            // Test connection and create tables
            createTables();
            initialized = true;
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Database initialization failed: " + e.getMessage());
            return false;
        }
    }

    private void createTables() throws SQLException {
        try (Connection conn = getConnection();
             Statement stmt = conn.createStatement()) {
            
            // Create player_preferences table with both sound and effect columns
            stmt.execute("CREATE TABLE IF NOT EXISTS player_preferences ("
                    + "uuid VARCHAR(36) PRIMARY KEY,"
                    + "sounds_enabled BOOLEAN DEFAULT TRUE,"
                    + "effects_enabled BOOLEAN DEFAULT TRUE,"
                    + "selected_animation TEXT DEFAULT 'PACK'"
                    + ")");
            
            // Check if we need to add the effects_enabled column (for upgrades)
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "player_preferences", "effects_enabled")) {
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE player_preferences ADD COLUMN effects_enabled BOOLEAN DEFAULT TRUE");
                }
            }

            // Add animation selection column if missing
            try (ResultSet rs = conn.getMetaData().getColumns(null, null, "player_preferences", "selected_animation")) {
                if (!rs.next()) {
                    stmt.execute("ALTER TABLE player_preferences ADD COLUMN selected_animation TEXT DEFAULT 'PACK'");
                }
            }
            
            plugin.getLogger().info("Database tables verified and updated if needed");
        }
    }

    public boolean getEffectPreference(UUID uuid) {
        if (!initialized) {
            return plugin.getConfig().getBoolean("effects.enabled", true);
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT effects_enabled FROM player_preferences WHERE uuid = ?")) {
            
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                insertDefaultPreferences(conn, uuid);
                return true;
            }
            
            return rs.getBoolean("effects_enabled");
        } catch (SQLException e) {
            plugin.getLogger().warning("Database error, using config fallback: " + e.getMessage());
            return plugin.getConfig().getBoolean("effects.enabled", true);
        }
    }

    public void setEffectPreference(UUID uuid, boolean enabled) {
        if (!initialized) {
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_preferences (uuid, effects_enabled) VALUES (?, ?) " +
                             "ON CONFLICT(uuid) DO UPDATE SET effects_enabled = excluded.effects_enabled")) {

            stmt.setString(1, uuid.toString());
            stmt.setBoolean(2, enabled);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save effect preference: " + e.getMessage());
        }
    }

    public boolean getSoundPreference(UUID uuid) {
        if (!initialized) {
            return plugin.getConfig().getBoolean("sounds.enabled", true);
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT sounds_enabled FROM player_preferences WHERE uuid = ?")) {
            
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (!rs.next()) {
                insertDefaultPreferences(conn, uuid);
                return true;
            }
            
            return rs.getBoolean("sounds_enabled");
        } catch (SQLException e) {
            plugin.getLogger().warning("Database error, using config fallback: " + e.getMessage());
            return plugin.getConfig().getBoolean("sounds.enabled", true);
        }
    }

    public void setSoundPreference(UUID uuid, boolean enabled) {
        if (!initialized) {
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_preferences (uuid, sounds_enabled) VALUES (?, ?) " +
                             "ON CONFLICT(uuid) DO UPDATE SET sounds_enabled = excluded.sounds_enabled")) {

            stmt.setString(1, uuid.toString());
            stmt.setBoolean(2, enabled);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save sound preference: " + e.getMessage());
        }
    }

    public void setPreferences(UUID uuid, boolean sounds, boolean effects) {
        if (!initialized) {
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_preferences (uuid, sounds_enabled, effects_enabled) VALUES (?, ?, ?) " +
                             "ON CONFLICT(uuid) DO UPDATE SET " +
                             "sounds_enabled = excluded.sounds_enabled, " +
                             "effects_enabled = excluded.effects_enabled")) {

            stmt.setString(1, uuid.toString());
            stmt.setBoolean(2, sounds);
            stmt.setBoolean(3, effects);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save preferences: " + e.getMessage());
        }
    }

    public String getAnimationPreference(UUID uuid) {
        if (!initialized) {
            return ANIMATION_FOLLOW_PACK;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT selected_animation FROM player_preferences WHERE uuid = ?")) {

            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();

            if (!rs.next()) {
                insertDefaultPreferences(conn, uuid);
                return ANIMATION_FOLLOW_PACK;
            }

            String stored = rs.getString("selected_animation");
            return stored == null || stored.isEmpty() ? ANIMATION_FOLLOW_PACK : stored;
        } catch (SQLException e) {
            plugin.getLogger().warning("Database error while reading animation preference: " + e.getMessage());
            return ANIMATION_FOLLOW_PACK;
        }
    }

    public void setAnimationPreference(UUID uuid, String preference) {
        if (!initialized) {
            return;
        }

        try (Connection conn = getConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO player_preferences (uuid, selected_animation) VALUES (?, ?) " +
                             "ON CONFLICT(uuid) DO UPDATE SET selected_animation = excluded.selected_animation")) {

            stmt.setString(1, uuid.toString());
            stmt.setString(2, preference);
            stmt.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().warning("Failed to save animation preference: " + e.getMessage());
        }
    }

    private void insertDefaultPreferences(Connection conn, UUID uuid) throws SQLException {
        try (PreparedStatement insert = conn.prepareStatement(
            "INSERT OR IGNORE INTO player_preferences (uuid, sounds_enabled, effects_enabled, selected_animation) " +
                "VALUES (?, 1, 1, ?)")) {
            insert.setString(1, uuid.toString());
            insert.setString(2, ANIMATION_FOLLOW_PACK);
            insert.executeUpdate();
        }
    }

    private Connection getConnection() throws SQLException {
        if (connection == null || connection.isClosed()) {
            String dbPath = plugin.getDataFolder().getAbsolutePath() + "/welcomemat.db";
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbPath);
            
            // Enable foreign keys
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
            }
        }
        return connection;
    }

    public void closeConnection() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Error closing database: " + e.getMessage());
        }
    }
} 