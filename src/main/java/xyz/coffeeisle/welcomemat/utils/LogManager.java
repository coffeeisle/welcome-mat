package xyz.coffeeisle.welcomemat.utils;

import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;

public class LogManager {
    private final WelcomeMat plugin;
    private final File logFile;
    private static final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public LogManager(WelcomeMat plugin) {
        this.plugin = plugin;
        this.logFile = new File(plugin.getDataFolder(), "admin_actions.log");
        createLogFileIfNotExists();
    }

    private void createLogFileIfNotExists() {
        if (!logFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                logFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create admin actions log file: " + e.getMessage());
            }
        }
    }

    public void logAdminAction(Player admin, String action, String details) {
        String timestamp = dateFormat.format(new Date());
        String logMessage = String.format("[%s] %s performed action: %s - %s", 
            timestamp, admin.getName(), action, details);

        // Log to console
        plugin.getLogger().info(logMessage);

        // Log to file
        try (PrintWriter writer = new PrintWriter(new FileWriter(logFile, true))) {
            writer.println(logMessage);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to write to admin actions log file: " + e.getMessage(), e);
        }
    }
}