package xyz.coffeeisle.welcomemat;

import org.bukkit.plugin.java.JavaPlugin;
import net.md_5.bungee.api.ChatColor;
import xyz.coffeeisle.welcomemat.database.DatabaseManager;
import xyz.coffeeisle.welcomemat.commands.WelcomeMatCommand;
import xyz.coffeeisle.welcomemat.LanguageManager;
import xyz.coffeeisle.welcomemat.gui.GUIListener;
import xyz.coffeeisle.welcomemat.effects.JoinEffectManager;
import org.bukkit.command.PluginCommand;
import xyz.coffeeisle.welcomemat.effects.AnimationRegistry;
import xyz.coffeeisle.welcomemat.analytics.Analytics;
import xyz.coffeeisle.welcomemat.utils.SchedulerAdapter;

public class WelcomeMat extends JavaPlugin {
    private static WelcomeMat instance;
    private ConfigManager configManager;
    private DatabaseManager databaseManager;
    private LanguageManager languageManager;
    private JoinEffectManager joinEffectManager;
    private AnimationRegistry animationRegistry;
    private Analytics analytics;
    private SchedulerAdapter schedulerAdapter;

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
        
        // Initialize AnimationRegistry
        this.animationRegistry = new AnimationRegistry();

        // Scheduler helper (Folia-aware)
        this.schedulerAdapter = new SchedulerAdapter(this);

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
        
        // Create plugin directory if it doesn't exist
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }

        // Initialize managers in the correct order
        this.configManager = new ConfigManager(this);
        this.languageManager = new LanguageManager(this);
        
        // Initialize database first and verify connection
        this.databaseManager = new DatabaseManager(this);
        if (!this.databaseManager.initialize()) {
            getLogger().warning("Failed to initialize database - using config fallback");
            // Continue loading as we have config fallback
        }

        // Initialize effects manager after database
        this.joinEffectManager = new JoinEffectManager(this);

        // Register events and commands
        getServer().getPluginManager().registerEvents(new PlayerEventListener(this), this);
        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        
        PluginCommand cmd = getCommand("welcomemat");
        if (cmd != null) {
            WelcomeMatCommand executor = new WelcomeMatCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        // Initialize and start analytics
        this.analytics = new Analytics(this);
        this.analytics.startTracking();

        getLogger().info("WelcomeMat has been enabled!");
    }

    @Override
    public void onDisable() {
        if (databaseManager != null) {
            databaseManager.closeConnection();
        }
        if (analytics != null) {
            analytics.shutdown();
        }
        getLogger().info("WelcomeMat has been disabled!");
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

    public JoinEffectManager getJoinEffectManager() {
        return joinEffectManager;
    }
    public AnimationRegistry getAnimationRegistry() {
        return animationRegistry;
    }

    public SchedulerAdapter getSchedulerAdapter() {
        return schedulerAdapter;
    }
}