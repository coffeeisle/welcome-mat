package xyz.coffeeisle.welcomemat.effects.animations;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import xyz.coffeeisle.welcomemat.WelcomeMat;

public abstract class Animation {
    protected final WelcomeMat plugin;
    protected final String name;
    protected final String displayName;
    protected final boolean isRandom;
    protected final int duration;

    public Animation(WelcomeMat plugin, String name, String displayName, boolean isRandom, int duration) {
        this.plugin = plugin;
        this.name = name;
        this.displayName = displayName;
        this.isRandom = isRandom;
        this.duration = duration;
    }

    public abstract void play(Player player);

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isRandom() {
        return isRandom;
    }

    public int getDuration() {
        return duration;
    }

    protected void runAnimation(Player player, AnimationTask task) {
        task.runTaskTimer(plugin, 0L, 1L);
    }

    protected abstract class AnimationTask extends BukkitRunnable {
        protected final Location location;
        protected int tick;

        protected AnimationTask(Player player) {
            this.location = player.getLocation();
            this.tick = 0;
        }

        @Override
        public void run() {
            if (tick >= duration) {
                this.cancel();
                return;
            }
            tick++;
            playStep();
        }

        protected abstract void playStep();
    }
}