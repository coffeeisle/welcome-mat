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
import java.util.Arrays;
import java.util.List;

public class SettingsGUI {
    private final WelcomeMat plugin;
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "WelcomeMat Settings";

    public SettingsGUI(WelcomeMat plugin) {
        this.plugin = plugin;
    }

    public void openMainMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 54, GUI_TITLE);

        // Section headers
        inv.setItem(0, createHeader("Features"));
        inv.setItem(9, createHeader("Packs & Language"));
        inv.setItem(18, createHeader("Sound & Effects"));
        inv.setItem(27, createHeader("Personal"));
        inv.setItem(45, GUIUtils.createBackButton());

        // Features toggles (row 1)
        inv.setItem(1, createToggleItem("Join Message",
            plugin.getConfigManager().isJoinMessageEnabled(), Material.PAPER));
        inv.setItem(2, createToggleItem("Leave Message",
            plugin.getConfigManager().isLeaveMessageEnabled(), Material.MAP));
        inv.setItem(3, createToggleItem("Join Title",
            plugin.getConfigManager().isJoinTitleEnabled(), Material.NAME_TAG));
        inv.setItem(4, createToggleItem("Join Sound",
            plugin.getConfigManager().isJoinSoundEnabled(), Material.NOTE_BLOCK));
        inv.setItem(5, createToggleItem("Leave Sound",
            plugin.getConfigManager().isLeaveSoundEnabled(), Material.JUKEBOX));

        // Packs & Language (row 2)
        inv.setItem(10, createMenuItem("Message Pack",
            Material.BOOK,
            Arrays.asList(
                ChatColor.GRAY + "Current: " + plugin.getConfigManager().getCurrentMessagePack(),
                "",
                ChatColor.YELLOW + "Click to change"
            )));
        inv.setItem(11, createMenuItem("Language",
            Material.WRITABLE_BOOK,
            Arrays.asList(
                ChatColor.GRAY + "Current: " + plugin.getLanguageManager().getCurrentLanguage(),
                "",
                ChatColor.YELLOW + "Click to change"
            )));
        inv.setItem(12, createMenuItem("Animations",
            Material.FIREWORK_ROCKET,
            Arrays.asList(
                ChatColor.GRAY + "Current: " + plugin.getConfig().getString("effects.type", "fire_spiral"),
                "",
                ChatColor.YELLOW + "Click to change"
            )));

        // Sound & Effects (row 3)
        inv.setItem(19, createMenuItem("Sound Settings",
            Material.MUSIC_DISC_CAT,
            Arrays.asList(
                ChatColor.YELLOW + "Click to configure:",
                ChatColor.GRAY + "• Join/Leave sounds",
                ChatColor.GRAY + "• Volume",
                ChatColor.GRAY + "• Pitch"
            )));
        if (plugin.getConfig().getBoolean("effects.enabled")) {
            inv.setItem(20, createToggleItem("Join Effects",
                plugin.getDatabaseManager().getEffectPreference(player.getUniqueId()),
                Material.FIREWORK_ROCKET));
        }

        // Personal (row 4)
        if (plugin.getConfigManager().isPlayerToggleAllowed()) {
            inv.setItem(28, createToggleItem("WelcomeMat",
                plugin.getDatabaseManager().getPluginEnabled(player.getUniqueId()),
                Material.LEVER));
        }

        player.openInventory(inv);
    }

    private ItemStack createHeader(String name) {
        ItemStack item = new ItemStack(Material.BLACK_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.GOLD + "§l" + name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createToggleItem(String name, boolean enabled, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(ChatColor.YELLOW + name);
            List<String> lore = new ArrayList<>();
            lore.add("");
            lore.add(enabled ? ChatColor.GREEN + "✔ Enabled" : ChatColor.RED + "✘ Disabled");
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
            meta.setDisplayName(ChatColor.AQUA + name);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    protected ItemStack createBackButton() {
        return GUIUtils.createBackButton();
    }
}