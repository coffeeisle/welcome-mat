package xyz.coffeeisle.welcomemat.utils;

import java.lang.reflect.Method;
import java.util.function.Consumer;
import java.util.logging.Level;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

/**
 * Platform-aware scheduler helper that keeps repeating tasks running on the
 * correct thread context for both legacy Bukkit servers and modern Folia
 * builds.
 */
public class SchedulerAdapter {
    private final Plugin plugin;
    private final boolean foliaAvailable;

    private Method entityGetScheduler;
    private Method schedulerRunAtFixedRate;
    private Method scheduledTaskCancel;

    public SchedulerAdapter(Plugin plugin) {
        this.plugin = plugin;
        this.foliaAvailable = detectFolia();
        if (foliaAvailable) {
            prepareFoliaReflection();
        }
    }

    public boolean isFolia() {
        return foliaAvailable;
    }

    public TaskHandle runEntityRepeating(Player player, long delayTicks, long periodTicks, Runnable runnable) {
        if (foliaAvailable) {
            TaskHandle handle = tryRunFoliaRepeating(player, delayTicks, periodTicks, runnable);
            if (handle != null) {
                return handle;
            }
        }

        BukkitTask task = new BukkitRunnable() {
            @Override
            public void run() {
                runnable.run();
            }
        }.runTaskTimer(plugin, delayTicks, periodTicks);

        return task::cancel;
    }

    private boolean detectFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException ex) {
            return false;
        }
    }

    private void prepareFoliaReflection() {
        try {
            Class<?> entityClass = Class.forName("org.bukkit.entity.Entity");
            entityGetScheduler = entityClass.getMethod("getScheduler");

            Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            schedulerRunAtFixedRate = entitySchedulerClass.getMethod(
                "runAtFixedRate",
                Plugin.class,
                Consumer.class,
                long.class,
                long.class
            );

            Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            scheduledTaskCancel = scheduledTaskClass.getMethod("cancel");
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to prepare Folia scheduler reflection", ex);
        }
    }

    private TaskHandle tryRunFoliaRepeating(Player player, long delayTicks, long periodTicks, Runnable runnable) {
        if (entityGetScheduler == null || schedulerRunAtFixedRate == null || scheduledTaskCancel == null) {
            return null;
        }

        try {
            Object scheduler = entityGetScheduler.invoke(player);
            Consumer<Object> consumer = scheduledTask -> runnable.run();
            Object scheduledTask = schedulerRunAtFixedRate.invoke(scheduler, plugin, consumer, delayTicks, periodTicks);

            return () -> {
                try {
                    scheduledTaskCancel.invoke(scheduledTask);
                } catch (Exception ex) {
                    plugin.getLogger().log(Level.WARNING, "Failed to cancel Folia task", ex);
                }
            };
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Folia scheduling failed, falling back to Bukkit scheduler", ex);
            return null;
        }
    }
}
