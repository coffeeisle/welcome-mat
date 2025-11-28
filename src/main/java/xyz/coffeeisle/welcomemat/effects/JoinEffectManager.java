package xyz.coffeeisle.welcomemat.effects;

import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.ConfigManager;
import xyz.coffeeisle.welcomemat.WelcomeMat;
import xyz.coffeeisle.welcomemat.database.DatabaseManager;

public class JoinEffectManager {
    private final WelcomeMat plugin;

    public JoinEffectManager(WelcomeMat plugin) {
        this.plugin = plugin;
    }

    public void playEffect(Player player) {
        if (!plugin.getConfig().getBoolean("effects.enabled")) {
            return;
        }

        DatabaseManager db = plugin.getDatabaseManager();
        if (!db.getEffectPreference(player.getUniqueId())) {
            return;
        }

        AnimationRegistry registry = plugin.getAnimationRegistry();
        if (registry == null) {
            return;
        }

        String preference = db.getAnimationPreference(player.getUniqueId());
        if (preference == null || preference.isEmpty()) {
            preference = DatabaseManager.ANIMATION_FOLLOW_PACK;
        }

        boolean played = false;
        if (DatabaseManager.ANIMATION_RANDOM.equalsIgnoreCase(preference)) {
            played = registry.playRandomAnimation(player);
        } else if (DatabaseManager.ANIMATION_FOLLOW_PACK.equalsIgnoreCase(preference)) {
            played = playConfiguredAnimation(player, registry);
        } else {
            played = registry.playAnimation(player, preference);
            if (!played) {
                // Animation was removed; fall back to pack behavior
                db.setAnimationPreference(player.getUniqueId(), DatabaseManager.ANIMATION_FOLLOW_PACK);
                played = playConfiguredAnimation(player, registry);
            }
        }

        if (!played) {
            playFallbackAnimation(player, registry);
        }
    }

    private boolean playConfiguredAnimation(Player player, AnimationRegistry registry) {
        ConfigManager config = plugin.getConfigManager();
        String animationId = config.getEffectiveAnimationId();
        if (animationId == null || animationId.isEmpty()) {
            return false;
        }
        return registry.playAnimation(player, animationId);
    }

    private void playFallbackAnimation(Player player, AnimationRegistry registry) {
        String defaultId = plugin.getConfigManager().getDefaultAnimationId();
        if (defaultId != null && !defaultId.isEmpty()) {
            registry.playAnimation(player, defaultId);
        } else {
            registry.playRandomAnimation(player);
        }
    }
}