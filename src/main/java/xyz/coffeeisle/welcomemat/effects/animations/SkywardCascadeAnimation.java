package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;

public class SkywardCascadeAnimation extends Animation {
    public SkywardCascadeAnimation(WelcomeMat plugin) {
        super(plugin, "skyward_cascade", "Skyward Cascade", true, 70);
    }

    @Override
    public void play(Player player) {
        runAnimation(player, new SkywardCascadeTask(player));
    }

    private class SkywardCascadeTask extends AnimationTask {
        protected SkywardCascadeTask(Player player) {
            super(player);
        }

        @Override
        protected void playStep() {
            double height = (tick * 0.08) + 0.5;
            Location columnBase = location.clone().add(0, height, 0);

            columnBase.getWorld().spawnParticle(
                Particle.DRAGON_BREATH,
                columnBase,
                6,
                0.2,
                0.3,
                0.2,
                0.02
            );

            double ringRadius = 0.8 + (tick * 0.01);
            for (double angle = 0; angle < Math.PI * 2; angle += Math.PI / 6) {
                double x = Math.cos(angle) * ringRadius;
                double z = Math.sin(angle) * ringRadius;
                columnBase.getWorld().spawnParticle(
                    Particle.CLOUD,
                    columnBase.clone().add(x, 0, z),
                    0,
                    0,
                    0,
                    0,
                    0
                );
            }
        }
    }
}
