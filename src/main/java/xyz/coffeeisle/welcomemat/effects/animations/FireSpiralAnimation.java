package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

public class FireSpiralAnimation extends Animation {
    public FireSpiralAnimation(WelcomeMat plugin) {
        super(plugin, "fire_spiral", "Fire Spiral", true, 60);
    }

    @Override
    public void play(Player player) {
        runAnimation(player, new FireSpiralTask(player));
    }

    private class FireSpiralTask extends AnimationTask {
        private double phi = 0;

        public FireSpiralTask(Player player) {
            super(player);
        }

        @Override
        protected void playStep() {
            phi += Math.PI/8;
            double x = 0.5 * Math.cos(phi);
            double z = 0.5 * Math.sin(phi);
            double y = phi/5;

            Location particleLoc = location.clone().add(x, y, z);
            location.getWorld().spawnParticle(
                Particle.FLAME,
                particleLoc,
                1, 0, 0, 0, 0
            );
        }
    }
}