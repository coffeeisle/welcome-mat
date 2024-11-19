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

public class MessagePackGUI {
    private final WelcomeMat plugin;
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "Message Packs";

    public MessagePackGUI(WelcomeMat plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, GUI_TITLE);

        String currentPack = plugin.getConfigManager().getCurrentMessagePack();

        // Default pack
        inv.setItem(2, createPackItem("default", "Default", Material.PAPER, currentPack));
        
        // Fun pack
        inv.setItem(4, createPackItem("fun", "Fun", Material.NOTE_BLOCK, currentPack));
        
        // RPG pack
        inv.setItem(6, createPackItem("rpg", "RPG", Material.DIAMOND_SWORD, currentPack));

        // Add back button
        inv.setItem(8, GUIUtils.createBackButton());

        player.openInventory(inv);
    }

    private ItemStack createPackItem(String id, String name, Material material, String currentPack) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(ChatColor.YELLOW + name + " Pack");
        meta.setLore(Arrays.asList(
            id.equals(currentPack) 
                ? ChatColor.GREEN + "Currently Selected" 
                : ChatColor.GRAY + "Click to select"
        ));
        item.setItemMeta(meta);
        return item;
    }
} 