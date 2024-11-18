package xyz.coffeeisle.welcomemat;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.Sound;

public class PlayerEventListener implements Listener {
    private final WelcomeMat plugin;

    public PlayerEventListener(WelcomeMat plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (player.hasPermission("welcomemat.bypass")) {
            return;
        }

        ConfigManager config = plugin.getConfigManager();

        if (config.isJoinMessageEnabled()) {
            event.setJoinMessage(config.getJoinMessage(player.getName()));
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

        if (config.isJoinSoundEnabled() && config.isOtherPlayersSoundsEnabled()) {
            Sound sound = config.getJoinSound();
            if (sound != null) {
                player.getWorld().getPlayers().forEach(p -> {
                    if (!p.equals(player) && config.getSoundPreference(p.getUniqueId().toString())) {
                        p.playSound(
                            player.getLocation(),
                            sound,
                            config.getJoinSoundVolume(),
                            config.getJoinSoundPitch()
                        );
                    }
                });
            }
        }
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

        if (config.isLeaveSoundEnabled() && config.isOtherPlayersSoundsEnabled()) {
            Sound sound = config.getLeaveSound();
            if (sound != null) {
                player.getWorld().getPlayers().forEach(p -> {
                    if (!p.equals(player) && config.getSoundPreference(p.getUniqueId().toString())) {
                        p.playSound(
                            player.getLocation(),
                            sound,
                            config.getLeaveSoundVolume(),
                            config.getLeaveSoundPitch()
                        );
                    }
                });
            }
        }
    }
} 