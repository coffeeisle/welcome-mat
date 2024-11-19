package xyz.coffeeisle.welcomemat.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.coffeeisle.welcomemat.WelcomeMat;

import java.util.ArrayList;
import java.util.List;

public class JoinEffectManager {
    private final WelcomeMat plugin;
    private final List<Color> colors;

    public JoinEffectManager(WelcomeMat plugin) {
        this.plugin = plugin;
        this.colors = loadColors();
    }

    private List<Color> loadColors() {
        List<Color> result = new ArrayList<>();
        for (String colorName : plugin.getConfig().getStringList("effects.colors")) {
            try {
                java.awt.Color awtColor = (java.awt.Color) java.awt.Color.class.getField(colorName).get(null);
                result.add(Color.fromRGB(awtColor.getRed(), awtColor.getGreen(), awtColor.getBlue()));
            } catch (Exception e) {
                plugin.getLogger().warning("Invalid color: " + colorName);
            }
        }
        return result;
    }

    public void playEffect(Player player) {
        if (!plugin.getConfig().getBoolean("effects.enabled")) return;
        if (!plugin.getDatabaseManager().getEffectPreference(player.getUniqueId())) return;

        String effectType = plugin.getConfig().getString("effects.type", "SPIRAL");
        int duration = plugin.getConfig().getInt("effects.duration", 3) * 20; // Convert to ticks

        switch (effectType.toUpperCase()) {
            case "SPIRAL":
                playSpiralEffect(player, duration);
                break;
            case "HELIX":
                playHelixEffect(player, duration);
                break;
            case "FOUNTAIN":
                playFountainEffect(player, duration);
                break;
            case "BURST":
                playBurstEffect(player);
                break;
        }
    }

    private void playSpiralEffect(Player player, int duration) {
        new BukkitRunnable() {
            final Location loc = player.getLocation();
            double phi = 0;
            int tick = 0;

            @Override
            public void run() {
                phi += Math.PI/8;
                double x = 0.5 * Math.cos(phi);
                double z = 0.5 * Math.sin(phi);
                double y = phi/5;

                Location particleLoc = loc.clone().add(x, y, z);
                player.getWorld().spawnParticle(
                    Particle.FLAME,
                    particleLoc,
                    1, 0, 0, 0, 0
                );

                tick++;
                if (tick >= duration) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void playHelixEffect(Player player, int duration) {
        new BukkitRunnable() {
            final Location loc = player.getLocation();
            double phi = 0;
            int tick = 0;

            @Override
            public void run() {
                phi += Math.PI/8;
                for (double i = 0; i < Math.PI * 2; i += Math.PI/2) {
                    double x = 0.8 * Math.cos(phi + i);
                    double z = 0.8 * Math.sin(phi + i);
                    double y = phi/4;

                    Location particleLoc = loc.clone().add(x, y, z);
                    player.getWorld().spawnParticle(
                        Particle.SPELL_WITCH,
                        particleLoc,
                        1, 0, 0, 0, 0
                    );
                }

                tick++;
                if (tick >= duration) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 1L);
    }

    private void playFountainEffect(Player player, int duration) {
        new BukkitRunnable() {
            final Location loc = player.getLocation();
            int tick = 0;

            @Override
            public void run() {
                for (int i = 0; i < 8; i++) {
                    double angle = 2 * Math.PI * i / 8;
                    double x = Math.cos(angle);
                    double z = Math.sin(angle);
                    
                    loc.getWorld().spawnParticle(
                        Particle.SPELL_INSTANT,
                        loc.clone().add(0, 1, 0),
                        0, x/2, 1, z/2, 0.15
                    );
                }

                tick++;
                if (tick >= duration) {
                    this.cancel();
                }
            }
        }.runTaskTimer(plugin, 0L, 2L);
    }

    private void playBurstEffect(Player player) {
        Location loc = player.getLocation().add(0, 1, 0);
        for (int i = 0; i < 20; i++) {
            double angle = 2 * Math.PI * i / 20;
            for (int layer = 0; layer < 3; layer++) {
                double y = layer * 0.2;
                double radius = 1 - (layer * 0.2);
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                
                loc.getWorld().spawnParticle(
                    Particle.SPELL_WITCH,
                    loc.clone().add(x, y, z),
                    1, 0, 0, 0, 0
                );
            }
        }
    }
} 