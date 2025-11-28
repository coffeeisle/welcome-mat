package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

public class MoonRiseAnimation extends Animation {
    public MoonRiseAnimation(WelcomeMat plugin) {
        super(plugin, "moonrise", "Moonrise Halo", true, 80);
    }

    @Override
    public void play(Player player) {
        runAnimation(player, new MoonTask(player));
    }

    private class MoonTask extends AnimationTask {
        protected MoonTask(Player player) {
            super(player);
        }

        @Override
        protected void playStep() {
            double progress = tick / (double) duration;
            double height = 5 * progress;
            double radius = 1.3 + (0.2 * Math.sin(progress * Math.PI));
            Location center = location.clone().add(0, height, 0);

            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 20) {
                double x = Math.cos(angle) * radius;
                double z = Math.sin(angle) * radius;
                center.getWorld().spawnParticle(
                    Particle.END_ROD,
                    center.clone().add(x, 0, z),
                    0,
                    0,
                    0.02,
                    0,
                    0
                );
            }

            if (tick > duration * 0.6) {
                double fade = 1 - ((tick - (duration * 0.6)) / (duration * 0.4));
                center.getWorld().spawnParticle(
                    Particle.SNOWFLAKE,
                    center,
                    2,
                    0.4 * fade,
                    0.2 * fade,
                    0.4 * fade,
                    0.01
                );
            }
        }
    }
}
