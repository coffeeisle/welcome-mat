package xyz.coffeeisle.welcomemat.player;

import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

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
        return playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(player));
    }
    
    public void savePlayerData(Player player) {
        PlayerData data = playerDataMap.get(player.getUniqueId());
        if (data != null) {
            // TODO: Implement data persistence
        }
    }
    
    public void loadPlayerData(Player player) {
        // TODO: Implement data loading from storage
        playerDataMap.computeIfAbsent(player.getUniqueId(), uuid -> new PlayerData(player));
    }
    
    public void clearPlayerData(Player player) {
        playerDataMap.remove(player.getUniqueId());
    }
    
    public static class PlayerData {
        private final UUID playerUuid;
        private final String playerName;
        private long lastJoinTime;
        private int joinCount;
        private Map<String, Long> messageDelays;
        
        public PlayerData(Player player) {
            this.playerUuid = player.getUniqueId();
            this.playerName = player.getName();
            this.lastJoinTime = System.currentTimeMillis();
            this.joinCount = 1;
            this.messageDelays = new HashMap<>();
        }
        
        public UUID getPlayerUuid() {
            return playerUuid;
        }
        
        public String getPlayerName() {
            return playerName;
        }
        
        public long getLastJoinTime() {
            return lastJoinTime;
        }
        
        public void updateJoinTime() {
            this.lastJoinTime = System.currentTimeMillis();
            this.joinCount++;
        }
        
        public int getJoinCount() {
            return joinCount;
        }
        
        public void setMessageDelay(String messageType, long delay) {
            messageDelays.put(messageType, System.currentTimeMillis() + delay);
        }
        
        public boolean canShowMessage(String messageType) {
            Long delayUntil = messageDelays.get(messageType);
            return delayUntil == null || System.currentTimeMillis() >= delayUntil;
        }
    }
}