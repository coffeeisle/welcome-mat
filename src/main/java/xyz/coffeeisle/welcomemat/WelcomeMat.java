package xyz.coffeeisle.welcomemat;

import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.ChatColor;
import xyz.coffeeisle.welcomemat.database.DatabaseManager;
import xyz.coffeeisle.welcomemat.commands.WelcomeMatCommand;
import xyz.coffeeisle.welcomemat.LanguageManager;
import xyz.coffeeisle.welcomemat.gui.GUIListener;

public class WelcomeMat extends JavaPlugin {
    private static WelcomeMat instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private LanguageManager languageManager;

    // ANSI color codes for console
    private static final String GOLD = "\u001B[33m";
    private static final String GRAY = "\u001B[90m";
    private static final String YELLOW = "\u001B[33m";
    private static final String RED = "\u001B[31m";
    private static final String GREEN = "\u001B[32m";
    private static final String RESET = "\u001B[0m";
    private static final String WHITE = "\u001B[37m";
    private static final String LIME = "\u001B[32m";

    @Override
    public void onEnable() {
        instance = this;
        
        // Display startup message
        getLogger().info(RED + "  __      __" + WHITE + "   _                  " + YELLOW + "  __  __       _   " + RESET);
        getLogger().info(RED + "  \\ \\    /" + WHITE + " /__| |__ ___ _ __  ___ " + YELLOW + " |  \\/  |__ _ | |_ " + RESET);
        getLogger().info(RED + "   \\ \\/\\/" + WHITE + " / -_) / _/ _ \\ '  \\/ -_)" + YELLOW + " | |\\/| / _` ||  _|" + RESET);
        getLogger().info(RED + "    \\_/\\_/" + WHITE + "\\___|_\\__\\___/_|_|_\\___|" + YELLOW + " |_|  |_\\__,_| \\__|" + RESET);
        getLogger().info(GRAY + "                                    v" + getDescription().getVersion() + RESET);
        getLogger().info("");
        getLogger().info(LIME + "Thank you for using WelcomeMat!" + RESET);
        getLogger().info(YELLOW + "Made with " + RED + "♥" + YELLOW + " by angeldev0" + RESET);
        getLogger().info("");
        
        // Initialize config
        saveDefaultConfig();
        configManager = new ConfigManager(this);
        
        // Initialize database
        databaseManager = new DatabaseManager(this);
        
        // Initialize language manager
        languageManager = new LanguageManager(this);
        
        // Verify database connection
        if (!databaseManager.isDatabaseConnected()) {
            getLogger().warning("Database connection failed - using config fallback for sound preferences");
        }
        
        // Register events
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
        
        // Register commands
        getCommand("welcomemat").setExecutor(new WelcomeMatCommand(this));
        
        // Register GUI listener
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        
        getLogger().info(GREEN + "WelcomeMat has been enabled!" + RESET);
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.close();
        }
        getLogger().info(RED + "WelcomeMat has been disabled!" + RESET);
        instance = null;
    }

    public static WelcomeMat getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public DatabaseManager getDatabaseManager() {
        return databaseManager;
    }

    public LanguageManager getLanguageManager() {
        return languageManager;
    }
} 