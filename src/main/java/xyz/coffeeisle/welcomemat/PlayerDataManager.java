package xyz.coffeeisle.welcomemat;

import org.bukkit.entity.Player;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class PlayerDataManager {
    private final WelcomeMat plugin;
    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager(WelcomeMat plugin) {
        this.plugin = plugin;
        this.playerDataMap = new HashMap<>();
    }

    public PlayerData getPlayerData(Player player) {
        return playerDataMap.computeIfAbsent(player.getUniqueId(), k -> new PlayerData());
    }

    public static class PlayerData {
        private boolean effectsEnabled;

        public PlayerData() {
            this.effectsEnabled = true; // Default to enabled
        }

        public boolean areEffectsEnabled() {
            return effectsEnabled;
        }

        public void setEffectsEnabled(boolean enabled) {
            this.effectsEnabled = enabled;
        }
    }
}