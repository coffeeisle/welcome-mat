package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

public class CopperGolemAnimation extends Animation {
    private static final Particle.DustOptions COPPER_DUST =
        new Particle.DustOptions(Color.fromRGB(198, 120, 66), 1.4F);

    public CopperGolemAnimation(WelcomeMat plugin) {
        super(plugin, "copper_golem", "Copper Golem Ward", true, 80);
    }

    @Override
    public void play(Player player) {
        runAnimation(player, new CopperGolemTask(player));
    }

    private class CopperGolemTask extends AnimationTask {
        protected CopperGolemTask(Player player) {
            super(player);
        }

        @Override
        protected void playStep() {
            if (tick <= 40) {
                drawCopperFrame();
            } else {
                launchCopperBurst();
            }
        }

        private void drawCopperFrame() {
            double size = 1.6;
            Location base = location.clone();
            for (double y = 0; y <= 2.4; y += 0.4) {
                for (int side = 0; side < 4; side++) {
                    double angle = Math.PI / 2 * side;
                    double x = size * Math.cos(angle);
                    double z = size * Math.sin(angle);
                    base.getWorld().spawnParticle(
                        Particle.REDSTONE,
                        base.clone().add(x, y, z),
                        1, 0.02, 0.02, 0.02, 0,
                        COPPER_DUST
                    );
                }
            }
        }

        private void launchCopperBurst() {
            double progress = Math.min(1.0, (tick - 40) / 30.0);
            double radius = 1 + (progress * 4);
            Location center = location.clone().add(0, 1.2, 0);
            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                center.getWorld().spawnParticle(
                    Particle.FIREWORKS_SPARK,
                    center.clone().add(x, 0.4 * progress, z),
                    2, 0.05, 0.05, 0.05, 0.01
                );
            }
        }
    }
}
