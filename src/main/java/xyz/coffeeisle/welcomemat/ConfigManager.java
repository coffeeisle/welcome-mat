package xyz.coffeeisle.welcomemat;

import org.bukkit.ChatColor;
import xyz.coffeeisle.welcomemat.WelcomeMat;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.coffeeisle.welcomemat.utils.ConfigFileMigrator;
import xyz.coffeeisle.welcomemat.utils.VersionUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Collections;
import java.util.stream.Collectors;

public class ConfigManager {
    private final WelcomeMat plugin;
    private FileConfiguration config;
    private static final String CONFIG_VERSION = "1.3";
    private static final String CONFIG_VERSION_PATH = "config-version";
    
    // Default delay values in milliseconds
    private static final long DEFAULT_JOIN_DELAY = 0;
    private static final long DEFAULT_WELCOME_DELAY = 1000;
    private static final long DEFAULT_TITLE_DELAY = 500;

    private static final String DEFAULT_JOIN_MESSAGE = "&e%player% &ajust joined the server!";
    private static final String DEFAULT_LEAVE_MESSAGE = "&e%player% &chas left the server!";
    private static final String DEFAULT_TITLE = "&6Welcome!";
    private static final String DEFAULT_SUBTITLE = "&eEnjoy your stay!";

    public ConfigManager(WelcomeMat plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    public void loadConfig() {
        plugin.saveDefaultConfig();
        File configFile = new File(plugin.getDataFolder(), "config.yml");
        ConfigFileMigrator.migrateIfOutdated(plugin, configFile, "config.yml", CONFIG_VERSION_PATH, CONFIG_VERSION);
        plugin.reloadConfig();
        config = plugin.getConfig();
        migrateConfigIfNeeded();
        
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

        ensureMessageDefaults();
        ensureAnimationDefaults();
        config.set(CONFIG_VERSION_PATH, CONFIG_VERSION);
        plugin.saveConfig();
    }

    private void migrateConfigIfNeeded() {
        String currentVersion = config.getString(CONFIG_VERSION_PATH, "0");
        boolean migratedLegacyPacks = migrateEmbeddedMessagePacks();
        if (VersionUtils.compare(currentVersion, CONFIG_VERSION) < 0 || migratedLegacyPacks) {
            config.set(CONFIG_VERSION_PATH, CONFIG_VERSION);
        }
    }

    private boolean migrateEmbeddedMessagePacks() {
        ConfigurationSection packsSection = config.getConfigurationSection("message-packs");
        if (packsSection == null) {
            return false;
        }

        boolean movedLegacyData = false;
        boolean legacySaved = true;
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }
        YamlConfiguration messagesYaml = YamlConfiguration.loadConfiguration(messagesFile);

        for (String key : packsSection.getKeys(false)) {
            if ("selected".equalsIgnoreCase(key)) {
                continue;
            }
            ConfigurationSection legacySection = packsSection.getConfigurationSection(key);
            if (legacySection == null) {
                continue;
            }
            boolean containsMessages = legacySection.isConfigurationSection("messages") || legacySection.isConfigurationSection("splash");
            if (!containsMessages) {
                continue;
            }

            String targetPath = "message-packs." + key;
            if (messagesYaml.contains(targetPath)) {
                continue;
            }

            messagesYaml.set(targetPath, legacySection.getValues(true));
            movedLegacyData = true;
        }

        if (movedLegacyData) {
            try {
                messagesYaml.save(messagesFile);
                plugin.getLogger().info("Migrated legacy message packs from config.yml to messages.yml");
            } catch (IOException e) {
                legacySaved = false;
                plugin.getLogger().severe("Failed to migrate legacy message packs: " + e.getMessage());
            }
            if (!legacySaved) {
                return false;
            }
        }

        String selectedPack = packsSection.getString("selected", "default");
        config.set("message-packs", null);
        config.set("message-packs.selected", selectedPack);
        return movedLegacyData;
    }

    private void ensureMessageDefaults() {
        if (!config.isSet("messages.use-packs.join")) {
            config.set("messages.use-packs.join", false);
        }
        if (!config.isSet("messages.use-packs.leave")) {
            config.set("messages.use-packs.leave", false);
        }
        if (!config.isSet("messages.use-packs.splash")) {
            config.set("messages.use-packs.splash", false);
        }

        if (!config.isList("messages.join")) {
            String existing = config.getString("messages.join", DEFAULT_JOIN_MESSAGE);
            config.set("messages.join", Collections.singletonList(existing));
        }
        if (!config.isList("messages.leave")) {
            String existing = config.getString("messages.leave", DEFAULT_LEAVE_MESSAGE);
            config.set("messages.leave", Collections.singletonList(existing));
        }
        if (!config.isSet("titles.join.title")) {
            config.set("titles.join.title", DEFAULT_TITLE);
        }
        if (!config.isSet("titles.join.subtitle")) {
            config.set("titles.join.subtitle", DEFAULT_SUBTITLE);
        }
    }

    private void ensureAnimationDefaults() {
        if (!config.isSet("effects.animation.source")) {
            config.set("effects.animation.source", "pack");
        }
        if (!config.isSet("effects.animation.default")) {
            config.set("effects.animation.default", "fire_spiral");
        }
    }

    public String getJoinMessage(String playerName, boolean isFirstJoin) {
        List<String> messages;
        if (usePackForJoinMessages()) {
            messages = plugin.getLanguageManager().getPackMessages(
                getCurrentMessagePack(),
                isFirstJoin ? "first-join" : "join"
            );
            if (messages.isEmpty() && isFirstJoin) {
                messages = plugin.getLanguageManager().getPackMessages(getCurrentMessagePack(), "join");
            }
        } else {
            messages = getConfigMessages("messages." + (isFirstJoin ? "first-join" : "join"), DEFAULT_JOIN_MESSAGE);
            if (messages.isEmpty() && isFirstJoin) {
                messages = getConfigMessages("messages.join", DEFAULT_JOIN_MESSAGE);
            }
        }

        if (messages.isEmpty()) {
            messages = Collections.singletonList(DEFAULT_JOIN_MESSAGE);
        }

        String message = messages.get((int) (Math.random() * messages.size()));
        return ChatColor.translateAlternateColorCodes('&', message.replace("%player%", playerName));
    }

    public long getMessageDelay(String type) {
        return config.getLong("delays." + type, DEFAULT_JOIN_DELAY);
    }
    
    public String getLeaveMessage(String playerName) {
        List<String> messages;
        if (usePackForLeaveMessages()) {
            messages = plugin.getLanguageManager().getPackMessages(
                getCurrentMessagePack(),
                "leave"
            );
        } else {
            messages = getConfigMessages("messages.leave", DEFAULT_LEAVE_MESSAGE);
        }

        if (messages.isEmpty()) {
            messages = Collections.singletonList(DEFAULT_LEAVE_MESSAGE);
        }

        String message = messages.get((int) (Math.random() * messages.size()));
        return ChatColor.translateAlternateColorCodes('&', message.replace("%player%", playerName));
    }

    public String getJoinTitle() {
        String raw;
        if (usePackForSplash()) {
            raw = plugin.getLanguageManager().getPackSplashTitle(getCurrentMessagePack());
        } else {
            raw = config.getString("titles.join.title", DEFAULT_TITLE);
        }
        if (raw == null || raw.isEmpty()) {
            raw = DEFAULT_TITLE;
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public String getJoinSubtitle() {
        String raw;
        if (usePackForSplash()) {
            raw = plugin.getLanguageManager().getPackSplashSubtitle(getCurrentMessagePack());
        } else {
            raw = config.getString("titles.join.subtitle", DEFAULT_SUBTITLE);
        }
        if (raw == null || raw.isEmpty()) {
            raw = DEFAULT_SUBTITLE;
        }
        return ChatColor.translateAlternateColorCodes('&', raw);
    }

    public String getRawJoinTitle() {
        return config.getString("titles.join.title", "&6Welcome!");
    }

    public String getRawJoinSubtitle() {
        return config.getString("titles.join.subtitle", "&eEnjoy your stay!");
    }

    public void updateJoinTitle(String rawValue) {
        config.set("titles.join.title", rawValue);
        setUsePackForSplash(false);
        plugin.saveConfig();
    }

    public void updateJoinSubtitle(String rawValue) {
        config.set("titles.join.subtitle", rawValue);
        setUsePackForSplash(false);
        plugin.saveConfig();
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
        if (!plugin.getLanguageManager().getMessagePackIds().contains(pack)) {
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
        return new ArrayList<>(plugin.getLanguageManager().getMessagePackIds());
    }

    public boolean usePackForJoinMessages() {
        return config.getBoolean("messages.use-packs.join", false);
    }

    public boolean usePackForLeaveMessages() {
        return config.getBoolean("messages.use-packs.leave", false);
    }

    public boolean usePackForSplash() {
        return config.getBoolean("messages.use-packs.splash", false);
    }

    public void setUsePackForJoinMessages(boolean usePack) {
        config.set("messages.use-packs.join", usePack);
        plugin.saveConfig();
    }

    public void setUsePackForLeaveMessages(boolean usePack) {
        config.set("messages.use-packs.leave", usePack);
        plugin.saveConfig();
    }

    public void setUsePackForSplash(boolean usePack) {
        config.set("messages.use-packs.splash", usePack);
        plugin.saveConfig();
    }

    public String getAnimationSource() {
        return config.getString("effects.animation.source", "pack");
    }

    public boolean usePackForAnimations() {
        return "pack".equalsIgnoreCase(getAnimationSource());
    }

    public void setAnimationSource(String source) {
        config.set("effects.animation.source", source);
        plugin.saveConfig();
    }

    public String getDefaultAnimationId() {
        return config.getString("effects.animation.default", "fire_spiral");
    }

    public void setDefaultAnimationId(String animationId) {
        config.set("effects.animation.default", animationId);
        plugin.saveConfig();
    }

    public String getPackAnimationId() {
        return plugin.getLanguageManager().getPackAnimation(getCurrentMessagePack());
    }

    public String getEffectiveAnimationId() {
        if (usePackForAnimations()) {
            String packAnimation = getPackAnimationId();
            if (packAnimation != null && !packAnimation.isEmpty()) {
                return packAnimation;
            }
        }
        return getDefaultAnimationId();
    }

    public List<String> getCustomJoinMessages() {
        return getConfigMessages("messages.join", DEFAULT_JOIN_MESSAGE);
    }

    public List<String> getCustomFirstJoinMessages() {
        return getConfigMessages("messages.first-join", DEFAULT_JOIN_MESSAGE);
    }

    public List<String> getCustomLeaveMessages() {
        return getConfigMessages("messages.leave", DEFAULT_LEAVE_MESSAGE);
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

    private List<String> getConfigMessages(String path, String fallback) {
        if (config.isList(path)) {
            List<String> list = config.getStringList(path);
            return list != null ? list : Collections.emptyList();
        }
        String value = config.getString(path, fallback);
        if (value == null || value.isEmpty()) {
            return fallback == null ? Collections.emptyList() : Collections.singletonList(fallback);
        }
        return Collections.singletonList(value);
    }
}