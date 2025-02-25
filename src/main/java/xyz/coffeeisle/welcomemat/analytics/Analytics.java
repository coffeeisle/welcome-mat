package xyz.coffeeisle.welcomemat.analytics;

import org.bstats.bukkit.Metrics;
import org.bukkit.plugin.java.JavaPlugin;

public class Analytics {
    private final JavaPlugin plugin;
    private Metrics metrics = null;
    private boolean enabled;

    public Analytics(JavaPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
        if (enabled) {
            this.metrics = new Metrics(plugin, 24923);
        }
    }

    private void loadConfig() {
        this.enabled = plugin.getConfig().getBoolean("analytics.enabled", true);
        if (!plugin.getConfig().contains("analytics.enabled")) {
            plugin.getConfig().set("analytics.enabled", true);
            plugin.saveConfig();
        }
    }

    public void startTracking() {
        if (!enabled) {
            plugin.getLogger().info("Analytics are disabled. No data will be collected.");
        }
    }

    public void shutdown() {
        // No need to do anything, bStats handles shutdown automatically
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        plugin.getConfig().set("analytics.enabled", enabled);
        plugin.saveConfig();
    }
}