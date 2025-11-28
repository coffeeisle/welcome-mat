package xyz.coffeeisle.welcomemat;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LanguageManager {
    private final WelcomeMat plugin;
    private FileConfiguration messages;
    private String currentLanguage;
    private final Map<String, String> languageCache = new HashMap<>();

    public LanguageManager(WelcomeMat plugin) {
        this.plugin = plugin;
        loadMessages();
    }

    public void loadMessages() {
        File messagesFile = new File(plugin.getDataFolder(), "messages.yml");
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

        currentLanguage = messages.getString("selected-language", "en_US");
        languageCache.clear();
    }

    public String getMessage(String path) {
        String cacheKey = currentLanguage + ":" + path;
        if (languageCache.containsKey(cacheKey)) {
            return languageCache.get(cacheKey);
        }

        String message = messages.getString("languages." + currentLanguage + ".messages." + path);
        if (message == null) {
            // Fallback to en_US
            message = messages.getString("languages.en_US.messages." + path, "Missing message: " + path);
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

        String name = messages.getString(localizedPath);
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
} 