package xyz.coffeeisle.welcomemat.gui;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import xyz.coffeeisle.welcomemat.WelcomeMat;
import xyz.coffeeisle.welcomemat.ConfigManager;
import xyz.coffeeisle.welcomemat.utils.LogManager;
import org.bukkit.Sound;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class GUIListener implements Listener {
    private final WelcomeMat plugin;
    private final LogManager logManager;
    private final NamespacedKey packKey;
    private static final String[] GUI_TITLES = {
        ChatColor.DARK_GRAY + "WelcomeMat Settings",
        ChatColor.DARK_GRAY + "Language Settings",
        ChatColor.DARK_GRAY + "Message Packs",
        ChatColor.DARK_GRAY + "Sound Settings"
    };

    public GUIListener(WelcomeMat plugin) {
        this.plugin = plugin;
        this.logManager = new LogManager(plugin);
        this.packKey = new NamespacedKey(plugin, "message_pack");
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        // Only handle events for our GUI windows
        if (!isPluginGUI(title)) {
            return;
        }

        // Cancel the event only for our GUIs
        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) return;
        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;

        // Main Settings Menu
        if (title.equals(ChatColor.DARK_GRAY + "WelcomeMat Settings")) {
            handleMainMenu(player, clicked);
        }
        // Language Menu
        else if (title.equals(ChatColor.DARK_GRAY + "Language Settings")) {
            handleLanguageMenu(player, clicked);
        }
        // Message Packs Menu
        else if (title.equals(ChatColor.DARK_GRAY + "Message Packs")) {
            handleMessagePackMenu(player, clicked);
        }
        // Sound Settings Menu
        else if (title.equals(ChatColor.DARK_GRAY + "Sound Settings")) {
            handleSoundMenu(player, clicked, event.isRightClick());
        }
    }

    private boolean isPluginGUI(String title) {
        for (String guiTitle : GUI_TITLES) {
            if (guiTitle.equals(title)) {
                return true;
            }
        }
        return false;
    }

    private void handleMainMenu(Player player, ItemStack clicked) {
        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        ConfigManager config = plugin.getConfigManager();

        switch (itemName) {
            case "Join Message":
                logManager.logAdminAction(player, "Toggle Join Message", "Set to: " + !config.isJoinMessageEnabled());
                config.set("features.join-message", !config.isJoinMessageEnabled());
                playToggleSound(player);
                break;
            case "Leave Message":
                logManager.logAdminAction(player, "Toggle Leave Message", "Set to: " + !config.isLeaveMessageEnabled());
                config.set("features.leave-message", !config.isLeaveMessageEnabled());
                playToggleSound(player);
                break;
            case "Join Title":
                logManager.logAdminAction(player, "Toggle Join Title", "Set to: " + !config.isJoinTitleEnabled());
                config.set("features.join-title", !config.isJoinTitleEnabled());
                playToggleSound(player);
                break;
            case "Join Sound":
                logManager.logAdminAction(player, "Toggle Join Sound", "Set to: " + !config.isJoinSoundEnabled());
                config.set("features.join-sound", !config.isJoinSoundEnabled());
                playToggleSound(player);
                break;
            case "Leave Sound":
                logManager.logAdminAction(player, "Toggle Leave Sound", "Set to: " + !config.isLeaveSoundEnabled());
                config.set("features.leave-sound", !config.isLeaveSoundEnabled());
                playToggleSound(player);
                break;
            case "Message Pack":
                new MessagePackGUI(plugin).openMenu(player);
                return;
            case "Language":
                new LanguageGUI(plugin).openMenu(player);
                return;
            case "Sound Settings":
                new SoundSettingsGUI(plugin).openMenu(player);
                return;
        }
        new SettingsGUI(plugin).openMainMenu(player);
    }

    private void handleLanguageMenu(Player player, ItemStack clicked) {
        String lang = ChatColor.stripColor(clicked.getItemMeta().getDisplayName()).toLowerCase();
        if (lang.equals("español")) lang = "spanish";
        if (plugin.getLanguageManager().setLanguage(lang)) {
            logManager.logAdminAction(player, "Change Language", "Set to: " + lang);
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        }
        new SettingsGUI(plugin).openMainMenu(player);
    }

    private void handleMessagePackMenu(Player player, ItemStack clicked) {
        ItemMeta meta = clicked.getItemMeta();
        if (meta == null) {
            return;
        }

        String strippedName = ChatColor.stripColor(meta.getDisplayName());
        if ("Back".equalsIgnoreCase(strippedName)) {
            new SettingsGUI(plugin).openMainMenu(player);
            return;
        }

        String pack = null;
        PersistentDataContainer container = meta.getPersistentDataContainer();
        if (container.has(packKey, PersistentDataType.STRING)) {
            pack = container.get(packKey, PersistentDataType.STRING);
        }

        if (pack == null || pack.isEmpty()) {
            pack = strippedName.toLowerCase().replace(" pack", "").replace(' ', '-');
        }

        plugin.getConfigManager().setMessagePack(pack);
        logManager.logAdminAction(player, "Change Message Pack", "Set to: " + pack);
        player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1.0f, 2.0f);
        new SettingsGUI(plugin).openMainMenu(player);
    }

    private void handleSoundMenu(Player player, ItemStack clicked, boolean isRightClick) {
        String itemName = ChatColor.stripColor(clicked.getItemMeta().getDisplayName());
        ConfigManager config = plugin.getConfigManager();

        switch (itemName) {
            case "Join Sound":
                logManager.logAdminAction(player, "Toggle Join Sound", "Set to: " + !config.isJoinSoundEnabled());
                // Cycle through available sounds
                Sound currentJoinSound = config.getJoinSound();
                Sound[] sounds = Sound.values();
                int index = 0;
                for (int i = 0; i < sounds.length; i++) {
                    if (sounds[i] == currentJoinSound) {
                        index = isRightClick ? (i - 1 + sounds.length) % sounds.length : (i + 1) % sounds.length;
                        break;
                    }
                }
                config.set("sounds.join.sound", sounds[index].name());
                logManager.logAdminAction(player, "Change Join Sound", "Set to: " + sounds[index].name());
                player.playSound(player.getLocation(), sounds[index], 1.0f, 1.0f);
                break;

            case "Leave Sound":
                logManager.logAdminAction(player, "Toggle Leave Sound", "Set to: " + !config.isLeaveSoundEnabled());
                // Similar sound cycling for leave sound
                Sound currentLeaveSound = config.getLeaveSound();
                Sound[] leaveSounds = Sound.values();
                int leaveIndex = 0;
                for (int i = 0; i < leaveSounds.length; i++) {
                    if (leaveSounds[i] == currentLeaveSound) {
                        leaveIndex = isRightClick ? (i - 1 + leaveSounds.length) % leaveSounds.length : (i + 1) % leaveSounds.length;
                        break;
                    }
                }
                config.set("sounds.leave.sound", leaveSounds[leaveIndex].name());
                logManager.logAdminAction(player, "Change Leave Sound", "Set to: " + leaveSounds[leaveIndex].name());
                player.playSound(player.getLocation(), leaveSounds[leaveIndex], 1.0f, 1.0f);
                break;

            case "Join Volume":
                float joinVolume = config.getJoinSoundVolume();
                joinVolume += isRightClick ? -0.1f : 0.1f;
                joinVolume = Math.max(0.0f, Math.min(2.0f, joinVolume));
                config.set("sounds.join.volume", joinVolume);
                logManager.logAdminAction(player, "Change Join Sound Volume", "Set to: " + joinVolume);
                player.playSound(player.getLocation(), config.getJoinSound(), joinVolume, 1.0f);
                break;

            case "Leave Volume":
                float leaveVolume = config.getLeaveSoundVolume();
                leaveVolume += isRightClick ? -0.1f : 0.1f;
                leaveVolume = Math.max(0.0f, Math.min(2.0f, leaveVolume));
                config.set("sounds.leave.volume", leaveVolume);
                logManager.logAdminAction(player, "Change Leave Sound Volume", "Set to: " + leaveVolume);
                player.playSound(player.getLocation(), config.getLeaveSound(), leaveVolume, 1.0f);
                break;

            case "Join Pitch":
                float joinPitch = config.getJoinSoundPitch();
                joinPitch += isRightClick ? -0.1f : 0.1f;
                joinPitch = Math.max(0.5f, Math.min(2.0f, joinPitch));
                config.set("sounds.join.pitch", joinPitch);
                logManager.logAdminAction(player, "Change Join Sound Pitch", "Set to: " + joinPitch);
                player.playSound(player.getLocation(), config.getJoinSound(), 1.0f, joinPitch);
                break;

            case "Preview Sounds":
                // Preview current sound settings
                player.playSound(player.getLocation(), config.getJoinSound(), 
                    config.getJoinSoundVolume(), config.getJoinSoundPitch());
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    player.playSound(player.getLocation(), config.getLeaveSound(), 
                        config.getLeaveSoundVolume(), 1.0f);
                }, 20L);
                break;

            case "Back":
                new SettingsGUI(plugin).openMainMenu(player);
                return;
        }
        new SoundSettingsGUI(plugin).openMenu(player);
    }

    private void playToggleSound(Player player) {
        player.playSound(player.getLocation(), Sound.BLOCK_LEVER_CLICK, 0.5f, 1.0f);
    }
}