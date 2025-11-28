package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

import java.util.concurrent.ThreadLocalRandom;

public class DataStreamAnimation extends Animation {
    private static final Particle.DustOptions NEON_BLUE = new Particle.DustOptions(Color.fromRGB(65, 233, 255), 1.25F);
    private static final Particle.DustOptions CODE_GREEN = new Particle.DustOptions(Color.fromRGB(146, 255, 186), 1.1F);

    public DataStreamAnimation(WelcomeMat plugin) {
        super(plugin, "data_stream", "Data Stream", true, 80);
    }

    @Override
    public void play(Player player) {
        runAnimation(player, new DataStreamTask(player));
    }

    private class DataStreamTask extends AnimationTask {
        protected DataStreamTask(Player player) {
            super(player);
        }

        @Override
        protected void playStep() {
            double rising = (tick * 0.06);
            Location base = location.clone().add(0, rising, 0);

            base.getWorld().spawnParticle(Particle.DUST, base, 4, 0.15, 0.1, 0.15, 0, NEON_BLUE);
            base.getWorld().spawnParticle(Particle.DUST, base, 2, 0.1, 0.05, 0.1, 0, CODE_GREEN);

            if (tick % 4 == 0) {
                drawBinaryRing(base.clone().add(0, 0.3, 0));
            }

            if (tick % 7 == 0) {
                base.getWorld().spawnParticle(
                    Particle.END_ROD,
                    base.clone().add(randomOffset(), 0.4, randomOffset()),
                    3,
                    0.02,
                    0.02,
                    0.02,
                    0.003
                );
            }
        }

        private void drawBinaryRing(Location center) {
            double ringRadius = 0.8 + (tick * 0.01);
            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 8) {
                double x = Math.cos(angle) * ringRadius;
                double z = Math.sin(angle) * ringRadius;
                double yVariation = Math.sin(tick * 0.2 + angle) * 0.2;
                center.getWorld().spawnParticle(
                    Particle.COMPOSTER,
                    center.clone().add(x, yVariation, z),
                    0,
                    0,
                    0,
                    0,
                    0
                );
            }
        }

        private double randomOffset() {
            return ThreadLocalRandom.current().nextDouble(-0.6, 0.6);
        }
    }
}
