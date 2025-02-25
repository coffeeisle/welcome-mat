package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

public class GreenSpikeFireworkAnimation extends Animation {
    public GreenSpikeFireworkAnimation(WelcomeMat plugin) {
        super(plugin, "green_spike", "Green Spike Firework", true, 40);
    }

    @Override
    public void play(Player player) {
        runAnimation(player, new GreenSpikeTask(player));
    }

    private class GreenSpikeTask extends AnimationTask {
        private double height = 0;

        public GreenSpikeTask(Player player) {
            super(player);
        }

        @Override
        protected void playStep() {
            height = Math.min(2.0, tick * 0.1);
            double radius = Math.max(0, 1.0 - height/2);
            
            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI/8) {
                double x = radius * Math.cos(angle);
                double z = radius * Math.sin(angle);
                
                Location particleLoc = location.clone().add(x, height, z);
                Particle.DustOptions dustOptions = new Particle.DustOptions(Color.LIME, 1.0F);
                
                location.getWorld().spawnParticle(
                    Particle.DUST,
                    particleLoc,
                    1, 0, 0, 0, 0,
                    dustOptions
                );
            }
        }
    }
}