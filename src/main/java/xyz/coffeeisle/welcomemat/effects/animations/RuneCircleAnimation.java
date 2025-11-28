package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

public class RuneCircleAnimation extends Animation {
    public RuneCircleAnimation(WelcomeMat plugin) {
        super(plugin, "rune_circle", "Runic Circle", true, 70);
    }

    @Override
    public void play(Player player) {
        runAnimation(player, new RuneCircleTask(player));
    }

    private class RuneCircleTask extends AnimationTask {
        protected RuneCircleTask(Player player) {
            super(player);
        }

        @Override
        protected void playStep() {
            double radius = 1.8;
            Location center = location.clone().add(0, 0.2, 0);
            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 16) {
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                center.getWorld().spawnParticle(
                    Particle.ENCHANTMENT_TABLE,
                    center.clone().add(x, 0, z),
                    1,
                    0,
                    0,
                    0,
                    0
                );
            }

            double pulse = 0.5 + 0.5 * Math.sin(tick * 0.3);
            center.getWorld().spawnParticle(
                Particle.PORTAL,
                center,
                15,
                pulse,
                0.05,
                pulse,
                0.02
            );
        }
    }
}
