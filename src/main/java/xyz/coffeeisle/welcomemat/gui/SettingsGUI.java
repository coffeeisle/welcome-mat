package xyz.coffeeisle.welcomemat.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import xyz.coffeeisle.welcomemat.WelcomeMat;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;

public class SettingsGUI {
    private final WelcomeMat plugin;
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "WelcomeMat Settings";

    public SettingsGUI(WelcomeMat plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Features toggle section
        inv.setItem(10, createToggleItem("Join Message", 
            plugin.getConfigManager().isJoinMessageEnabled(), Material.PAPER));
        inv.setItem(11, createToggleItem("Leave Message", 
            plugin.getConfigManager().isLeaveMessageEnabled(), Material.MAP));
        inv.setItem(12, createToggleItem("Join Title", 
            plugin.getConfigManager().isJoinTitleEnabled(), Material.NAME_TAG));
        inv.setItem(13, createToggleItem("Join Sound", 
            plugin.getConfigManager().isJoinSoundEnabled(), Material.NOTE_BLOCK));
        inv.setItem(14, createToggleItem("Leave Sound", 
            plugin.getConfigManager().isLeaveSoundEnabled(), Material.JUKEBOX));
        
        // Message pack selector
        inv.setItem(15, createMenuItem("Message Pack", 
            Material.BOOK, 
            Arrays.asList(
                ChatColor.GRAY + "Current: " + plugin.getConfigManager().getCurrentMessagePack(),
                "",
                ChatColor.YELLOW + "Click to change"
            )));

        // Language selector
        inv.setItem(16, createMenuItem("Language", 
            Material.WRITABLE_BOOK,
            Arrays.asList(
                ChatColor.GRAY + "Current: " + plugin.getLanguageManager().getCurrentLanguage(),
                "",
                ChatColor.YELLOW + "Click to change"
            )));

        // Sound settings
        inv.setItem(21, createMenuItem("Sound Settings", 
            Material.MUSIC_DISC_CAT,
            Arrays.asList(
                ChatColor.YELLOW + "Click to configure:",
                ChatColor.GRAY + "• Join/Leave sounds",
                ChatColor.GRAY + "• Volume",
                ChatColor.GRAY + "• Pitch"
            )));

        // Join Effects toggle
        if (plugin.getConfig().getBoolean("effects.enabled")) {
            inv.setItem(16, createToggleItem("Join Effects", 
                plugin.getDatabaseManager().getEffectPreference(player.getUniqueId()), 
                Material.FIREWORK_ROCKET));
        }

        player.openInventory(inv);
    }

    private ItemStack createToggleItem(String name, boolean enabled, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(enabled ? ChatColor.GREEN + "Enabled" : ChatColor.RED + "Disabled");
            lore.add("");
            lore.add(ChatColor.GRAY + "Click to toggle");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createMenuItem(String name, Material material, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.RED + "Back");
            meta.setLore(Arrays.asList(ChatColor.GRAY + "Return to main menu"));
            item.setItemMeta(meta);
        }
        return item;
    }
} 