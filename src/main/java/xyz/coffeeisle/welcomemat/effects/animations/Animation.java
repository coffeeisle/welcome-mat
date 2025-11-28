package xyz.coffeeisle.welcomemat.effects.animations;

import java.util.concurrent.atomic.AtomicReference;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;
import xyz.coffeeisle.welcomemat.utils.TaskHandle;

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
        AtomicReference<TaskHandle> handleRef = new AtomicReference<>();
        Runnable tickTask = () -> {
            if (!player.isOnline() || !task.tick()) {
                cancelHandle(handleRef.getAndSet(null));
            }
        };

        handleRef.set(plugin.getSchedulerAdapter().runEntityRepeating(player, 0L, 1L, tickTask));
    }

    private void cancelHandle(TaskHandle handle) {
        if (handle != null) {
            handle.cancel();
        }
    }

    protected abstract class AnimationTask {
        protected final Location location;
        protected int tick;

        protected AnimationTask(Player player) {
            this.location = player.getLocation().clone();
            this.tick = 0;
        }

        public boolean tick() {
            if (tick >= duration) {
                return false;
            }
            tick++;
            playStep();
            return true;
        }

        protected abstract void playStep();
    }
}