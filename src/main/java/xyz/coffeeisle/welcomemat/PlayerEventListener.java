package xyz.coffeeisle.welcomemat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Sound;
import java.util.HashMap;
import java.util.Map;

public class PlayerEventListener implements Listener {
    private final WelcomeMat plugin;
    private final PlayerDataManager playerDataManager;

    public PlayerEventListener(WelcomeMat plugin) {
        this.plugin = plugin;
        this.playerDataManager = new PlayerDataManager(plugin);
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (player.hasPermission("welcomemat.bypass")) {
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        PlayerDataManager.PlayerData playerData = playerDataManager.getPlayerData(player);
        boolean isFirstJoin = !player.hasPlayedBefore();
        
        if (config.isJoinMessageEnabled()) {
            event.setJoinMessage(config.getJoinMessage(player.getName(), isFirstJoin));
        }

        if (config.isJoinTitleEnabled()) {
            try {
                player.sendTitle(
                    config.getJoinTitle(),
                    config.getJoinSubtitle(),
                    20, 60, 20
                );
            } catch (NoSuchMethodError e) {
                player.sendTitle(
                    config.getJoinTitle(),
                    config.getJoinSubtitle()
                );
            }
        }

        if (config.isJoinSoundEnabled()) {
            Sound sound = config.getJoinSound();
            if (sound != null) {
                float volume = config.getJoinSoundVolume();
                float pitch = config.getJoinSoundPitch();
                
                plugin.getLogger().info("Playing join sound: " + sound.name() + 
                    " (Volume: " + volume + ", Pitch: " + pitch + ")");

                try {
                    player.playSound(player.getLocation(), sound, volume, pitch);
                } catch (Exception e) {
                    plugin.getLogger().warning("Failed to play join sound for joining player: " + e.getMessage());
                }

                if (config.isOtherPlayersSoundsEnabled()) {
                    for (Player p : player.getWorld().getPlayers()) {
                        if (!p.equals(player) && plugin.getDatabaseManager().getSoundPreference(p.getUniqueId())) {
                            try {
                                p.playSound(player.getLocation(), sound, volume, pitch);
                                plugin.getLogger().info("Played join sound for: " + p.getName());
                            } catch (Exception e) {
                                plugin.getLogger().warning("Failed to play join sound for " + p.getName() + ": " + e.getMessage());
                            }
                        }
                    }
                }
            }
        }

        // Play join effect if enabled
        plugin.getJoinEffectManager().playEffect(player);
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (player.hasPermission("welcomemat.bypass")) {
            return;
        }

        ConfigManager config = plugin.getConfigManager();

        if (config.isLeaveMessageEnabled()) {
            event.setQuitMessage(config.getLeaveMessage(player.getName()));
        }

        if (config.isLeaveSoundEnabled()) {
            Sound sound = config.getLeaveSound();
            if (sound != null) {
                for (Player p : player.getWorld().getPlayers()) {
                    if (!p.equals(player) && plugin.getDatabaseManager().getSoundPreference(p.getUniqueId())) {
                        try {
                            p.playSound(
                                player.getLocation(),
                                sound,
                                config.getLeaveSoundVolume(),
                                config.getLeaveSoundPitch()
                            );
                        } catch (IllegalArgumentException e) {
                            plugin.getLogger().warning("Failed to play leave sound: " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
}