package xyz.coffeeisle.welcomemat.utils;

import org.bukkit.configuration.file.YamlConfiguration;
import xyz.coffeeisle.welcomemat.WelcomeMat;
import xyz.coffeeisle.welcomemat.utils.VersionUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Set;
import java.util.logging.Logger;

public final class ConfigFileMigrator {
    private static final SimpleDateFormat BACKUP_FORMAT = new SimpleDateFormat("yyyyMMdd-HHmmss");

    private ConfigFileMigrator() {
    }

    public static void migrateIfOutdated(WelcomeMat plugin,
                                         File targetFile,
                                         String resourcePath,
                                         String versionPath,
                                         String targetVersion) {
        ensureResourceExists(plugin, targetFile, resourcePath);

        YamlConfiguration existing = YamlConfiguration.loadConfiguration(targetFile);
        String currentVersion = existing.getString(versionPath, "0");

        if (VersionUtils.compare(currentVersion, targetVersion) >= 0) {
            if (!targetVersion.equals(currentVersion)) {
                existing.set(versionPath, targetVersion);
                saveYaml(plugin.getLogger(), existing, targetFile);
            }
            return;
        }

        File backupFile = createBackup(plugin, targetFile);
        YamlConfiguration defaults = loadDefaults(plugin, resourcePath);
        if (defaults == null) {
            plugin.getLogger().severe("Unable to load bundled defaults for " + resourcePath + ". Migration skipped.");
            return;
        }

        mergeConfig(defaults, existing);
        defaults.set(versionPath, targetVersion);
        saveYaml(plugin.getLogger(), defaults, targetFile);
        plugin.getLogger().warning("Updated " + targetFile.getName() + " from version " + currentVersion +
            " to " + targetVersion + ". Backup saved as " + backupFile.getName() + ".");
    }

    private static void ensureResourceExists(WelcomeMat plugin, File targetFile, String resourcePath) {
        if (!targetFile.exists()) {
            targetFile.getParentFile().mkdirs();
            plugin.saveResource(resourcePath, false);
        }
    }

    private static File createBackup(WelcomeMat plugin, File original) {
        File backup = new File(original.getParentFile(), original.getName() + ".bak-" + BACKUP_FORMAT.format(new Date()));
        try {
            Files.copy(original.toPath(), backup.toPath(), StandardCopyOption.REPLACE_EXISTING);
            return backup;
        } catch (IOException ex) {
            plugin.getLogger().severe("Failed to create backup for " + original.getName() + ": " + ex.getMessage());
            return original;
        }
    }

    private static YamlConfiguration loadDefaults(WelcomeMat plugin, String resourcePath) {
        InputStream stream = plugin.getResource(resourcePath);
        if (stream == null) {
            return null;
        }
        YamlConfiguration defaults = new YamlConfiguration();
        try (InputStreamReader reader = new InputStreamReader(stream, StandardCharsets.UTF_8)) {
            defaults.load(reader);
            return defaults;
        } catch (Exception ex) {
            plugin.getLogger().severe("Failed to read default resource " + resourcePath + ": " + ex.getMessage());
            return null;
        }
    }

    private static void mergeConfig(YamlConfiguration destination, YamlConfiguration source) {
        Set<String> keys = source.getKeys(true);
        for (String key : keys) {
            if (key == null || key.isEmpty()) {
                continue;
            }
            Object value = source.get(key);
            if (value != null) {
                destination.set(key, value);
            }
        }
    }

    private static void saveYaml(Logger logger, YamlConfiguration yaml, File target) {
        try {
            yaml.save(target);
        } catch (IOException ex) {
            logger.severe("Failed to save " + target.getName() + ": " + ex.getMessage());
        }
    }
}
