package xyz.coffeeisle.welcomemat.commands;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.command.*;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;
import xyz.coffeeisle.welcomemat.WelcomeMat;
import xyz.coffeeisle.welcomemat.ConfigManager;
import xyz.coffeeisle.welcomemat.database.DatabaseManager;
import xyz.coffeeisle.welcomemat.LanguageManager;
import xyz.coffeeisle.welcomemat.gui.SettingsGUI;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.UUID;
import java.util.stream.Collectors;

public class WelcomeMatCommand implements CommandExecutor, TabCompleter {
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

        switch (args[0].toLowerCase()) {
            case "reload":
                handleReload(sender);
                break;
            case "sound":
                handleSound(sender);
                break;
            case "pack":
                handlePack(sender, args);
                break;
            case "config":
                handleConfig(sender, args);
                break;
            case "help":
                sendHelp(sender);
                break;
            case "language":
                handleLanguage(sender, args);
                break;
            case "gui":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
                    return true;
                }
                if (!sender.hasPermission("welcomemat.config")) {
                    sender.sendMessage(ChatColor.RED + "You don't have permission to access the settings menu!");
                    return true;
                }
                new SettingsGUI(plugin).openMainMenu((Player) sender);
                break;
            case "effects":
                handleEffects(sender);
                break;
            default:
                sender.sendMessage(ChatColor.RED + "Unknown command. Use /wm help for help.");
        }
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("welcomemat.reload")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("reload.no_permission"));
            return;
        }
        
        plugin.getConfigManager().loadConfig();
        plugin.getLanguageManager().loadMessages();
        sender.sendMessage(plugin.getLanguageManager().getMessage("reload.success"));
    }

    private void handleSound(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return;
        }

        if (!sender.hasPermission("welcomemat.sound.toggle")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to toggle sounds!");
            return;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        DatabaseManager db = plugin.getDatabaseManager();
        
        boolean currentState = db.getSoundPreference(uuid);
        boolean newState = !currentState;
        
        db.setSoundPreference(uuid, newState);

        if (newState) {
            sender.sendMessage(ChatColor.GREEN + "Join/Leave sounds enabled!");
            try {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            } catch (IllegalArgumentException e) {
                // Fallback for older versions
                try {
                    player.playSound(player.getLocation(), Sound.valueOf("NOTE_PLING"), 1.0f, 1.0f);
                } catch (IllegalArgumentException ex) {
                    // Ignore if sound doesn't exist
                }
            }
        } else {
            sender.sendMessage(ChatColor.YELLOW + "Join/Leave sounds disabled!");
        }
    }

    private void handlePack(CommandSender sender, String[] args) {
        if (!sender.hasPermission("welcomemat.pack")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to change message packs!");
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        if (args.length < 2) {
            String currentPack = config.getCurrentMessagePack();
            sender.sendMessage(ChatColor.GOLD + "Current message pack: " + ChatColor.YELLOW + currentPack);
            sender.sendMessage(ChatColor.GOLD + "Available packs: " + ChatColor.YELLOW + 
                String.join(", ", config.getAvailableMessagePacks()));
            return;
        }

        String pack = args[1].toLowerCase();
        if (config.setMessagePack(pack)) {
            sender.sendMessage(ChatColor.GREEN + "Message pack changed to: " + pack);
        } else {
            sender.sendMessage(ChatColor.RED + "Invalid message pack! Use /wm pack to see available packs.");
        }
    }

    private void handleConfig(CommandSender sender, String[] args) {
        if (!sender.hasPermission("welcomemat.config")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to modify the configuration!");
            return;
        }

        if (args.length < 2) {
            sendConfigHelp(sender);
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        switch (args[1].toLowerCase()) {
            case "set":
                if (args.length < 4) {
                    sender.sendMessage(ChatColor.RED + "Usage: /wm config set <path> <value>");
                    return;
                }
                String path = args[2];
                String value = String.join(" ", Arrays.copyOfRange(args, 3, args.length));
                
                // Handle sound settings specially
                if (path.endsWith(".sound")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Only players can preview sounds!");
                        return;
                    }
                    
                    try {
                        Sound sound = Sound.valueOf(value.toUpperCase());
                        Player player = (Player) sender;
                        
                        // Get volume and pitch from config or use defaults
                        float volume = 1.0f;
                        float pitch = 1.0f;
                        
                        if (path.startsWith("sounds.join.")) {
                            volume = config.getJoinSoundVolume();
                            pitch = config.getJoinSoundPitch();
                        } else if (path.startsWith("sounds.leave.")) {
                            volume = config.getLeaveSoundVolume();
                            pitch = config.getLeaveSoundPitch();
                        }
                        
                        // Play the sound
                        player.playSound(player.getLocation(), sound, volume, pitch);
                        
                        // Save the config
                        config.set(path, value);
                        sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.YELLOW + path + 
                            ChatColor.GREEN + " to: " + ChatColor.YELLOW + value + 
                            ChatColor.GREEN + " (Sound previewed!)");
                        
                    } catch (IllegalArgumentException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid sound: " + value);
                        sender.sendMessage(ChatColor.GRAY + "Available sounds:");
                        for (String soundName : getAllSoundNames()) {
                            sender.sendMessage(ChatColor.GRAY + "• " + soundName);
                        }
                    }
                    return;
                }
                
                // Handle volume/pitch settings
                if (path.endsWith(".volume") || path.endsWith(".pitch")) {
                    if (!(sender instanceof Player)) {
                        sender.sendMessage(ChatColor.RED + "Only players can preview sounds!");
                        return;
                    }
                    
                    try {
                        float numValue = Float.parseFloat(value);
                        if (numValue < 0 || numValue > 2) {
                            sender.sendMessage(ChatColor.RED + "Value must be between 0 and 2!");
                            return;
                        }
                        
                        // Save the config with the float value
                        config.set(path, numValue);
                        
                        // Preview the sound with new settings
                        Player player = (Player) sender;
                        String soundPath = path.replace(".volume", ".sound").replace(".pitch", ".sound");
                        String soundName = config.getString(soundPath);
                        
                        if (soundName != null) {
                            try {
                                Sound sound = Sound.valueOf(soundName.toUpperCase());
                                float volume = path.endsWith(".volume") ? numValue : 
                                    config.getFloat(path.substring(0, path.lastIndexOf('.')) + ".volume", 1.0f);
                                float pitch = path.endsWith(".pitch") ? numValue : 
                                    config.getFloat(path.substring(0, path.lastIndexOf('.')) + ".pitch", 1.0f);
                                
                                player.playSound(player.getLocation(), sound, volume, pitch);
                                sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.YELLOW + path + 
                                    ChatColor.GREEN + " to: " + ChatColor.YELLOW + value + 
                                    ChatColor.GREEN + " (Sound previewed!)");
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.YELLOW + path + 
                                    ChatColor.GREEN + " to: " + ChatColor.YELLOW + value + 
                                    ChatColor.RED + " (Could not preview sound)");
                            }
                        }
                        return;
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Value must be a number!");
                        return;
                    }
                }
                
                // Handle other config settings normally
                config.set(path, value);
                sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.YELLOW + path + 
                    ChatColor.GREEN + " to: " + ChatColor.YELLOW + value);
                break;

            case "get":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /wm config get <path>");
                    return;
                }
                Object val = config.get(args[2]);
                if (val == null) {
                    sender.sendMessage(ChatColor.RED + "No value found for path: " + args[2]);
                    return;
                }
                
                // Pretty print the value based on type
                if (val instanceof ConfigurationSection) {
                    String displayPath = plugin.getConfig().getString("display-names.config_paths." + args[2], args[2]);
                    sender.sendMessage(ChatColor.GOLD + "Section " + ChatColor.YELLOW + displayPath + ChatColor.GOLD + ":");
                    ConfigurationSection section = (ConfigurationSection) val;
                    for (String key : section.getKeys(false)) {
                        Object sectionValue = section.get(key);
                        String displayKey = plugin.getConfig().getString("display-names." + args[2] + "." + key, key);
                        String displayValue = formatValue(sectionValue);
                        sender.sendMessage(ChatColor.YELLOW + "  " + displayKey + ": " + ChatColor.WHITE + displayValue);
                    }
                } else {
                    String displayPath = plugin.getConfig().getString("display-names.config_paths." + args[2], args[2]);
                    String displayValue = formatValue(val);
                    sender.sendMessage(ChatColor.GOLD + displayPath + ": " + ChatColor.YELLOW + displayValue);
                }
                break;

            case "list":
                String listPath = args.length > 2 ? args[2] : "";
                List<String> keys = config.getKeys(listPath);
                if (keys.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "No configuration keys found" + 
                        (listPath.isEmpty() ? "" : " for path: " + listPath));
                    return;
                }
                sender.sendMessage(ChatColor.GOLD + "Configuration keys" + 
                    (listPath.isEmpty() ? "" : " in " + listPath) + ":");
                keys.forEach(key -> sender.sendMessage(ChatColor.YELLOW + "• " + key));
                break;

            default:
                sendConfigHelp(sender);
        }
    }

    private void handleLanguage(CommandSender sender, String[] args) {
        if (!sender.hasPermission("welcomemat.language")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("language.no_permission"));
            return;
        }

        LanguageManager langManager = plugin.getLanguageManager();
        if (args.length < 2) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("language", langManager.getCurrentLanguage());
            placeholders.put("languages", "english, spanish");  // Simplified language list
            sender.sendMessage(langManager.getMessage("language.current", placeholders));
            sender.sendMessage(langManager.getMessage("language.available", placeholders));
            return;
        }

        String language = args[1].toLowerCase();
        // Map user-friendly names to language codes
        switch (language) {
            case "english":
            case "spanish":
                if (langManager.setLanguage(language)) {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("language", language);
                    sender.sendMessage(langManager.getMessage("language.changed", placeholders));
                } else {
                    Map<String, String> placeholders = new HashMap<>();
                    placeholders.put("languages", "english, spanish");
                    sender.sendMessage(langManager.getMessage("language.invalid", placeholders));
                }
                break;
            default:
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("languages", "english, spanish");
                sender.sendMessage(langManager.getMessage("language.invalid", placeholders));
                break;
        }
    }

    private void handleEffects(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("effects.players_only"));
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("welcomemat.effects")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("effects.no_permission"));
            return;
        }

        if (!plugin.getConfig().getBoolean("effects.enabled")) {
            player.sendMessage(plugin.getLanguageManager().getMessage("effects.disabled"));
            return;
        }

        DatabaseManager db = plugin.getDatabaseManager();
        boolean current = db.getEffectPreference(player.getUniqueId());
        db.setEffectPreference(player.getUniqueId(), !current);
        
        player.sendMessage(plugin.getLanguageManager().getMessage(
            current ? "effects.disabled" : "effects.enabled"));
        playToggleSound(player);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            String[] commands = {"reload", "sound", "pack", "config", "help", "language", "gui"};
            return filterCompletions(commands, args[0]);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "pack":
                    return filterCompletions(
                        plugin.getConfigManager().getAvailableMessagePacks().toArray(new String[0]),
                        args[1]
                    );
                case "config":
                    return filterCompletions(new String[]{"set", "get", "list"}, args[1]);
                case "language":
                    return filterCompletions(new String[]{"english", "spanish"}, args[1]);
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("config")) {
            List<String> paths = new ArrayList<>();
            
            // Add common configuration paths
            paths.addAll(plugin.getConfigManager().getKeys(""));
            
            // Add sound-specific paths for better navigation
            if (args[2].toLowerCase().startsWith("sounds")) {
                paths.add("sounds.join.sound");
                paths.add("sounds.join.volume");
                paths.add("sounds.join.pitch");
                paths.add("sounds.leave.sound");
                paths.add("sounds.leave.volume");
                paths.add("sounds.leave.pitch");
            }
            
            return filterCompletions(paths.toArray(new String[0]), args[2]);
        }

        // Sound suggestions for sound configuration
        if (args.length == 4 && args[0].equalsIgnoreCase("config") && 
            args[1].equalsIgnoreCase("set") && 
            (args[2].endsWith(".sound"))) {
            return filterCompletions(getAllSoundNames(), args[3]);
        }

        return completions;
    }

    private String[] getAllSoundNames() {
        List<String> sounds = new ArrayList<>();
        
        // Modern sounds (1.13+)
        sounds.add("BLOCK_NOTE_BLOCK_PLING");
        sounds.add("BLOCK_NOTE_BLOCK_CHIME");
        sounds.add("BLOCK_NOTE_BLOCK_BELL");
        sounds.add("BLOCK_NOTE_BLOCK_BASS");
        sounds.add("ENTITY_PLAYER_LEVELUP");
        sounds.add("ENTITY_EXPERIENCE_ORB_PICKUP");
        sounds.add("BLOCK_ANVIL_LAND");
        sounds.add("ENTITY_VILLAGER_YES");
        sounds.add("ENTITY_VILLAGER_NO");
        
        return sounds.toArray(new String[0]);
    }

    private void sendConfigHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "══ WelcomeMat Config Help ══");
        sender.sendMessage(ChatColor.YELLOW + "➤ /wm config set <path> <value> " + 
            ChatColor.WHITE + "- Set a config value");
        sender.sendMessage(ChatColor.YELLOW + "➤ /wm config get <path> " + 
            ChatColor.WHITE + "- Get a config value");
        sender.sendMessage(ChatColor.YELLOW + "➤ /wm config list [path] " + 
            ChatColor.WHITE + "- List config keys");
        sender.sendMessage(ChatColor.GRAY + "Common paths:");
        sender.sendMessage(ChatColor.GRAY + "• sounds.join.sound");
        sender.sendMessage(ChatColor.GRAY + "• sounds.join.volume");
        sender.sendMessage(ChatColor.GRAY + "• sounds.leave.sound");
        sender.sendMessage(ChatColor.GRAY + "• messages.style");
    }

    private void sendHelp(CommandSender sender) {
        LanguageManager lang = plugin.getLanguageManager();
        sender.sendMessage(lang.getMessage("help.header"));
        
        if (sender instanceof Player && sender.hasPermission("welcomemat.config")) {
            sender.sendMessage(ChatColor.GOLD + "➤ " + ChatColor.GREEN + "/wm gui " + 
                ChatColor.GRAY + "- Open settings menu");
            sender.sendMessage("");
        }

        if (sender.hasPermission("welcomemat.reload")) {
            sender.sendMessage(lang.getMessage("help.reload"));
        }
        if (sender.hasPermission("welcomemat.sound.toggle")) {
            sender.sendMessage(lang.getMessage("help.sound"));
        }
        if (sender.hasPermission("welcomemat.pack")) {
            sender.sendMessage(lang.getMessage("help.pack"));
        }
        if (sender.hasPermission("welcomemat.config")) {
            sender.sendMessage(lang.getMessage("help.config"));
        }
        if (sender.hasPermission("welcomemat.language")) {
            sender.sendMessage(lang.getMessage("help.language"));
        }
        sender.sendMessage(lang.getMessage("help.help"));
    }

    private List<String> filterCompletions(String[] options, String input) {
        return Arrays.stream(options)
            .filter(option -> option.toLowerCase().startsWith(input.toLowerCase()))
            .collect(Collectors.toList());
    }

    private void playToggleSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.5f, 1.0f);
    }

    private String formatValue(Object value) {
        if (value instanceof String) {
            String strValue = (String) value;
            // Check if it's a sound name
            String soundDisplay = plugin.getConfig().getString("display-names.sounds." + strValue, null);
            if (soundDisplay != null) {
                return soundDisplay;
            }
            // Check if it's an effect type
            String effectDisplay = plugin.getConfig().getString("display-names.effects." + strValue, null);
            if (effectDisplay != null) {
                return effectDisplay;
            }
            // Check if it's a particle type
            String particleDisplay = plugin.getConfig().getString("display-names.particles." + strValue, null);
            if (particleDisplay != null) {
                return particleDisplay;
            }
        }
        return String.valueOf(value);
    }
}