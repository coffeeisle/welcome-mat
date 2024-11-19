package xyz.coffeeisle.welcomemat.gui;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import java.util.Arrays;

public class GUIUtils {
    public static ItemStack createBackButton() {
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