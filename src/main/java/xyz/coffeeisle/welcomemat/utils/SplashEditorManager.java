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
        activeEdits.put(uuid, field);
        openVirtualEditor(player, editorBook);

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
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        if (!activeEdits.containsKey(uuid)) {
            return;
        }

        activeEdits.remove(uuid);
    }

    private boolean isSplashEditorBook(BookMeta meta) {
        return meta != null && meta.getPersistentDataContainer().has(editorKey, PersistentDataType.STRING);
    }

    private void openVirtualEditor(Player player, ItemStack editorBook) {
        ItemStack original = player.getInventory().getItemInMainHand();
        ItemStack originalCopy = original == null ? new ItemStack(Material.AIR) : original.clone();

        player.getInventory().setItemInMainHand(editorBook);
        player.openBook(editorBook);

        plugin.getServer().getScheduler().runTask(plugin, () -> {
            player.getInventory().setItemInMainHand(originalCopy);
        });
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
