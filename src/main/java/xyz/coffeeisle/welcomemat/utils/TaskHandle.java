package xyz.coffeeisle.welcomemat.utils;

/**
 * Simple abstraction over a scheduled task so we can cancel it without
 * depending on platform-specific scheduler implementations.
 */
@FunctionalInterface
public interface TaskHandle {
    void cancel();
}
