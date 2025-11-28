package xyz.coffeeisle.welcomemat;

import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import xyz.coffeeisle.welcomemat.utils.VersionUtils;

public class LanguageManager {
    private final WelcomeMat plugin;
    private FileConfiguration messages;
    private FileConfiguration customPacks;
    private File messagesFile;
    private File customsFile;
    private String currentLanguage;
    private final Map<String, String> languageCache = new HashMap<>();
    private static final String MESSAGES_VERSION = "1.3";
    private static final String CUSTOMS_VERSION = "1.0";

    public LanguageManager(WelcomeMat plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        if (!messagesFile.exists()) {
            plugin.saveResource("messages.yml", false);
        }

        messages = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Load default messages from jar for fallback
        InputStream defaultStream = plugin.getResource("messages.yml");
        if (defaultStream != null) {
            YamlConfiguration defaultMessages = YamlConfiguration.loadConfiguration(
                new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            messages.setDefaults(defaultMessages);
        }

        migrateMessagesFile();
        loadCustomPacks();

        currentLanguage = messages.getString("selected-language", "english");
        languageCache.clear();
    }

    private void migrateMessagesFile() {
        String detectedVersion = messages.getString("messages-version", "0");
        if (VersionUtils.compare(detectedVersion, MESSAGES_VERSION) < 0) {
            messages.set("messages-version", MESSAGES_VERSION);
            saveYaml(messages, messagesFile, "messages.yml");
        }
    }

    private void loadCustomPacks() {
        customsFile = new File(plugin.getDataFolder(), "customs.yml");
        if (!customsFile.exists()) {
            plugin.saveResource("customs.yml", false);
        }
        customPacks = YamlConfiguration.loadConfiguration(customsFile);
        migrateCustomFile();
    }

    private void migrateCustomFile() {
        if (customPacks == null) {
            return;
        }
        if (!customPacks.isConfigurationSection("message-packs")) {
            customPacks.createSection("message-packs");
        }
        String detectedVersion = customPacks.getString("customs-version", "0");
        if (VersionUtils.compare(detectedVersion, CUSTOMS_VERSION) < 0) {
            customPacks.set("customs-version", CUSTOMS_VERSION);
            saveYaml(customPacks, customsFile, "customs.yml");
        }
    }

    private boolean saveYaml(FileConfiguration yaml, File file, String name) {
        try {
            yaml.save(file);
            return true;
        } catch (IOException e) {
            plugin.getLogger().severe("Failed to save " + name + ": " + e.getMessage());
            return false;
        }
    }

    public String getMessage(String path) {
        String cacheKey = currentLanguage + ":" + path;
        if (languageCache.containsKey(cacheKey)) {
            return languageCache.get(cacheKey);
        }

        String message = messages.getString("languages." + currentLanguage + ".messages." + path);
        if (message == null) {
            // Fallback to English block
            message = messages.getString("languages.english.messages." + path, "Missing message: " + path);
        }

        message = ChatColor.translateAlternateColorCodes('&', message);
        languageCache.put(cacheKey, message);
        return message;
    }

    public String getMessage(String path, Map<String, String> placeholders) {
        String message = getMessage(path);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    public List<String> getAvailableLanguages() {
        return messages.getConfigurationSection("languages").getKeys(false).stream()
            .map(lang -> lang + " (" + messages.getString("languages." + lang + ".name") + ")")
            .collect(Collectors.toList());
    }

    public boolean setLanguage(String language) {
        if (!messages.contains("languages." + language)) {
            return false;
        }
        currentLanguage = language;
        messages.set("selected-language", language);
        try {
            messages.save(new File(plugin.getDataFolder(), "messages.yml"));
            languageCache.clear();
            return true;
        } catch (Exception e) {
            plugin.getLogger().severe("Failed to save language setting: " + e.getMessage());
            return false;
        }
    }

    public String getCurrentLanguage() {
        return currentLanguage;
    }

    public String getMessagePackDisplayName(String packId) {
        if (packId == null || packId.isEmpty()) {
            return ChatColor.RED + "Unknown";
        }

        String localizedPath = "languages." + currentLanguage + ".message-packs." + packId + ".name";
        String defaultPath = "message-packs." + packId + ".name";
        String customPath = "message-packs." + packId + ".name";

        String name = messages.getString(localizedPath);
        if (name == null && customPacks != null) {
            name = customPacks.getString(customPath);
        }
        if (name == null) {
            name = messages.getString(defaultPath);
        }

        if (name != null) {
            return ChatColor.translateAlternateColorCodes('&', name);
        }

        String[] parts = packId.replace('-', ' ').replace('_', ' ').split(" ");
        StringBuilder builder = new StringBuilder();
        for (String part : parts) {
            if (part.isEmpty()) {
                continue;
            }
            if (builder.length() > 0) {
                builder.append(' ');
            }
            builder.append(Character.toUpperCase(part.charAt(0)));
            if (part.length() > 1) {
                builder.append(part.substring(1).toLowerCase());
            }
        }

        return builder.length() > 0 ? builder.toString() : packId;
    }

    public List<String> getMessagePackIds() {
        Set<String> ids = new LinkedHashSet<>();
        if (messages.isConfigurationSection("message-packs")) {
            ids.addAll(messages.getConfigurationSection("message-packs").getKeys(false));
        }
        if (customPacks != null && customPacks.isConfigurationSection("message-packs")) {
            ids.addAll(customPacks.getConfigurationSection("message-packs").getKeys(false));
        }
        return new ArrayList<>(ids);
    }

    public List<String> getPackMessages(String packId, String messageType) {
        if (packId == null || packId.isEmpty()) {
            return Collections.emptyList();
        }

        String path = "messages." + messageType;
        ConfigurationSection section = resolvePackSection(packId);
        if (section == null) {
            return Collections.emptyList();
        }

        if (section.isList(path)) {
            List<String> values = section.getStringList(path);
            return values != null ? values : Collections.emptyList();
        }
        if (section.isString(path)) {
            return Collections.singletonList(section.getString(path));
        }
        return Collections.emptyList();
    }

    public String getPackSplashTitle(String packId) {
        ConfigurationSection section = resolvePackSection(packId);
        if (section == null) {
            return null;
        }
        return section.getString("splash.title");
    }

    public String getPackSplashSubtitle(String packId) {
        ConfigurationSection section = resolvePackSection(packId);
        if (section == null) {
            return null;
        }
        return section.getString("splash.subtitle");
    }

    public boolean saveCustomPack(
            String packId,
            String displayName,
            List<String> joinMessages,
            List<String> firstJoinMessages,
            List<String> leaveMessages,
            String title,
            String subtitle,
            String createdBy) {
        if (customPacks == null) {
            loadCustomPacks();
        }

        ConfigurationSection root = customPacks.getConfigurationSection("message-packs");
        if (root == null) {
            root = customPacks.createSection("message-packs");
        }

        ConfigurationSection packSection = root.createSection(packId);
        packSection.set("name", displayName);
        if (joinMessages != null && !joinMessages.isEmpty()) {
            packSection.set("messages.join", joinMessages);
        }
        if (firstJoinMessages != null && !firstJoinMessages.isEmpty()) {
            packSection.set("messages.first-join", firstJoinMessages);
        }
        if (leaveMessages != null && !leaveMessages.isEmpty()) {
            packSection.set("messages.leave", leaveMessages);
        }
        if (title != null && !title.isEmpty()) {
            packSection.set("splash.title", title);
        }
        if (subtitle != null && !subtitle.isEmpty()) {
            packSection.set("splash.subtitle", subtitle);
        }

        ConfigurationSection metadata = packSection.createSection("metadata");
        metadata.set("created-by", createdBy);
        metadata.set("created-at", Instant.now().toString());

        boolean saved = saveYaml(customPacks, customsFile, "customs.yml");
        languageCache.clear();
        return saved;
    }

    private ConfigurationSection resolvePackSection(String packId) {
        if (customPacks != null) {
            ConfigurationSection customSection = customPacks.getConfigurationSection("message-packs." + packId);
            if (customSection != null) {
                return customSection;
            }
        }
        return messages.getConfigurationSection("message-packs." + packId);
    }
} 