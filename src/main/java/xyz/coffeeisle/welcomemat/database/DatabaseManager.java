package xyz.coffeeisle.welcomemat.database;

import xyz.coffeeisle.welcomemat.WelcomeMat;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.io.File;

public class DatabaseManager {
    private final WelcomeMat plugin;
    private Connection connection;
    private final String dbPath;
    private final Map<UUID, Boolean> soundPreferencesCache = new HashMap<>();

    public DatabaseManager(WelcomeMat plugin) {
        this.plugin = plugin;
        this.dbPath = plugin.getDataFolder().getAbsolutePath() + "/player_preferences.db";
        initializeDatabase();
    }

    private void initializeDatabase() {
        File dbFile = new File(plugin.getDataFolder(), "player_preferences.db");
        boolean dbExists = dbFile.exists();
        
        try {
            // Load SQLite JDBC driver
            Class.forName("org.sqlite.JDBC");
            
            // Ensure plugin folder exists
            if (!plugin.getDataFolder().exists()) {
                plugin.getDataFolder().mkdirs();
            }
            
            // Log database status
            plugin.getLogger().info("Database " + (dbExists ? "found" : "not found") + 
                " at: " + dbFile.getAbsolutePath());
            
            // Initialize connection
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            
            // Enable foreign keys and WAL mode for better performance
            try (Statement stmt = connection.createStatement()) {
                stmt.execute("PRAGMA foreign_keys = ON");
                stmt.execute("PRAGMA journal_mode = WAL");
                
                // Create table if it doesn't exist (this is safe even if table exists)
                stmt.execute("CREATE TABLE IF NOT EXISTS sound_preferences (" +
                        "uuid VARCHAR(36) PRIMARY KEY," +
                        "enabled BOOLEAN NOT NULL DEFAULT 1)");
            }
            
            // If this is a new database, log it
            if (!dbExists) {
                plugin.getLogger().info("New database created successfully!");
            } else {
                plugin.getLogger().info("Connected to existing database successfully!");
            }
            
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to initialize database: " + e.getMessage());
            plugin.getLogger().severe("Using fallback configuration storage for sound preferences");
            e.printStackTrace();
        }
    }

    public void setSoundPreference(UUID uuid, boolean enabled) {
        if (connection == null) {
            // Fallback to config
            plugin.getConfigManager().setSoundPreference(uuid.toString(), enabled);
            return;
        }

        try (PreparedStatement stmt = connection.prepareStatement(
                "INSERT OR REPLACE INTO sound_preferences (uuid, enabled) VALUES (?, ?)")) {
            stmt.setString(1, uuid.toString());
            stmt.setBoolean(2, enabled);
            stmt.executeUpdate();
            soundPreferencesCache.put(uuid, enabled);
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to save sound preference: " + e.getMessage());
            // Fallback to config
            plugin.getConfigManager().setSoundPreference(uuid.toString(), enabled);
        }
    }

    public boolean getSoundPreference(UUID uuid) {
        if (connection == null) {
            // Fallback to config
            return plugin.getConfigManager().getSoundPreference(uuid.toString());
        }

        if (soundPreferencesCache.containsKey(uuid)) {
            return soundPreferencesCache.get(uuid);
        }

        try (PreparedStatement stmt = connection.prepareStatement(
                "SELECT enabled FROM sound_preferences WHERE uuid = ?")) {
            stmt.setString(1, uuid.toString());
            ResultSet rs = stmt.executeQuery();
            
            if (rs.next()) {
                boolean enabled = rs.getBoolean("enabled");
                soundPreferencesCache.put(uuid, enabled);
                return enabled;
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to get sound preference: " + e.getMessage());
            // Fallback to config
            return plugin.getConfigManager().getSoundPreference(uuid.toString());
        }

        return true; // Default enabled
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            plugin.getLogger().severe("Failed to close database: " + e.getMessage());
        }
    }

    public boolean isDatabaseAvailable() {
        File dbFile = new File(plugin.getDataFolder(), "player_preferences.db");
        boolean exists = dbFile.exists();
        plugin.getLogger().info("Database file " + (exists ? "exists" : "does not exist") + 
            " at: " + dbFile.getAbsolutePath());
        return exists;
    }

    public boolean isDatabaseConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }
} 