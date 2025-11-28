package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

import java.util.concurrent.ThreadLocalRandom;

public class ConfettiBurstAnimation extends Animation {
    public ConfettiBurstAnimation(WelcomeMat plugin) {
        super(plugin, "confetti_burst", "Confetti Burst", true, 60);
    }

    @Override
    public void play(Player player) {
        runAnimation(player, new ConfettiBurstTask(player));
    }

    private class ConfettiBurstTask extends AnimationTask {
        private final Color[] palette = new Color[] {
            Color.fromRGB(243, 65, 65),
            Color.fromRGB(255, 147, 61),
            Color.fromRGB(255, 215, 64),
            Color.fromRGB(90, 214, 120),
            Color.fromRGB(65, 198, 255),
            Color.fromRGB(186, 110, 255)
        };

        protected ConfettiBurstTask(Player player) {
            super(player);
        }

        @Override
        protected void playStep() {
            if (tick == 1) {
                location.getWorld().playSound(location, Sound.ENTITY_FIREWORK_ROCKET_BLAST, 0.8F, 1.2F);
            }

            launchColorSpray();
            sprinkleSparkles();
        }

        private void launchColorSpray() {
            Location origin = location.clone().add(0, 1.2, 0);
            for (int i = 0; i < 3; i++) {
                double vx = randomVelocity();
                double vy = 0.6 + ThreadLocalRandom.current().nextDouble(0.2);
                double vz = randomVelocity();
                Color primary = randomColor();
                Color fade = randomColor();
                Particle.DustTransition dust = new Particle.DustTransition(primary, fade, 1.2F);
                origin.getWorld().spawnParticle(
                    Particle.DUST_COLOR_TRANSITION,
                    origin,
                    0,
                    vx,
                    vy,
                    vz,
                    0,
                    dust
                );
            }
        }

        private void sprinkleSparkles() {
            Location center = location.clone().add(0, 1.4, 0);
            Color dustColor = randomColor();
            Particle.DustOptions sparkle = new Particle.DustOptions(dustColor, 0.8F);
            center.getWorld().spawnParticle(Particle.DUST, center, 5, 0.4, 0.2, 0.4, 0, sparkle);
            center.getWorld().spawnParticle(Particle.ENTITY_EFFECT, center, 12, 0.8, 0.5, 0.8, 0.01);
        }

        private double randomVelocity() {
            return ThreadLocalRandom.current().nextDouble(-0.4, 0.4);
        }

        private Color randomColor() {
            return palette[ThreadLocalRandom.current().nextInt(palette.length)];
        }
    }
}
