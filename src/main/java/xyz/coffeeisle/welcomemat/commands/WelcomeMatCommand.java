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
import xyz.coffeeisle.welcomemat.utils.SplashEditorManager;
import xyz.coffeeisle.welcomemat.effects.AnimationRegistry;
import xyz.coffeeisle.welcomemat.effects.animations.Animation;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.hover.content.Text;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
            case "splash":
                handleSplash(sender, args);
                break;
            case "language":
                handleLanguage(sender, args);
                break;
            case "animation":
                handleAnimationCommand(sender, args);
                break;
            case "gui":
                if (!(sender instanceof Player)) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("errors.players_only"));
                    return true;
                }
                if (!sender.hasPermission("welcomemat.config") && !sender.hasPermission("welcomemat.gui")) {
                    sender.sendMessage(plugin.getLanguageManager().getMessage("errors.no_permission"));
                    return true;
                }
                new SettingsGUI(plugin).openMainMenu((Player) sender);
                break;
            case "effects":
                handleEffects(sender);
                break;
            case "on":
                handlePluginToggle(sender, true);
                break;
            case "off":
                handlePluginToggle(sender, false);
                break;
            default:
                sender.sendMessage(plugin.getLanguageManager().getMessage("errors.unknown_command"));
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
        LanguageManager lang = plugin.getLanguageManager();
        if (!(sender instanceof Player)) {
            sender.sendMessage(lang.getMessage("errors.players_only"));
            return;
        }

        if (!sender.hasPermission("welcomemat.sound.toggle")) {
            sender.sendMessage(lang.getMessage("sound.no-permission"));
            return;
        }

        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();
        DatabaseManager db = plugin.getDatabaseManager();
        
        boolean currentState = db.getSoundPreference(uuid);
        boolean newState = !currentState;
        
        db.setSoundPreference(uuid, newState);

        if (newState) {
            sender.sendMessage(lang.getMessage("sound.enabled"));
            try {
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.0f);
            } catch (IllegalArgumentException ignored) {
            }
        } else {
            sender.sendMessage(lang.getMessage("sound.disabled"));
        }
    }

    private void handlePack(CommandSender sender, String[] args) {
        LanguageManager lang = plugin.getLanguageManager();
        if (!sender.hasPermission("welcomemat.pack")) {
            sender.sendMessage(lang.getMessage("pack.no-permission"));
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        if (args.length == 1) {
            sendPackOverview(sender);
            return;
        }

        if (args[1].equalsIgnoreCase("mode")) {
            handlePackMode(sender, args);
            return;
        }

        if (args[1].equalsIgnoreCase("create")) {
            handlePackCreate(sender, args);
            return;
        }

        String pack = args[1].toLowerCase();
        if (config.setMessagePack(pack)) {
            config.setUsePackForJoinMessages(true);
            config.setUsePackForLeaveMessages(true);
            config.setUsePackForSplash(true);
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("pack", pack);
            sender.sendMessage(lang.getMessage("pack.changed", placeholders));
            sender.sendMessage(lang.getMessage("pack.changed_mode_tip"));
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("packs", String.join(", ", lang.getMessagePackIds()));
            sender.sendMessage(lang.getMessage("pack.invalid", placeholders));
        }
    }

    private void handlePackMode(CommandSender sender, String[] args) {
        LanguageManager lang = plugin.getLanguageManager();
        if (args.length < 4) {
            sender.sendMessage(lang.getMessage("pack.mode.usage"));
            return;
        }

        String target = args[2].toLowerCase();
        String mode = args[3].toLowerCase();
        boolean usePack;
        if (mode.equals("pack") || mode.equals("on")) {
            usePack = true;
        } else if (mode.equals("custom") || mode.equals("off")) {
            usePack = false;
        } else {
            sender.sendMessage(lang.getMessage("pack.mode.invalid_option"));
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        switch (target) {
            case "join":
                config.setUsePackForJoinMessages(usePack);
                sender.sendMessage(describeModeChange(lang, "pack.mode.target.join", usePack));
                break;
            case "leave":
                config.setUsePackForLeaveMessages(usePack);
                sender.sendMessage(describeModeChange(lang, "pack.mode.target.leave", usePack));
                break;
            case "splash":
                config.setUsePackForSplash(usePack);
                sender.sendMessage(describeModeChange(lang, "pack.mode.target.splash", usePack));
                break;
            default:
                sender.sendMessage(lang.getMessage("pack.mode.invalid_target"));
        }
    }

    private void handlePackCreate(CommandSender sender, String[] args) {
        LanguageManager lang = plugin.getLanguageManager();
        if (args.length < 3) {
            sender.sendMessage(lang.getMessage("pack.create.usage"));
            return;
        }

        String rawName = String.join(" ", Arrays.copyOfRange(args, 2, args.length)).trim();
        if (rawName.isEmpty()) {
            sender.sendMessage(lang.getMessage("pack.create.name_required"));
            return;
        }

        String packId = normalizePackId(rawName);
        if (packId.length() < 3) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("name", rawName);
            sender.sendMessage(lang.getMessage("pack.create.invalid_name", placeholders));
            return;
        }

        if (lang.getMessagePackIds().contains(packId)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("id", packId);
            sender.sendMessage(lang.getMessage("pack.create.exists", placeholders));
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        List<String> joinMessages = config.getCustomJoinMessages();
        List<String> firstJoinMessages = config.getCustomFirstJoinMessages();
        List<String> leaveMessages = config.getCustomLeaveMessages();

        if (joinMessages.isEmpty()) {
            joinMessages = Collections.singletonList("&e%player% &ajust joined the server!");
        }
        if (leaveMessages.isEmpty()) {
            leaveMessages = Collections.singletonList("&e%player% &chas left the server!");
        }

        boolean saved = plugin.getLanguageManager().saveCustomPack(
            packId,
            rawName,
            joinMessages,
            firstJoinMessages,
            leaveMessages,
            config.getRawJoinTitle(),
            config.getRawJoinSubtitle(),
            sender.getName()
        );

        if (!saved) {
            sender.sendMessage(lang.getMessage("pack.create.failed"));
            return;
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("name", rawName);
        placeholders.put("id", packId);
        sender.sendMessage(lang.getMessage("pack.create.success", placeholders));
        placeholders.clear();
        placeholders.put("id", packId);
        sender.sendMessage(lang.getMessage("pack.create.hint", placeholders));
    }

    private void sendPackOverview(CommandSender sender) {
        ConfigManager config = plugin.getConfigManager();
        LanguageManager lang = plugin.getLanguageManager();
        Map<String, String> placeholders = new HashMap<>();
        sender.sendMessage(lang.getMessage("pack.overview.header"));
        placeholders.put("pack", config.getCurrentMessagePack());
        sender.sendMessage(lang.getMessage("pack.overview.current", placeholders));
        sender.sendMessage(lang.getMessage("pack.overview.usage"));
        sender.sendMessage(lang.getMessage("pack.overview.modes"));
        sender.sendMessage(lang.getMessage("pack.overview.create"));

        sender.sendMessage("");
        sender.sendMessage(lang.getMessage("pack.overview.available"));
        List<String> packs = lang.getMessagePackIds();
        if (packs.isEmpty()) {
            sender.sendMessage(lang.getMessage("pack.overview.none"));
        } else {
            for (String id : packs) {
                placeholders.clear();
                placeholders.put("id", id);
                placeholders.put("name", lang.getMessagePackDisplayName(id));
                sender.sendMessage(lang.getMessage("pack.overview.entry", placeholders));
            }
        }

        sender.sendMessage("");
        sender.sendMessage(lang.getMessage("pack.overview.sources"));
        sender.sendMessage(describePackSource(lang, "pack.mode.target.join", config.usePackForJoinMessages()));
        sender.sendMessage(describePackSource(lang, "pack.mode.target.leave", config.usePackForLeaveMessages()));
        sender.sendMessage(describePackSource(lang, "pack.mode.target.splash", config.usePackForSplash()));
    }

    private String describePackSource(LanguageManager lang, String targetKey, boolean usingPack) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("label", lang.getMessage(targetKey));
        placeholders.put("source", lang.getMessage(usingPack ? "pack.mode.source.pack" : "pack.mode.source.custom"));
        return lang.getMessage("pack.overview.source_line", placeholders);
    }

    private String describeModeChange(LanguageManager lang, String targetKey, boolean usingPack) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("target", lang.getMessage(targetKey));
        placeholders.put("source", lang.getMessage(usingPack ? "pack.mode.source.pack" : "pack.mode.source.custom"));
        return lang.getMessage("pack.mode.set", placeholders);
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
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /wm config set <path> <value>");
                    // Show available paths
                    sender.sendMessage(ChatColor.GRAY + "Available settings:");
                    List<String> commonPaths = new ArrayList<>();
                    commonPaths.add("join-message");
                    commonPaths.add("leave-message");
                    commonPaths.add("join-sound");
                    commonPaths.add("leave-sound");
                    commonPaths.add("effect-type");
                    commonPaths.add("message-pack");
                    
                    for (String commonPath : commonPaths) {
                        sender.sendMessage(ChatColor.GRAY + "• " + ChatColor.AQUA + commonPath);
                    }
                    return;
                }
                
                if (args.length < 4) {
                    String path = resolveConfigPath(args[2]);
                    sender.sendMessage(ChatColor.RED + "Usage: /wm config set " + ChatColor.AQUA + friendlyConfigName(args[2]) + ChatColor.RED + " <value>");
                    
                    // Show available values based on the path
                    if (path.endsWith(".sound")) {
                        sender.sendMessage(ChatColor.GRAY + "Available sounds:");
                        for (String soundName : getAllSoundNames()) {
                            sender.sendMessage(ChatColor.GRAY + "• " + soundName);
                        }
                    } else if (path.startsWith("features.") || path.equals("effects.enabled")) {
                        sender.sendMessage(ChatColor.GRAY + "Available values: true, false");
                    } else if (path.equals("effects.type")) {
                        sender.sendMessage(ChatColor.GRAY + "Available types: SPIRAL, HELIX, FOUNTAIN, BURST");
                    } else if (path.equals("message-packs.selected")) {
                        sender.sendMessage(ChatColor.GRAY + "Available packs: " + 
                            String.join(", ", config.getAvailableMessagePacks()));
                    } else if (path.endsWith(".volume") || path.endsWith(".pitch")) {
                        sender.sendMessage(ChatColor.GRAY + "Recommended values: 0.5, 1.0, 1.5, 2.0 (must be between 0 and 2)");
                    }
                    return;
                }
                String path = resolveConfigPath(args[2]);
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
                        sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.AQUA + friendlyConfigName(args[2]) + 
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
                                sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.AQUA + friendlyConfigName(args[2]) + 
                                    ChatColor.GREEN + " to: " + ChatColor.YELLOW + value + 
                                    ChatColor.GREEN + " (Sound previewed!)");
                            } catch (IllegalArgumentException e) {
                                sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.AQUA + friendlyConfigName(args[2]) + 
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
                sender.sendMessage(ChatColor.GREEN + "Set " + ChatColor.AQUA + friendlyConfigName(args[2]) + 
                    ChatColor.GREEN + " to: " + ChatColor.YELLOW + value);
                break;

            case "get":
                if (args.length < 3) {
                    sender.sendMessage(ChatColor.RED + "Usage: /wm config get " + ChatColor.AQUA + "<name>");
                    return;
                }
                String getPath = resolveConfigPath(args[2]);
                Object val = config.get(getPath);
                if (val == null) {
                    sender.sendMessage(ChatColor.RED + "No value found for: " + ChatColor.AQUA + friendlyConfigName(args[2]));
                    return;
                }
                
                // Pretty print the value based on type
                if (val instanceof ConfigurationSection) {
                    String displayPath = plugin.getConfig().getString("display-names.config_paths." + getPath, friendlyConfigName(args[2]));
                    sender.sendMessage(ChatColor.GOLD + "Section " + ChatColor.AQUA + displayPath + ChatColor.GOLD + ":");
                    ConfigurationSection section = (ConfigurationSection) val;
                    for (String key : section.getKeys(false)) {
                        Object sectionValue = section.get(key);
                        String displayKey = plugin.getConfig().getString("display-names." + getPath + "." + key, key);
                        String displayValue = formatValue(sectionValue);
                        sender.sendMessage(ChatColor.YELLOW + "  " + displayKey + ": " + ChatColor.WHITE + displayValue);
                    }
                } else {
                    String displayPath = plugin.getConfig().getString("display-names.config_paths." + getPath, friendlyConfigName(args[2]));
                    String displayValue = formatValue(val);
                    sender.sendMessage(ChatColor.GOLD + displayPath + ": " + ChatColor.YELLOW + displayValue);
                }
                break;

            case "list":
                String listPath = args.length > 2 ? resolveConfigPath(args[2]) : "";
                List<String> keys = config.getKeys(listPath);
                if (keys.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + "No configuration keys found" + 
                        (listPath.isEmpty() ? "" : " for: " + ChatColor.AQUA + friendlyConfigName(args[2])));
                    return;
                }
                sender.sendMessage(ChatColor.GOLD + "Configuration keys" + 
                    (listPath.isEmpty() ? "" : " in " + ChatColor.AQUA + friendlyConfigName(args[2])) + ":");
                keys.forEach(key -> sender.sendMessage(ChatColor.YELLOW + "• " + key));
                break;

            default:
                sendConfigHelp(sender);
        }
    }

    private void handleSplash(CommandSender sender, String[] args) {
        LanguageManager lang = plugin.getLanguageManager();

        if (!sender.hasPermission("welcomemat.config") && !sender.hasPermission("welcomemat.splash")) {
            sender.sendMessage(lang.getMessage("splash.no_permission"));
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        if (args.length == 1) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("title", config.getJoinTitle());
            placeholders.put("subtitle", config.getJoinSubtitle());
            sender.sendMessage(lang.getMessage("splash.header"));
            sender.sendMessage(lang.getMessage("splash.overview", placeholders));
            sender.sendMessage(lang.getMessage("splash.helper"));

            if (sender instanceof Player) {
                sendSplashButtons((Player) sender);
            } else {
                sender.sendMessage(lang.getMessage("splash.players_only"));
            }
            return;
        }

        SplashEditorManager.SplashField field;
        switch (args[1].toLowerCase()) {
            case "title":
                field = SplashEditorManager.SplashField.TITLE;
                break;
            case "subtitle":
                field = SplashEditorManager.SplashField.SUBTITLE;
                break;
            default:
                sender.sendMessage(lang.getMessage("splash.helper"));
                return;
        }

        sendSplashFieldInfo(sender, field);

        if (args.length >= 3 && args[2].equalsIgnoreCase("edit")) {
            startSplashEdit(sender, field);
        }
    }

    private void sendSplashFieldInfo(CommandSender sender, SplashEditorManager.SplashField field) {
        LanguageManager lang = plugin.getLanguageManager();
        ConfigManager config = plugin.getConfigManager();
        Map<String, String> placeholders = new HashMap<>();

        if (field == SplashEditorManager.SplashField.TITLE) {
            placeholders.put("title", config.getJoinTitle());
            sender.sendMessage(lang.getMessage("splash.current_title", placeholders));
        } else {
            placeholders.put("subtitle", config.getJoinSubtitle());
            sender.sendMessage(lang.getMessage("splash.current_subtitle", placeholders));
        }

        if (sender instanceof Player) {
            sendSplashButton((Player) sender, field);
        } else {
            sender.sendMessage(lang.getMessage("splash.players_only"));
        }
    }

    private void startSplashEdit(CommandSender sender, SplashEditorManager.SplashField field) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("splash.not_player"));
            return;
        }

        Player player = (Player) sender;
        plugin.getSplashEditorManager().beginEditing(player, field);
    }

    private void sendSplashButtons(Player player) {
        sendSplashButton(player, SplashEditorManager.SplashField.TITLE);
        sendSplashButton(player, SplashEditorManager.SplashField.SUBTITLE);
    }

    private void sendSplashButton(Player player, SplashEditorManager.SplashField field) {
        LanguageManager lang = plugin.getLanguageManager();
        String textKey = field == SplashEditorManager.SplashField.TITLE ?
            "splash.button_title" : "splash.button_subtitle";
        String hoverKey = field == SplashEditorManager.SplashField.TITLE ?
            "splash.hover_title" : "splash.hover_subtitle";
        String command = field == SplashEditorManager.SplashField.TITLE ?
            "/wm splash title edit" : "/wm splash subtitle edit";

        BaseComponent[] components = TextComponent.fromLegacyText(lang.getMessage(textKey));
        ClickEvent clickEvent = new ClickEvent(ClickEvent.Action.RUN_COMMAND, command);
        HoverEvent hoverEvent = new HoverEvent(HoverEvent.Action.SHOW_TEXT, new Text(lang.getMessage(hoverKey)));
        for (BaseComponent component : components) {
            component.setClickEvent(clickEvent);
            component.setHoverEvent(hoverEvent);
        }
        player.spigot().sendMessage(components);
    }

    private void handleLanguage(CommandSender sender, String[] args) {
        if (!sender.hasPermission("welcomemat.language")) {
            sender.sendMessage(plugin.getLanguageManager().getMessage("language.no_permission"));
            return;
        }

        LanguageManager langManager = plugin.getLanguageManager();
        String available = String.join(", ", langManager.getAvailableLanguages());
        if (args.length < 2) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("language", langManager.getCurrentLanguage());
            placeholders.put("languages", available);
            sender.sendMessage(langManager.getMessage("language.current", placeholders));
            sender.sendMessage(langManager.getMessage("language.available", placeholders));
            return;
        }

        String language = args[1].toLowerCase();
        if (langManager.setLanguage(language)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("language", language);
            sender.sendMessage(langManager.getMessage("language.changed", placeholders));
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("languages", available);
            sender.sendMessage(langManager.getMessage("language.invalid", placeholders));
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

    private void handleAnimationCommand(CommandSender sender, String[] args) {
        LanguageManager lang = plugin.getLanguageManager();
        if (!(sender instanceof Player)) {
            sender.sendMessage(lang.getMessage("effects.players_only"));
            return;
        }

        Player player = (Player) sender;
        if (!player.hasPermission("welcomemat.animation")) {
            player.sendMessage(lang.getMessage("effects.no_permission"));
            return;
        }

        if (!plugin.getConfig().getBoolean("effects.enabled")) {
            player.sendMessage(lang.getMessage("effects.disabled"));
            return;
        }

        AnimationRegistry registry = plugin.getAnimationRegistry();
        if (registry == null) {
            player.sendMessage(lang.getMessage("effects.animation.list_none"));
            return;
        }

        DatabaseManager db = plugin.getDatabaseManager();
        String preference = db.getAnimationPreference(player.getUniqueId());

        if (args.length == 1) {
            sendAnimationStatus(player, registry, preference);
            return;
        }

        String sub = args[1].toLowerCase();
        switch (sub) {
            case "list":
                sendAnimationList(player, registry);
                return;
            case "set":
                if (args.length < 3) {
                    player.sendMessage(lang.getMessage("effects.animation.usage"));
                    return;
                }
                applyAnimationSelection(player, registry, args[2]);
                return;
            case "test":
                if (args.length < 3) {
                    player.sendMessage(lang.getMessage("effects.animation.test_usage"));
                    return;
                }
                performAnimationTest(player, registry, args[2]);
                return;
            default:
                applyAnimationSelection(player, registry, args[1]);
        }
    }

    private void sendAnimationStatus(Player player, AnimationRegistry registry, String preference) {
        LanguageManager lang = plugin.getLanguageManager();
        player.sendMessage(lang.getMessage("effects.animation.header"));
        if (preference == null || DatabaseManager.ANIMATION_FOLLOW_PACK.equalsIgnoreCase(preference)) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("animation", describeAnimation(registry, plugin.getConfigManager().getEffectiveAnimationId()));
            player.sendMessage(lang.getMessage("effects.animation.current_pack", placeholders));
        } else if (DatabaseManager.ANIMATION_RANDOM.equalsIgnoreCase(preference)) {
            player.sendMessage(lang.getMessage("effects.animation.current_random"));
        } else {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("animation", describeAnimation(registry, preference));
            player.sendMessage(lang.getMessage("effects.animation.current_custom", placeholders));
        }
        player.sendMessage(lang.getMessage("effects.animation.usage"));
    }

    private void sendAnimationList(Player player, AnimationRegistry registry) {
        LanguageManager lang = plugin.getLanguageManager();
        List<Animation> animations = registry.getRegisteredAnimations();
        if (animations.isEmpty()) {
            player.sendMessage(lang.getMessage("effects.animation.list_none"));
            return;
        }

        player.sendMessage(lang.getMessage("effects.animation.list_header"));
        player.sendMessage(lang.getMessage("effects.animation.list_pack"));
        player.sendMessage(lang.getMessage("effects.animation.list_random"));

        animations.sort(Comparator.comparing(Animation::getDisplayName, String.CASE_INSENSITIVE_ORDER));
        for (Animation animation : animations) {
            Map<String, String> placeholders = new HashMap<>();
            placeholders.put("id", animation.getName());
            placeholders.put("name", animation.getDisplayName());
            player.sendMessage(lang.getMessage("effects.animation.list_entry", placeholders));
        }
        player.sendMessage(lang.getMessage("effects.animation.list_hint"));
    }

    private void applyAnimationSelection(Player player, AnimationRegistry registry, String selectionRaw) {
        LanguageManager lang = plugin.getLanguageManager();
        if (selectionRaw == null || selectionRaw.isEmpty()) {
            player.sendMessage(lang.getMessage("effects.animation.usage"));
            return;
        }

        String selection = selectionRaw.trim().toLowerCase();
        DatabaseManager db = plugin.getDatabaseManager();

        if (selection.equals("pack") || selection.equals("default")) {
            db.setAnimationPreference(player.getUniqueId(), DatabaseManager.ANIMATION_FOLLOW_PACK);
            player.sendMessage(lang.getMessage("effects.animation.set_pack"));
            return;
        }

        if (selection.equals("random")) {
            db.setAnimationPreference(player.getUniqueId(), DatabaseManager.ANIMATION_RANDOM);
            player.sendMessage(lang.getMessage("effects.animation.set_random"));
            return;
        }

        if (!registry.hasAnimation(selection)) {
            player.sendMessage(lang.getMessage("effects.animation.invalid"));
            return;
        }

        db.setAnimationPreference(player.getUniqueId(), selection);
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("animation", describeAnimation(registry, selection));
        player.sendMessage(lang.getMessage("effects.animation.set_custom", placeholders));
    }

    private String describeAnimation(AnimationRegistry registry, String animationId) {
        LanguageManager lang = plugin.getLanguageManager();
        if (animationId == null || animationId.isEmpty()) {
            return lang.getMessage("effects.animation.describe_unknown");
        }

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("id", animationId);
        Animation animation = registry.getAnimation(animationId);
        if (animation == null) {
            return lang.getMessage("effects.animation.describe_missing", placeholders);
        }
        placeholders.put("name", animation.getDisplayName());
        return lang.getMessage("effects.animation.describe_known", placeholders);
    }

    private void performAnimationTest(Player player, AnimationRegistry registry, String selectionRaw) {
        LanguageManager lang = plugin.getLanguageManager();
        if (selectionRaw == null || selectionRaw.trim().isEmpty()) {
            player.sendMessage(lang.getMessage("effects.animation.test_usage"));
            return;
        }

        String selection = selectionRaw.trim().toLowerCase();
        Map<String, String> placeholders = new HashMap<>();

        if (selection.equals("pack") || selection.equals("default")) {
            String animationId = plugin.getConfigManager().getEffectiveAnimationId();
            if (animationId == null || animationId.isEmpty() || !registry.playAnimation(player, animationId)) {
                player.sendMessage(lang.getMessage("effects.animation.test_pack_unavailable"));
                return;
            }
            placeholders.put("animation", describeAnimation(registry, animationId));
            player.sendMessage(lang.getMessage("effects.animation.test_pack", placeholders));
            return;
        }

        if (selection.equals("random")) {
            String randomId = registry.pickRandomAnimationId();
            if (randomId == null) {
                player.sendMessage(lang.getMessage("effects.animation.test_random_unavailable"));
                return;
            }
            if (!registry.playAnimation(player, randomId)) {
                player.sendMessage(lang.getMessage("effects.animation.test_failed"));
                return;
            }
            placeholders.put("animation", describeAnimation(registry, randomId));
            player.sendMessage(lang.getMessage("effects.animation.test_random", placeholders));
            return;
        }

        if (!registry.hasAnimation(selection)) {
            placeholders.put("id", selectionRaw);
            player.sendMessage(lang.getMessage("effects.animation.test_invalid", placeholders));
            return;
        }

        if (!registry.playAnimation(player, selection)) {
            player.sendMessage(lang.getMessage("effects.animation.test_failed"));
            return;
        }

        placeholders.put("animation", describeAnimation(registry, selection));
        player.sendMessage(lang.getMessage("effects.animation.test_success", placeholders));
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            List<String> commands = new ArrayList<>();
            commands.add("help");
            if (sender.hasPermission("welcomemat.reload")) commands.add("reload");
            if (sender.hasPermission("welcomemat.sound.toggle")) commands.add("sound");
            if (sender.hasPermission("welcomemat.pack")) commands.add("pack");
            if (sender.hasPermission("welcomemat.config")) commands.add("config");
            if (sender.hasPermission("welcomemat.config")) commands.add("gui");
            if (sender.hasPermission("welcomemat.config")) commands.add("splash");
            if (sender.hasPermission("welcomemat.language")) commands.add("language");
            if (sender.hasPermission("welcomemat.animation")) commands.add("animation");
            if (sender.hasPermission("welcomemat.effects")) commands.add("effects");
            // Player self-toggle (only if allowed by admin)
            if (sender instanceof Player && sender.hasPermission("welcomemat.toggle")
                    && plugin.getConfigManager().isPlayerToggleAllowed()) {
                commands.add("on");
                commands.add("off");
            }
            return filterCompletions(commands.toArray(new String[0]), args[0]);
        }

        if (args.length == 2) {
            switch (args[0].toLowerCase()) {
                case "pack":
                    List<String> packOptions = new ArrayList<>(plugin.getConfigManager().getAvailableMessagePacks());
                    packOptions.add("mode");
                    packOptions.add("create");
                    return filterCompletions(
                        packOptions.toArray(new String[0]),
                        args[1]
                    );
                case "config":
                    return filterCompletions(new String[]{"set", "get", "list"}, args[1]);
                case "language":
                    return filterCompletions(new String[]{"english", "spanish", "french", "romanian", "german", "chinese"}, args[1]);
                case "splash":
                    return filterCompletions(new String[]{"title", "subtitle"}, args[1]);
                case "animation":
                    List<String> animationOptions = new ArrayList<>();
                    animationOptions.add("list");
                    animationOptions.add("set");
                    animationOptions.add("test");
                    animationOptions.add("pack");
                    animationOptions.add("random");
                    animationOptions.addAll(plugin.getAnimationRegistry().getAvailableAnimations());
                    return filterCompletions(animationOptions.toArray(new String[0]), args[1]);
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("animation") && args[1].equalsIgnoreCase("set")) {
            List<String> selections = new ArrayList<>();
            selections.add("pack");
            selections.add("random");
            selections.addAll(plugin.getAnimationRegistry().getAvailableAnimations());
            return filterCompletions(selections.toArray(new String[0]), args[2]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("animation") && args[1].equalsIgnoreCase("test")) {
            List<String> selections = new ArrayList<>();
            selections.add("pack");
            selections.add("random");
            selections.addAll(plugin.getAnimationRegistry().getAvailableAnimations());
            return filterCompletions(selections.toArray(new String[0]), args[2]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("pack") && args[1].equalsIgnoreCase("mode")) {
            return filterCompletions(new String[]{"join", "leave", "splash"}, args[2]);
        }

        if (args.length == 4 && args[0].equalsIgnoreCase("pack") && args[1].equalsIgnoreCase("mode")) {
            return filterCompletions(new String[]{"custom", "pack"}, args[3]);
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("config")) {
            List<String> paths = new ArrayList<>();
            
            if (args[1].equalsIgnoreCase("set") || args[1].equalsIgnoreCase("get")) {
                // Add common configuration paths
                paths.addAll(plugin.getConfigManager().getKeys(""));
                
                // Add feature-specific settings (friendly names)
                paths.add("join-message");
                paths.add("leave-message");
                paths.add("join-title");
                paths.add("join-sound");
                paths.add("leave-sound");

                // Add sound-specific settings
                if (args[2].toLowerCase().startsWith("join") || args[2].toLowerCase().startsWith("leave") || args[2].isEmpty()) {
                    paths.add("join-sound");
                    paths.add("join-sound-volume");
                    paths.add("join-sound-pitch");
                    paths.add("leave-sound");
                    paths.add("leave-sound-volume");
                    paths.add("leave-sound-pitch");
                }

                // Add effects-specific settings
                if (args[2].toLowerCase().startsWith("effect") || args[2].isEmpty()) {
                    paths.add("effects-enabled");
                    paths.add("effect-type");
                }

                // Add message-packs settings
                if (args[2].toLowerCase().startsWith("message") || args[2].isEmpty()) {
                    paths.add("message-pack");
                }
            }
            
            return filterCompletions(paths.toArray(new String[0]), args[2]);
        }

                    if (args.length == 3 && args[0].equalsIgnoreCase("splash")) {
                        return filterCompletions(new String[]{"edit"}, args[2]);
                    }

        // Value suggestions for configuration
        if (args.length == 4 && args[0].equalsIgnoreCase("config") && args[1].equalsIgnoreCase("set")) {
            String path = args[2].toLowerCase();
            List<String> suggestions = new ArrayList<>();
            
            // Sound suggestions
            if (path.endsWith(".sound")) {
                return filterCompletions(getAllSoundNames(), args[3]);
            }
            
            // Boolean suggestions
            else if (path.startsWith("features.") || path.equals("effects.enabled")) {
                suggestions.add("true");
                suggestions.add("false");
                return filterCompletions(suggestions.toArray(new String[0]), args[3]);
            }
            
            // Effect type suggestions
            else if (path.equals("effects.type")) {
                suggestions.add("SPIRAL");
                suggestions.add("HELIX");
                suggestions.add("FOUNTAIN");
                suggestions.add("BURST");
                return filterCompletions(suggestions.toArray(new String[0]), args[3]);
            }
            
            // Particle suggestions
            else if (path.startsWith("effects.particles.")) {
                suggestions.add("FIREWORK");
                suggestions.add("SPELL_WITCH");
                suggestions.add("FLAME");
                suggestions.add("HEART");
                suggestions.add("HAPPY_VILLAGER");
                return filterCompletions(suggestions.toArray(new String[0]), args[3]);
            }
            
            // Message pack suggestions
            else if (path.equals("message-packs.selected")) {
                return filterCompletions(
                    plugin.getConfigManager().getAvailableMessagePacks().toArray(new String[0]),
                    args[3]
                );
            }
            
            // Volume/pitch suggestions
            else if (path.endsWith(".volume") || path.endsWith(".pitch")) {
                suggestions.add("0.5");
                suggestions.add("1.0");
                suggestions.add("1.5");
                suggestions.add("2.0");
                return filterCompletions(suggestions.toArray(new String[0]), args[3]);
            }
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

    private String resolveConfigPath(String input) {
        if (input == null) return "";
        String key = input.toLowerCase().replace("_", "-").trim();
        // Already a dotted path: pass through
        if (key.contains(".")) return input;
        switch (key) {
            case "join-message": return "features.join-message";
            case "leave-message": return "features.leave-message";
            case "join-title": return "features.join-title";
            case "join-sound": return "sounds.join.sound";
            case "leave-sound": return "sounds.leave.sound";
            case "join-sound-volume":
            case "join-volume": return "sounds.join.volume";
            case "leave-sound-volume":
            case "leave-volume": return "sounds.leave.volume";
            case "join-sound-pitch":
            case "join-pitch": return "sounds.join.pitch";
            case "leave-sound-pitch":
            case "leave-pitch": return "sounds.leave.pitch";
            case "effect-type":
            case "effects-type": return "effects.type";
            case "effects-enabled":
            case "effects": return "effects.enabled";
            case "message-pack":
            case "message-packs":
            case "pack": return "message-packs.selected";
            case "message-style": return "messages.style";
            default: return input;
        }
    }

    private String friendlyConfigName(String input) {
        if (input == null) return "";
        String resolved = resolveConfigPath(input);
        // Try display-names first
        String display = plugin.getConfig().getString("display-names.config_paths." + resolved, null);
        if (display != null && !display.isEmpty()) return display;
        // Fall back to a human-friendly version of the path
        String base = resolved.isEmpty() ? input : resolved;
        String last = base.contains(".") ? base.substring(base.lastIndexOf('.') + 1) : base;
        String pretty = last.replace("-", " ").replace("_", " ");
        // Title-case
        StringBuilder sb = new StringBuilder();
        for (String word : pretty.split(" ")) {
            if (word.isEmpty()) continue;
            sb.append(Character.toUpperCase(word.charAt(0)))
               .append(word.length() > 1 ? word.substring(1) : "").append(" ");
        }
        return sb.toString().trim();
    }

    private void sendConfigHelp(CommandSender sender) {
        sender.sendMessage(ChatColor.GOLD + "══ WelcomeMat Config Help ══");
        sender.sendMessage(ChatColor.YELLOW + "➤ /wm config set <path> <value> " + 
            ChatColor.WHITE + "- Set a config value");
        sender.sendMessage(ChatColor.YELLOW + "➤ /wm config get <path> " + 
            ChatColor.WHITE + "- Get a config value");
        sender.sendMessage(ChatColor.YELLOW + "➤ /wm config list [path] " + 
            ChatColor.WHITE + "- List config keys");
        sender.sendMessage(ChatColor.GRAY + "Common settings:");
        sender.sendMessage(ChatColor.GRAY + "• " + ChatColor.AQUA + "join-sound");
        sender.sendMessage(ChatColor.GRAY + "• " + ChatColor.AQUA + "join-sound-volume");
        sender.sendMessage(ChatColor.GRAY + "• " + ChatColor.AQUA + "leave-sound");
        sender.sendMessage(ChatColor.GRAY + "• " + ChatColor.AQUA + "message-style");
    }

    private void handlePluginToggle(CommandSender sender, boolean enable) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(ChatColor.RED + "This command can only be used by players!");
            return;
        }
        Player player = (Player) sender;
        LanguageManager lang = plugin.getLanguageManager();
        if (!sender.hasPermission("welcomemat.toggle")) {
            sender.sendMessage(lang.getMessage("toggle.no_permission"));
            return;
        }
        if (!plugin.getConfigManager().isPlayerToggleAllowed()) {
            sender.sendMessage(lang.getMessage("toggle.not_allowed"));
            return;
        }
        plugin.getDatabaseManager().setPluginEnabled(player.getUniqueId(), enable);
        if (enable) {
            sender.sendMessage(lang.getMessage("toggle.enabled"));
            try { player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 1.2f); } catch (Exception ignored) {}
        } else {
            sender.sendMessage(lang.getMessage("toggle.disabled"));
            try { player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 0.8f); } catch (Exception ignored) {}
        }
    }

    private void sendHelp(CommandSender sender) {
        LanguageManager lang = plugin.getLanguageManager();
        sender.sendMessage(ChatColor.GOLD + "═══════ " + ChatColor.YELLOW + "WelcomeMat" + ChatColor.GOLD + " ═══════");
        sender.sendMessage(ChatColor.GRAY + lang.getMessage("help.subtitle"));
        sender.sendMessage("");

        if (sender instanceof Player && (sender.hasPermission("welcomemat.config") || sender.hasPermission("welcomemat.gui"))) {
            sender.sendMessage(ChatColor.AQUA + "➤ " + lang.getMessage("help.gui"));
            sender.sendMessage("");
        }

        if (sender.hasPermission("welcomemat.reload")) {
            sender.sendMessage(ChatColor.YELLOW + "➤ " + lang.getMessage("help.reload"));
        }
        if (sender.hasPermission("welcomemat.sound.toggle")) {
            sender.sendMessage(ChatColor.YELLOW + "➤ " + lang.getMessage("help.sound"));
        }
        if (sender.hasPermission("welcomemat.animation")) {
            sender.sendMessage(ChatColor.YELLOW + "➤ " + lang.getMessage("help.animation"));
        }
        if (sender.hasPermission("welcomemat.pack")) {
            sender.sendMessage(ChatColor.YELLOW + "➤ " + lang.getMessage("help.pack"));
        }
        if (sender.hasPermission("welcomemat.config")) {
            sender.sendMessage(ChatColor.YELLOW + "➤ " + lang.getMessage("help.config"));
            sender.sendMessage(ChatColor.YELLOW + "➤ " + lang.getMessage("help.splash"));
        }
        if (sender.hasPermission("welcomemat.language")) {
            sender.sendMessage(ChatColor.YELLOW + "➤ " + lang.getMessage("help.language"));
        }
        if (sender instanceof Player && sender.hasPermission("welcomemat.toggle")
                && plugin.getConfigManager().isPlayerToggleAllowed()) {
            sender.sendMessage(ChatColor.YELLOW + "➤ " + lang.getMessage("help.on"));
            sender.sendMessage(ChatColor.YELLOW + "➤ " + lang.getMessage("help.off"));
        }
        sender.sendMessage("");
        sender.sendMessage(ChatColor.GRAY + "➤ " + lang.getMessage("help.help"));
        sender.sendMessage(ChatColor.GOLD + "═════════════════════════════════");
    }

    private List<String> filterCompletions(String[] options, String input) {
        return Arrays.stream(options)
            .filter(option -> option.toLowerCase().startsWith(input.toLowerCase()))
            .collect(Collectors.toList());
    }

    private String normalizePackId(String input) {
        String lowered = input.toLowerCase().replaceAll("[^a-z0-9]+", "-");
        lowered = lowered.replaceAll("-+", "-");
        if (lowered.startsWith("-")) {
            lowered = lowered.substring(1);
        }
        if (lowered.endsWith("-")) {
            lowered = lowered.substring(0, lowered.length() - 1);
        }
        return lowered;
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