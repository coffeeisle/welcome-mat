package xyz.coffeeisle.welcomemat.gui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.coffeeisle.welcomemat.WelcomeMat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MessagePackGUI {
    private static final String GUI_TITLE = ChatColor.DARK_GRAY + "Message Packs";
    private static final Map<String, Material> PACK_ICONS = new HashMap<>();
    private final WelcomeMat plugin;
    private final NamespacedKey packKey;

    static {
        PACK_ICONS.put("default", Material.PAPER);
        PACK_ICONS.put("friendly", Material.SUNFLOWER);
        PACK_ICONS.put("professional", Material.BOOK);
        PACK_ICONS.put("funny", Material.SLIME_BALL);
        PACK_ICONS.put("rpg", Material.DIAMOND_SWORD);
        PACK_ICONS.put("skyward", Material.GHAST_TEAR);
        PACK_ICONS.put("copper", Material.BRICK);
    }

    public MessagePackGUI(WelcomeMat plugin) {
        this.plugin = plugin;
        this.packKey = new NamespacedKey(plugin, "message_pack");
    }

    public void openMenu(Player player) {
        List<String> packIds = plugin.getConfigManager().getAvailableMessagePacks();
        String currentPack = plugin.getConfigManager().getCurrentMessagePack();

        int totalItems = packIds.size() + 1; // packs + back button
        int inventorySize = Math.max(9, ((totalItems + 8) / 9) * 9);
        Inventory inv = Bukkit.createInventory(null, inventorySize, GUI_TITLE);

        int slot = 0;
        for (String packId : packIds) {
            inv.setItem(slot++, createPackItem(packId, currentPack));
        }

        inv.setItem(inventorySize - 1, GUIUtils.createBackButton());
        player.openInventory(inv);
    }

    private ItemStack createPackItem(String id, String currentPack) {
        Material icon = PACK_ICONS.getOrDefault(id, Material.WRITABLE_BOOK);
        ItemStack item = new ItemStack(icon);
        ItemMeta meta = item.getItemMeta();

        if (meta != null) {
            String displayName = plugin.getLanguageManager().getMessagePackDisplayName(id);
            meta.setDisplayName(ChatColor.YELLOW + displayName);

            List<String> lore = new ArrayList<>();
            lore.add(id.equalsIgnoreCase(currentPack)
                ? ChatColor.GREEN + "Currently Selected"
                : ChatColor.GRAY + "Click to select");
            meta.setLore(lore);
            meta.getPersistentDataContainer().set(packKey, PersistentDataType.STRING, id);
            item.setItemMeta(meta);
        }

        return item;
    }
}