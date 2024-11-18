package xyz.coffeeisle.welcomemat;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class WelcomeMatCommand implements CommandExecutor {
    private final WelcomeMat plugin;

    public WelcomeMatCommand(WelcomeMat plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        if (args[0].equalsIgnoreCase("reload")) {
            if (!sender.hasPermission("welcomemat.reload")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to use this command!");
                return true;
            }

            plugin.getConfigManager().loadConfig();
            sender.sendMessage(ChatColor.GREEN + "WelcomeMat configuration reloaded!");
            return true;
        }

        if (args[0].equalsIgnoreCase("sound") || args[0].equalsIgnoreCase("sounds")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                return true;
            }

            if (!sender.hasPermission("welcomemat.sound.toggle")) {
                sender.sendMessage(ChatColor.RED + "You don't have permission to toggle sounds!");
                return true;
            }

            Player player = (Player) sender;
            String uuid = player.getUniqueId().toString();
            ConfigManager config = plugin.getConfigManager();
            boolean currentPreference = config.getSoundPreference(uuid);
            config.setSoundPreference(uuid, !currentPreference);

            if (!currentPreference) {
                sender.sendMessage(ChatColor.GREEN + "Join/Leave sounds enabled!");
                // Play test sound
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            } else {
                sender.sendMessage(ChatColor.YELLOW + "Join/Leave sounds disabled!");
            }
            return true;
        }

        sendHelp(sender);
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "=== WelcomeMat Help ===");
        sender.sendMessage(ChatColor.YELLOW + "/welcomemat reload " + ChatColor.WHITE + "- Reload the configuration");
        if (sender.hasPermission("welcomemat.sound.toggle")) {
            sender.sendMessage(ChatColor.YELLOW + "/welcomemat sound " + ChatColor.WHITE + "- Toggle join/leave sounds");
        }
        sender.sendMessage(ChatColor.YELLOW + "/welcomemat help " + ChatColor.WHITE + "- Show this help message");
    }
} 