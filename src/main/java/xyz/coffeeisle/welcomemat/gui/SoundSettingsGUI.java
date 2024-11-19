package xyz.coffeeisle.welcomemat.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import xyz.coffeeisle.welcomemat.WelcomeMat;

import java.util.Arrays;

public class SoundSettingsGUI {
    private final WelcomeMat plugin;
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "Sound Settings";

    public SoundSettingsGUI(WelcomeMat plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Join Sound selector
        inv.setItem(11, createSoundItem("Join Sound", 
            plugin.getConfigManager().getJoinSound().name(),
            Material.NOTE_BLOCK));

        // Join Volume slider
        inv.setItem(12, createVolumeItem("Join Volume", 
            plugin.getConfigManager().getJoinSoundVolume()));

        // Join Pitch slider
        inv.setItem(13, createPitchItem("Join Pitch", 
            plugin.getConfigManager().getJoinSoundPitch()));

        // Leave Sound selector
        inv.setItem(14, createSoundItem("Leave Sound", 
            plugin.getConfigManager().getLeaveSound().name(),
            Material.NOTE_BLOCK));

        // Leave Volume slider
        inv.setItem(15, createVolumeItem("Leave Volume", 
            plugin.getConfigManager().getLeaveSoundVolume()));

        // Preview button
        inv.setItem(22, createPreviewButton());

        // Add back button
        inv.setItem(26, GUIUtils.createBackButton());

        player.openInventory(inv);
    }

    private ItemStack createSoundItem(String name, String current, Material material) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Current: " + current,
            "",
            ChatColor.YELLOW + "Click to change"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createVolumeItem(String name, float volume) {
        ItemStack item = new ItemStack(Material.LEVER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Current: " + volume,
            "",
            ChatColor.YELLOW + "Left-click: +0.1",
            ChatColor.YELLOW + "Right-click: -0.1"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPitchItem(String name, float pitch) {
        ItemStack item = new ItemStack(Material.REPEATER);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name);
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Current: " + pitch,
            "",
            ChatColor.YELLOW + "Left-click: +0.1",
            ChatColor.YELLOW + "Right-click: -0.1"
        ));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack createPreviewButton() {
        ItemStack item = new ItemStack(Material.JUKEBOX);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Preview Sounds");
        meta.setLore(Arrays.asList(
            ChatColor.GRAY + "Click to preview",
            ChatColor.GRAY + "current sound settings"
        ));
        item.setItemMeta(meta);
        return item;
    }
} 