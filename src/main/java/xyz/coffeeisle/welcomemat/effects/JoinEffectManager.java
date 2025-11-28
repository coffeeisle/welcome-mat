package xyz.coffeeisle.welcomemat.effects;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;
import xyz.coffeeisle.welcomemat.utils.TaskHandle;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

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
        Location base = player.getLocation().clone();
        AtomicInteger tick = new AtomicInteger();
        AtomicReference<TaskHandle> handleRef = new AtomicReference<>();

        Runnable task = () -> {
            double phi = tick.get() * Math.PI / 8;
            double x = 0.5 * Math.cos(phi);
            double z = 0.5 * Math.sin(phi);
            double y = phi / 5;

            Location particleLoc = base.clone().add(x, y, z);
            player.getWorld().spawnParticle(Particle.FLAME, particleLoc, 1, 0, 0, 0, 0);

            if (tick.incrementAndGet() >= duration || !player.isOnline()) {
                cancelHandle(handleRef.getAndSet(null));
            }
        };

        handleRef.set(plugin.getSchedulerAdapter().runEntityRepeating(player, 0L, 1L, task));
    }

    private void playHelixEffect(Player player, int duration) {
        Location base = player.getLocation().clone();
        AtomicInteger tick = new AtomicInteger();
        AtomicReference<TaskHandle> handleRef = new AtomicReference<>();

        Runnable task = () -> {
            double phi = tick.get() * Math.PI / 8;
            for (double i = 0; i < Math.PI * 2; i += Math.PI / 2) {
                double x = 0.8 * Math.cos(phi + i);
                double z = 0.8 * Math.sin(phi + i);
                double y = phi / 4;

                Location particleLoc = base.clone().add(x, y, z);
                player.getWorld().spawnParticle(Particle.DRAGON_BREATH, particleLoc, 1, 0, 0, 0, 0);
            }

            if (tick.incrementAndGet() >= duration || !player.isOnline()) {
                cancelHandle(handleRef.getAndSet(null));
            }
        };

        handleRef.set(plugin.getSchedulerAdapter().runEntityRepeating(player, 0L, 1L, task));
    }

    private void playFountainEffect(Player player, int duration) {
        Location base = player.getLocation().clone();
        AtomicInteger tick = new AtomicInteger();
        AtomicReference<TaskHandle> handleRef = new AtomicReference<>();

        Runnable task = () -> {
            for (int i = 0; i < 8; i++) {
                double angle = 2 * Math.PI * i / 8;
                double x = Math.cos(angle);
                double z = Math.sin(angle);

                base.getWorld().spawnParticle(
                    Particle.CRIT,
                    base.clone().add(0, 1, 0),
                    0, x / 2, 1, z / 2, 0.15
                );
            }

            if (tick.incrementAndGet() >= duration || !player.isOnline()) {
                cancelHandle(handleRef.getAndSet(null));
            }
        };

        handleRef.set(plugin.getSchedulerAdapter().runEntityRepeating(player, 0L, 2L, task));
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
                    Particle.DRAGON_BREATH,
                    loc.clone().add(x, y, z),
                    1, 0, 0, 0, 0
                );
            }
        }
    }

    private void cancelHandle(TaskHandle handle) {
        if (handle != null) {
            handle.cancel();
        }
    }
}