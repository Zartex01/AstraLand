package com.astraland.pvpfactions.shop;

import com.astraland.pvpfactions.PvpFactions;
import com.astraland.pvpfactions.managers.KitManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class KitGUI implements InventoryHolder {

    private static final int[] KIT_SLOTS = {10, 11, 12, 13, 14, 15, 16, 19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34};

    private final Inventory inv;
    private final PvpFactions plugin;
    private final Map<Integer, String> kitAtSlot = new HashMap<>();

    public KitGUI(Player player, PvpFactions plugin) {
        this.plugin = plugin;
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &a&lKITS &8&l✦ &7PvP/Factions"));
        build(player);
    }

    private void build(Player player) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.GREEN_STAINED_GLASS_PANE, " ");

        for (int i = 0; i < 54; i++) inv.setItem(i, border);
        for (int i = 0; i < 9; i++) inv.setItem(i, accent);
        for (int i = 45; i < 54; i++) inv.setItem(i, accent);
        for (int r = 1; r <= 4; r++) {
            inv.setItem(r * 9, border);
            inv.setItem(r * 9 + 8, border);
        }

        inv.setItem(4, makeHeader());

        KitManager km = plugin.getKitManager();
        Set<String> kitNames = km.getKitNames();
        List<String> sorted = new ArrayList<>(kitNames);
        sorted.sort(String::compareToIgnoreCase);

        int slotIdx = 0;
        for (String kitName : sorted) {
            if (slotIdx >= KIT_SLOTS.length) break;
            long remaining = km.getCooldownRemaining(player.getUniqueId(), kitName);
            String perm = plugin.getConfig().getString("kits." + kitName + ".permission", "");
            boolean hasPerm = perm.isEmpty() || player.hasPermission(perm);

            inv.setItem(KIT_SLOTS[slotIdx], makeKitItem(kitName, remaining, hasPerm));
            kitAtSlot.put(KIT_SLOTS[slotIdx], kitName);
            slotIdx++;
        }

        if (sorted.isEmpty()) {
            ItemStack noKit = new ItemStack(Material.BARRIER);
            ItemMeta m = noKit.getItemMeta();
            m.setDisplayName(c("&cAucun kit disponible"));
            m.setLore(List.of(c("&7Configure les kits dans config.yml")));
            noKit.setItemMeta(m);
            inv.setItem(22, noKit);
        }
    }

    private ItemStack makeKitItem(String kitName, long remainingSec, boolean hasPerm) {
        String display = c(plugin.getConfig().getString("kits." + kitName + ".display", "&f" + kitName));
        long cooldown = plugin.getConfig().getLong("kits." + kitName + ".cooldown", 3600);

        Material icon = getMaterialForKit(kitName);

        ItemStack item = new ItemStack(icon);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(display);

        List<String> lore = new ArrayList<>();
        lore.add(c("&7Kit : &f" + kitName.toLowerCase()));
        lore.add(c("&7Cooldown : &e" + formatTime(cooldown)));
        lore.add("");

        if (!hasPerm) {
            m.setDisplayName(c("&8" + ChatColor.stripColor(display) + " &8[Verrouillé]"));
            lore.add(c("&cPermission requise."));
            lore.add(c("&7Tu ne peux pas utiliser ce kit."));
        } else if (remainingSec > 0) {
            lore.add(c("&cDisponible dans : &e" + formatTime(remainingSec)));
            lore.add(c("&7Reviens plus tard !"));
        } else {
            lore.add(c("&a✔ Disponible !"));
            lore.add(c("&7▶ &fClic pour obtenir le kit"));
        }

        m.setLore(lore);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
        item.setItemMeta(m);
        return item;
    }

    private Material getMaterialForKit(String kitName) {
        String lower = kitName.toLowerCase();
        if (lower.contains("diamond") || lower.contains("diamant")) return Material.DIAMOND_CHESTPLATE;
        if (lower.contains("netherite") || lower.contains("nether")) return Material.NETHERITE_CHESTPLATE;
        if (lower.contains("gold") || lower.contains("or")) return Material.GOLDEN_CHESTPLATE;
        if (lower.contains("iron") || lower.contains("fer")) return Material.IRON_CHESTPLATE;
        if (lower.contains("archer") || lower.contains("arc")) return Material.BOW;
        if (lower.contains("mage") || lower.contains("wizard")) return Material.BLAZE_ROD;
        if (lower.contains("bomber")) return Material.TNT;
        if (lower.contains("starter") || lower.contains("depart")) return Material.WOODEN_SWORD;
        return Material.CHEST;
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        String kitName = kitAtSlot.get(slot);
        if (kitName == null) return;

        KitManager km = plugin.getKitManager();
        String perm = plugin.getConfig().getString("kits." + kitName + ".permission", "");
        if (!perm.isEmpty() && !player.hasPermission(perm)) {
            player.sendMessage(c("&c✗ Tu n'as pas la permission d'utiliser ce kit."));
            return;
        }

        long remaining = km.getCooldownRemaining(player.getUniqueId(), kitName);
        if (remaining > 0) {
            player.sendMessage(c("&c✗ Ce kit sera disponible dans &e" + formatTime(remaining) + "&c."));
            return;
        }

        if (km.giveKit(player, kitName)) {
            String display = c(plugin.getConfig().getString("kits." + kitName + ".display", kitName));
            player.sendMessage(c("&a✔ Kit " + display + " &aobtenu !"));
            player.playSound(player.getLocation(), org.bukkit.Sound.ENTITY_PLAYER_LEVELUP, 0.6f, 1.0f);
            player.closeInventory();
        } else {
            player.sendMessage(c("&c✗ Erreur lors de l'obtention du kit."));
        }
    }

    private ItemStack makeHeader() {
        ItemStack item = new ItemStack(Material.COMPASS);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&a&l✦ Kits PvP/Factions ✦"));
        m.setLore(List.of(
            c("&7Clique sur un kit pour l'obtenir."),
            c("&7Chaque kit a un cooldown de recharge."),
            c(""),
            c("&e⚠ L'inventaire sera vidé avant le kit !")
        ));
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(m);
        return item;
    }

    private String formatTime(long seconds) {
        if (seconds >= 3600) return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "min";
        if (seconds >= 60) return (seconds / 60) + "min " + (seconds % 60) + "s";
        return seconds + "s";
    }

    private ItemStack glass(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta m = item.getItemMeta();
        m.setDisplayName(name);
        item.setItemMeta(m);
        return item;
    }

    public void open(Player p) { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }
}
