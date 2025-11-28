package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

public class HeartBloomAnimation extends Animation {
    public HeartBloomAnimation(WelcomeMat plugin) {
        super(plugin, "heart_bloom", "Heart Bloom", true, 60);
    }

    @Override
    public void play(Player player) {
        runAnimation(player, new HeartBloomTask(player));
    }

    private class HeartBloomTask extends AnimationTask {
        protected HeartBloomTask(Player player) {
            super(player);
        }

        @Override
        protected void playStep() {
            double swirl = tick * 0.35;
            double radius = 0.6 + (tick * 0.01);
            Location center = location.clone().add(0, 1, 0);

            for (int i = 0; i < 4; i++) {
                double angle = swirl + (Math.PI / 2) * i;
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                center.getWorld().spawnParticle(
                    Particle.HEART,
                    center.clone().add(x, (tick % 10) * 0.05, z),
                    1,
                    0,
                    0,
                    0,
                    0
                );
            }

            center.getWorld().spawnParticle(
                Particle.VILLAGER_HAPPY,
                center,
                3,
                0.6,
                0.4,
                0.6,
                0.05
            );
        }
    }
}
