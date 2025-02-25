package xyz.coffeeisle.welcomemat;

import org.bukkit.ChatColor;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

public class ConfigManager {
    private final WelcomeMat plugin;
    private FileConfiguration config;
    
    // Default delay values in milliseconds
    private static final long DEFAULT_JOIN_DELAY = 0;
    private static final long DEFAULT_WELCOME_DELAY = 1000;
    private static final long DEFAULT_TITLE_DELAY = 500;

    public ConfigManager(WelcomeMat plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
        
        // Ensure default delay configurations exist
        if (!config.isSet("delays.join")) {
            config.set("delays.join", DEFAULT_JOIN_DELAY);
        }
        if (!config.isSet("delays.welcome")) {
            config.set("delays.welcome", DEFAULT_WELCOME_DELAY);
        }
        if (!config.isSet("delays.title")) {
            config.set("delays.title", DEFAULT_TITLE_DELAY);
        }
        plugin.saveConfig();
    }

    public String getJoinMessage(String playerName, boolean isFirstJoin) {
        String pack = config.getString("message-packs.selected", "default");
        String messageType = isFirstJoin ? "first-join" : "join";
        List<String> messages = config.getStringList("message-packs." + pack + "." + messageType);
        
        if (messages == null || messages.isEmpty()) {
            // Fallback to default message
            String message = config.getString("messages.join", "&aWelcome &e%player% &ato the server!");
            return ChatColor.translateAlternateColorCodes('&', message.replace("%player%", playerName));
        }
        
        // Get random message from pack
        String message = messages.get((int) (Math.random() * messages.size()));
        return ChatColor.translateAlternateColorCodes('&', message.replace("%player%", playerName));
    }

    public long getMessageDelay(String type) {
        return config.getLong("delays." + type, DEFAULT_JOIN_DELAY);
    }
    
    public String getLeaveMessage(String playerName) {
        String pack = config.getString("message-packs.selected", "default");
        List<String> messages = config.getStringList("message-packs." + pack + ".leave");
        
        if (messages == null || messages.isEmpty()) {
            // Fallback to default message
            String message = config.getString("messages.leave", "&e%player% &chas left the server!");
            return ChatColor.translateAlternateColorCodes('&', message.replace("%player%", playerName));
        }
        
        // Get random message from pack
        String message = messages.get((int) (Math.random() * messages.size()));
        return ChatColor.translateAlternateColorCodes('&', message.replace("%player%", playerName));
    }

    public String getJoinTitle() {
        return ChatColor.translateAlternateColorCodes('&', 
            config.getString("titles.join.title", "&6Welcome!"));
    }

    public String getJoinSubtitle() {
        return ChatColor.translateAlternateColorCodes('&', 
            config.getString("titles.join.subtitle", "&eEnjoy your stay!"));
    }

    public boolean isJoinMessageEnabled() {
        return config.getBoolean("features.join-message", true);
    }

    public boolean isLeaveMessageEnabled() {
        return config.getBoolean("features.leave-message", true);
    }

    public boolean isJoinTitleEnabled() {
        return config.getBoolean("features.join-title", true);
    }

    public boolean isJoinSoundEnabled() {
        return config.getBoolean("features.join-sound", true);
    }

    public boolean isLeaveSoundEnabled() {
        return config.getBoolean("features.leave-sound", true);
    }

    public Sound getJoinSound() {
        String soundName = config.getString("sounds.join.sound", "ENTITY_PLAYER_LEVELUP");
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid join sound in config: " + soundName);
            // Try legacy sound name (1.8)
            try {
                return Sound.valueOf("LEVEL_UP");
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Fallback sound also failed. Join sounds will be disabled.");
                return null;
            }
        }
    }

    public Sound getLeaveSound() {
        String soundName = config.getString("sounds.leave.sound", "ENTITY_IRON_GOLEM_DEATH");
        try {
            return Sound.valueOf(soundName.toUpperCase());
        } catch (IllegalArgumentException e) {
            // Try legacy sound name (1.8)
            try {
                return Sound.valueOf("IRONGOLEM_DEATH");
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Could not find sound: " + soundName + ". Sound disabled.");
                return null;
            }
        }
    }

    public float getJoinSoundVolume() {
        return (float) config.getDouble("sounds.join.volume", 1.0);
    }

    public float getJoinSoundPitch() {
        return (float) config.getDouble("sounds.join.pitch", 1.0);
    }

    public float getLeaveSoundVolume() {
        return (float) config.getDouble("sounds.leave.volume", 0.5);
    }

    public float getLeaveSoundPitch() {
        return (float) config.getDouble("sounds.leave.pitch", 1.0);
    }

    public boolean isOtherPlayersSoundsEnabled() {
        return config.getBoolean("features.other-players-sounds", true);
    }

    public void setSoundPreference(String playerUUID, boolean enabled) {
        config.set("player-preferences.sounds." + playerUUID, enabled);
        plugin.saveConfig();
    }

    public boolean getSoundPreference(String playerUUID) {
        return config.getBoolean("player-preferences.sounds." + playerUUID, true);
    }

    public boolean setMessagePack(String pack) {
        if (!config.contains("message-packs." + pack)) {
            return false;
        }
        config.set("message-packs.selected", pack);
        plugin.saveConfig();
        return true;
    }

    public String getCurrentMessagePack() {
        return config.getString("message-packs.selected", "default");
    }

    public List<String> getAvailableMessagePacks() {
        ConfigurationSection packs = config.getConfigurationSection("message-packs");
        if (packs == null) return new ArrayList<>();
        
        return packs.getKeys(false).stream()
            .filter(key -> !key.equals("selected"))
            .collect(Collectors.toList());
    }

    public void set(String path, String value) {
        config.set(path, value);
        plugin.saveConfig();
    }

    public Object get(String path) {
        return config.get(path);
    }

    public List<String> getKeys(String path) {
        if (path.isEmpty()) {
            return new ArrayList<>(config.getKeys(false));
        }
        
        ConfigurationSection section = config.getConfigurationSection(path);
        if (section == null) {
            return new ArrayList<>();
        }
        
        return new ArrayList<>(section.getKeys(false));
    }

    public void set(String path, Object value) {
        config.set(path, value);
        plugin.saveConfig();
    }

    public String getString(String path) {
        return config.getString(path);
    }

    public float getFloat(String path, float defaultValue) {
        return (float) config.getDouble(path, defaultValue);
    }
}