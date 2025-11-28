package xyz.coffeeisle.welcomemat.effects.animations;

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
                origin.getWorld().spawnParticle(Particle.FIREWORKS_SPARK, origin, 0, vx, vy, vz, 0.02);
            }
        }

        private void sprinkleSparkles() {
            Location center = location.clone().add(0, 1.4, 0);
            center.getWorld().spawnParticle(Particle.NOTE, center, 3, 0.6, 0.4, 0.6, 0.3);
            center.getWorld().spawnParticle(Particle.SPELL_MOB, center, 15, 0.8, 0.5, 0.8, 0.01);
        }

        private double randomVelocity() {
            return ThreadLocalRandom.current().nextDouble(-0.4, 0.4);
        }
    }
}
