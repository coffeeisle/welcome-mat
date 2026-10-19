package xyz.coffeeisle.welcomemat.gui;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.ChatColor;
import org.bukkit.persistence.PersistentDataType;
import xyz.coffeeisle.welcomemat.WelcomeMat;

import java.util.Arrays;
import java.util.List;

public class LanguageGUI {
    private final WelcomeMat plugin;
    private final NamespacedKey langKey;
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "Language Settings";

    public LanguageGUI(WelcomeMat plugin) {
        this.plugin = plugin;
        this.langKey = new NamespacedKey(plugin, "language");
    }

    public void openMenu(Player player) {
        List<String> languages = plugin.getLanguageManager().getAvailableLanguages();
        int size = Math.max(9, ((languages.size() + 1 + 8) / 9) * 9);
        Inventory inv = Bukkit.createInventory(null, size, GUI_TITLE);

        int slot = 0;
        for (String langEntry : languages) {
            String id = langEntry.contains(" ") ? langEntry.substring(0, langEntry.indexOf(' ')) : langEntry;
            String name = langEntry.contains("(") ? langEntry.substring(langEntry.indexOf('(') + 1, langEntry.lastIndexOf(')')) : id;

            ItemStack item = new ItemStack(Material.BOOK);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(ChatColor.YELLOW + name);
                meta.setLore(Arrays.asList(
                    plugin.getLanguageManager().getCurrentLanguage().equalsIgnoreCase(id)
                        ? ChatColor.GREEN + "Currently Selected"
                        : ChatColor.GRAY + "Click to select"
                ));
                meta.getPersistentDataContainer().set(langKey, PersistentDataType.STRING, id);
                item.setItemMeta(meta);
            }
            inv.setItem(slot++, item);
        }

        inv.setItem(size - 1, GUIUtils.createBackButton());
        player.openInventory(inv);
    }

    public NamespacedKey getLangKey() {
        return langKey;
    }
}