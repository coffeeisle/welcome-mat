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
import xyz.coffeeisle.welcomemat.gui.GUIUtils;

import java.util.Arrays;

public class LanguageGUI {
    private final WelcomeMat plugin;
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "Language Settings";

    public LanguageGUI(WelcomeMat plugin) {
        this.plugin = plugin;
    }

    public void openMenu(Player player) {
        Inventory inv = Bukkit.createInventory(null, 9, GUI_TITLE);

        // English option
        ItemStack english = new ItemStack(Material.BOOK);
        ItemMeta englishMeta = english.getItemMeta();
        englishMeta.setDisplayName(ChatColor.YELLOW + "English");
        englishMeta.setLore(Arrays.asList(
            plugin.getLanguageManager().getCurrentLanguage().equals("english") 
                ? ChatColor.GREEN + "Currently Selected" 
                : ChatColor.GRAY + "Click to select"
        ));
        english.setItemMeta(englishMeta);
        inv.setItem(3, english);

        // Spanish option
        ItemStack spanish = new ItemStack(Material.BOOK);
        ItemMeta spanishMeta = spanish.getItemMeta();
        spanishMeta.setDisplayName(ChatColor.YELLOW + "Español");
        spanishMeta.setLore(Arrays.asList(
            plugin.getLanguageManager().getCurrentLanguage().equals("spanish") 
                ? ChatColor.GREEN + "Currently Selected" 
                : ChatColor.GRAY + "Click to select"
        ));
        spanish.setItemMeta(spanishMeta);
        inv.setItem(5, spanish);

        // Add back button
        inv.setItem(8, GUIUtils.createBackButton());

        player.openInventory(inv);
    }
} 