package xyz.coffeeisle.welcomemat;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;

public class ConfigManager {
    private final WelcomeMat plugin;
    private FileConfiguration config;

    public ConfigManager(WelcomeMat plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }

    public String getJoinMessage(String playerName) {
        String message = config.getString("messages.join", "&aWelcome &e%player% &ato the server!");
        return ChatColor.translateAlternateColorCodes('&', message.replace("%player%", playerName));
    }

    public String getLeaveMessage(String playerName) {
        String message = config.getString("messages.leave", "&e%player% &chas left the server!");
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
            // Try legacy sound name (1.8)
            try {
                return Sound.valueOf("LEVEL_UP");
            } catch (IllegalArgumentException ex) {
                plugin.getLogger().warning("Could not find sound: " + soundName + ". Sound disabled.");
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
} 