package xyz.coffeeisle.welcomemat.utils;

import java.lang.reflect.Method;
import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
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
    private enum FoliaSchedulerVariant {
        LEGACY_TICKS,
        TICKS_WITH_UNIT,
        DURATION
    }

    private final Plugin plugin;
    private final boolean foliaAvailable;

    private Method entityGetScheduler;
    private Method schedulerRunAtFixedRate;
    private Method scheduledTaskCancel;
    private FoliaSchedulerVariant foliaVariant;

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
        List<String> discoveredSignatures = new ArrayList<>();

        try {
            Class<?> entityClass = Class.forName("org.bukkit.entity.Entity");
            entityGetScheduler = entityClass.getMethod("getScheduler");

            Class<?> entitySchedulerClass = Class.forName("io.papermc.paper.threadedregions.scheduler.EntityScheduler");
            for (Method method : entitySchedulerClass.getMethods()) {
                if (!"runAtFixedRate".equals(method.getName())) {
                    continue;
                }

                discoveredSignatures.add(describeMethod(method));

                Class<?>[] params = method.getParameterTypes();
                if (params.length == 4
                        && Plugin.class.isAssignableFrom(params[0])
                        && Consumer.class.isAssignableFrom(params[1])
                        && params[2] == long.class
                        && params[3] == long.class) {
                    schedulerRunAtFixedRate = method;
                    foliaVariant = FoliaSchedulerVariant.LEGACY_TICKS;
                    break;
                }

                if (params.length == 5
                        && Plugin.class.isAssignableFrom(params[0])
                        && Consumer.class.isAssignableFrom(params[1])
                        && params[2] == long.class
                        && params[3] == long.class
                        && TimeUnit.class.isAssignableFrom(params[4])) {
                    schedulerRunAtFixedRate = method;
                    foliaVariant = FoliaSchedulerVariant.TICKS_WITH_UNIT;
                    break;
                }

                if (params.length == 4
                        && Plugin.class.isAssignableFrom(params[0])
                        && Consumer.class.isAssignableFrom(params[1])
                        && Duration.class.isAssignableFrom(params[2])
                        && Duration.class.isAssignableFrom(params[3])) {
                    schedulerRunAtFixedRate = method;
                    foliaVariant = FoliaSchedulerVariant.DURATION;
                    break;
                }
            }

            if (schedulerRunAtFixedRate == null) {
                plugin.getLogger().severe("Could not find a compatible Folia runAtFixedRate signature");
                if (discoveredSignatures.isEmpty()) {
                    plugin.getLogger().severe("EntityScheduler had no runAtFixedRate methods to inspect.");
                } else {
                    plugin.getLogger().severe("Discovered Folia signatures:");
                    for (String sig : discoveredSignatures) {
                        plugin.getLogger().severe(" - " + sig);
                    }
                }
            }

            Class<?> scheduledTaskClass = Class.forName("io.papermc.paper.threadedregions.scheduler.ScheduledTask");
            scheduledTaskCancel = scheduledTaskClass.getMethod("cancel");
        } catch (ReflectiveOperationException ex) {
            plugin.getLogger().log(Level.SEVERE, "Failed to prepare Folia scheduler reflection", ex);
        }
    }

    private String describeMethod(Method method) {
        StringBuilder builder = new StringBuilder(method.getName()).append('(');
        Class<?>[] parameters = method.getParameterTypes();
        for (int i = 0; i < parameters.length; i++) {
            builder.append(parameters[i].getName());
            if (i + 1 < parameters.length) {
                builder.append(", ");
            }
        }
        builder.append(')');
        return builder.toString();
    }

    private TaskHandle tryRunFoliaRepeating(Player player, long delayTicks, long periodTicks, Runnable runnable) {
        if (entityGetScheduler == null || schedulerRunAtFixedRate == null || scheduledTaskCancel == null || foliaVariant == null) {
            return null;
        }

        try {
            Object scheduler = entityGetScheduler.invoke(player);
            Consumer<Object> consumer = scheduledTask -> runnable.run();
            Object scheduledTask;

            switch (foliaVariant) {
                case LEGACY_TICKS:
                    scheduledTask = schedulerRunAtFixedRate.invoke(scheduler, plugin, consumer, delayTicks, periodTicks);
                    break;
                case TICKS_WITH_UNIT:
                    long delayMillis = ticksToMillis(delayTicks);
                    long periodMillis = ticksToMillis(periodTicks);
                    scheduledTask = schedulerRunAtFixedRate.invoke(
                        scheduler,
                        plugin,
                        consumer,
                        delayMillis,
                        periodMillis,
                        TimeUnit.MILLISECONDS
                    );
                    break;
                case DURATION:
                    Duration delayDuration = ticksToDuration(delayTicks);
                    Duration periodDuration = ticksToDuration(periodTicks);
                    scheduledTask = schedulerRunAtFixedRate.invoke(
                        scheduler,
                        plugin,
                        consumer,
                        delayDuration,
                        periodDuration
                    );
                    break;
                default:
                    return null;
            }

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

    private long ticksToMillis(long ticks) {
        return Math.max(0L, ticks) * 50L;
    }

    private Duration ticksToDuration(long ticks) {
        if (ticks <= 0L) {
            return Duration.ZERO;
        }
        return Duration.ofMillis(ticksToMillis(ticks));
    }
}
