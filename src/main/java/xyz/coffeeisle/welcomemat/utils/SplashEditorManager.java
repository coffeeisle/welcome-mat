package xyz.coffeeisle.welcomemat.utils;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerEditBookEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BookMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.coffeeisle.welcomemat.ConfigManager;
import xyz.coffeeisle.welcomemat.LanguageManager;
import xyz.coffeeisle.welcomemat.WelcomeMat;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SplashEditorManager implements Listener {
    public enum SplashField {
        TITLE,
        SUBTITLE
    }

    private final WelcomeMat plugin;
    private final Map<UUID, SplashField> activeEdits = new HashMap<>();
    private final NamespacedKey editorKey;

    public SplashEditorManager(WelcomeMat plugin) {
        this.plugin = plugin;
        this.editorKey = new NamespacedKey(plugin, "splash-editor");
    }

    public boolean beginEditing(Player player, SplashField field) {
        UUID uuid = player.getUniqueId();
        LanguageManager lang = plugin.getLanguageManager();

        if (activeEdits.containsKey(uuid)) {
            player.sendMessage(lang.getMessage("splash.edit_in_progress"));
            return false;
        }

        ItemStack editorBook = createEditorBook(field);
        if (!giveEditorBook(player, editorBook)) {
            player.sendMessage(lang.getMessage("splash.inventory_full"));
            return false;
        }

        activeEdits.put(uuid, field);
        player.sendMessage(lang.getMessage(field == SplashField.TITLE ?
            "splash.started_title" : "splash.started_subtitle"));
        return true;
    }

    private ItemStack createEditorBook(SplashField field) {
        ItemStack book = new ItemStack(Material.WRITABLE_BOOK);
        BookMeta meta = (BookMeta) book.getItemMeta();
        if (meta == null) {
            return book;
        }

        ConfigManager config = plugin.getConfigManager();
        String currentValue = field == SplashField.TITLE
            ? config.getRawJoinTitle()
            : config.getRawJoinSubtitle();
        if (currentValue == null) {
            currentValue = "";
        }

        LanguageManager lang = plugin.getLanguageManager();
        String instructionsKey = field == SplashField.TITLE
            ? "splash.editor_instructions_title"
            : "splash.editor_instructions_subtitle";

        String firstPage = currentValue.isEmpty() ? " " : currentValue;
        meta.setDisplayName(ChatColor.GOLD + "Splash " + field.name().toLowerCase());
        meta.setPages(firstPage, lang.getMessage(instructionsKey));
        meta.getPersistentDataContainer().set(editorKey, PersistentDataType.STRING, field.name());
        book.setItemMeta(meta);
        return book;
    }

    @EventHandler
    public void onBookEdit(PlayerEditBookEvent event) {
        BookMeta meta = event.getNewBookMeta();
        if (!isSplashEditorBook(meta)) {
            return;
        }

        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        SplashField field = activeEdits.remove(uuid);
        if (field == null) {
            return;
        }

        event.setCancelled(true);

        String newValue = extractFirstPage(meta);
        if (newValue.isEmpty()) {
            player.sendMessage(plugin.getLanguageManager().getMessage("splash.empty_value"));
            return;
        }

        ConfigManager config = plugin.getConfigManager();
        if (field == SplashField.TITLE) {
            config.updateJoinTitle(newValue);
        } else {
            config.updateJoinSubtitle(newValue);
        }
        config.loadConfig();

        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("value", ChatColor.translateAlternateColorCodes('&', newValue));
        player.sendMessage(plugin.getLanguageManager().getMessage(
            field == SplashField.TITLE ? "splash.updated_title" : "splash.updated_subtitle",
            placeholders
        ));
        removeEditorBook(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        activeEdits.remove(uuid);
        removeEditorBook(event.getPlayer());
    }

    private boolean isSplashEditorBook(BookMeta meta) {
        return meta != null && meta.getPersistentDataContainer().has(editorKey, PersistentDataType.STRING);
    }

    private boolean isSplashEditorBook(ItemStack itemStack) {
        if (itemStack == null || itemStack.getType() != Material.WRITABLE_BOOK) {
            return false;
        }

        if (!(itemStack.getItemMeta() instanceof BookMeta)) {
            return false;
        }

        return isSplashEditorBook((BookMeta) itemStack.getItemMeta());
    }

    private boolean giveEditorBook(Player player, ItemStack editorBook) {
        ItemStack current = player.getInventory().getItemInMainHand();
        if (current == null || current.getType().isAir()) {
            player.getInventory().setItemInMainHand(editorBook);
            player.updateInventory();
            return true;
        }

        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(editorBook);
        boolean success = leftovers.isEmpty();
        if (!success) {
            // Nothing was added because the inventory was full.
            return false;
        }

        player.updateInventory();
        return true;
    }

    private void removeEditorBook(Player player) {
        ItemStack[] contents = player.getInventory().getContents();
        for (int slot = 0; slot < contents.length; slot++) {
            ItemStack stack = contents[slot];
            if (!isSplashEditorBook(stack)) {
                continue;
            }

            if (stack.getAmount() <= 1) {
                player.getInventory().setItem(slot, null);
            } else {
                stack.setAmount(stack.getAmount() - 1);
                player.getInventory().setItem(slot, stack);
            }

            player.updateInventory();
            return;
        }
    }

    private String extractFirstPage(BookMeta meta) {
        if (meta == null || meta.getPageCount() == 0) {
            return "";
        }

        String page = meta.getPage(1);
        if (page == null) {
            return "";
        }

        String sanitized = page.replace('\r', ' ').replace('\n', ' ').trim();
        return sanitized.replace('§', '&');
    }
}
