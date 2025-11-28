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

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

public class SplashEditorManager implements Listener {
    public enum SplashField {
        TITLE,
        SUBTITLE
    }

    private final WelcomeMat plugin;
    private final Map<UUID, SplashField> activeEdits = new HashMap<>();
    private final Map<UUID, ItemStack> heldItemBackups = new HashMap<>();
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
            restoreHeldItem(player);
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

        restoreHeldItem(player);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID uuid = event.getPlayer().getUniqueId();
        restoreHeldItem(event.getPlayer());
        activeEdits.remove(uuid);
    }

    private boolean isSplashEditorBook(BookMeta meta) {
        return meta != null && meta.getPersistentDataContainer().has(editorKey, PersistentDataType.STRING);
    }

    private void openVirtualEditor(Player player, ItemStack editorBook) {
        LanguageManager lang = plugin.getLanguageManager();
        UUID uuid = player.getUniqueId();
        ItemStack original = player.getInventory().getItemInMainHand();
        ItemStack originalCopy = original == null ? new ItemStack(Material.AIR) : original.clone();

        heldItemBackups.put(uuid, originalCopy);
        player.getInventory().setItemInMainHand(editorBook);
        player.updateInventory();

        if (sendOpenBookPacket(player)) {
            return;
        }

        // Fallback: return their original item immediately and hand them the editor book manually.
        heldItemBackups.remove(uuid);
        player.getInventory().setItemInMainHand(originalCopy);
        player.updateInventory();
        giveManualEditorBook(player, editorBook);
        player.sendMessage(lang.getMessage("splash.editor_manual_fallback"));
    }

    private boolean sendOpenBookPacket(Player player) {
        try {
            Method getHandle = player.getClass().getMethod("getHandle");
            Object serverPlayer = getHandle.invoke(player);
            Field connectionField = serverPlayer.getClass().getField("connection");
            Object connection = connectionField.get(serverPlayer);

            Class<?> interactionHandClass = Class.forName("net.minecraft.world.InteractionHand");
            @SuppressWarnings("unchecked")
            Object mainHand = Enum.valueOf((Class<Enum>) interactionHandClass, "MAIN_HAND");

            Class<?> packetClass = Class.forName("net.minecraft.network.protocol.game.ClientboundOpenBookPacket");
            Constructor<?> packetConstructor = packetClass.getConstructor(interactionHandClass);
            Object packet = packetConstructor.newInstance(mainHand);

            Class<?> packetParent = Class.forName("net.minecraft.network.protocol.Packet");
            Method sendMethod = connection.getClass().getMethod("send", packetParent);
            sendMethod.invoke(connection, packet);
            return true;
        } catch (Exception ex) {
            plugin.getLogger().log(Level.WARNING, "Failed to open virtual splash editor for " + player.getName(), ex);
            return false;
        }
    }

    private void giveManualEditorBook(Player player, ItemStack editorBook) {
        Map<Integer, ItemStack> leftovers = player.getInventory().addItem(editorBook);
        if (!leftovers.isEmpty()) {
            leftovers.values().forEach(item -> player.getWorld().dropItemNaturally(player.getLocation(), item));
        }
    }

    private void restoreHeldItem(Player player) {
        UUID uuid = player.getUniqueId();
        ItemStack original = heldItemBackups.remove(uuid);
        if (original == null) {
            return;
        }

        player.getInventory().setItemInMainHand(original);
        player.updateInventory();
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
