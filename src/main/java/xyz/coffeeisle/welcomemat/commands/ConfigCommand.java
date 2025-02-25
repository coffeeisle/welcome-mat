package xyz.coffeeisle.welcomemat.commands;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import xyz.coffeeisle.welcomemat.WelcomeMat;
import xyz.coffeeisle.welcomemat.effects.animations.Animation;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ConfigCommand implements CommandExecutor, TabCompleter {
    private final WelcomeMat plugin;

    public ConfigCommand(WelcomeMat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("welcomemat.config")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
            return true;
        }

        if (args.length < 2) {
            sendUsage(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();
        String category = args[1].toLowerCase();

        switch (subCommand) {
            case "effects":
                handleEffectsCommand(sender, args);
                break;
            case "sounds":
                handleSoundsCommand(sender, args);
                break;
            default:
                sendUsage(sender);
                break;
        }

        return true;
    }

    private void handleEffectsCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.YELLOW + "Available effects: " + 
                String.join(", ", plugin.getAnimationRegistry().getAvailableAnimations()));
            return;
        }

        String action = args[2].toLowerCase();
        if (action.equals("set") && args.length == 4) {
            String effectName = args[3].toLowerCase();
            if (!plugin.getAnimationRegistry().hasAnimation(effectName)) {
                sender.sendMessage(ChatColor.RED + "Unknown effect: " + effectName);
                return;
            }

            Animation animation = plugin.getAnimationRegistry().getAnimation(effectName);
            plugin.getConfig().set("effects.type", effectName);
            plugin.saveConfig();
            sender.sendMessage(ChatColor.GREEN + "Set join effect to: " + animation.getDisplayName());
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /wm config effects set <effect_name>");
        }
    }

    private void handleSoundsCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(ChatColor.YELLOW + "Usage: /wm config sounds set <sound_name>");
            return;
        }

        // TODO: Implement predefined sounds configuration
    }

    private void sendUsage(CommandSender sender) {
        sender.sendMessage(ChatColor.YELLOW + "=== WelcomeMat Config Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/wm config effects set <effect_name> - Set join effect");
        sender.sendMessage(ChatColor.YELLOW + "/wm config sounds set <sound_name> - Set join sound");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!sender.hasPermission("welcomemat.config")) {
            return new ArrayList<>();
        }

        if (args.length == 1) {
            return Arrays.asList("effects", "sounds").stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("effects")) {
            return Arrays.asList("set").stream()
                    .filter(s -> s.startsWith(args[1].toLowerCase()))
                    .collect(Collectors.toList());
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("effects") && args[1].equalsIgnoreCase("set")) {
            return plugin.getAnimationRegistry().getAvailableAnimations().stream()
                    .filter(s -> s.startsWith(args[2].toLowerCase()))
                    .collect(Collectors.toList());
        }

        return new ArrayList<>();
    }
}