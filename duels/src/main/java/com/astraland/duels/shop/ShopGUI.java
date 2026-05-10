package com.astraland.duels.shop;

import com.astraland.duels.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopGUI implements InventoryHolder {

    private final Inventory inv;
    private final Map<Integer, ShopEntry> entries = new HashMap<>();
    private record ShopEntry(int price, ItemStack[] reward) {}

    public ShopGUI() {
        inv = Bukkit.createInventory(this, 54,
            ChatColor.translateAlternateColorCodes('&', "&5&l⚡ &dShop Duels &5&l⚡"));
        build();
    }

    private void build() {
        ItemStack border = border(Material.MAGENTA_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, border); inv.setItem(r * 9 + 8, border); }
        inv.setItem(4, header(Material.DIAMOND_SWORD, "&d&lDuels Shop", "&7Prépare-toi avant ton duel !"));

        /* ── Consommables Combat ── */
        inv.setItem(10, section("&d&lCOMBAT"));
        set(11, d(Material.GOLDEN_APPLE, "&6Pomme Dorée", 200, "&7Régén + Absorption en combat"), 200, new ItemStack(Material.GOLDEN_APPLE));
        set(12, d(Material.ENCHANTED_GOLDEN_APPLE, "&dPomme Enchantée", 800, "&7Puissance maximale !"), 800, new ItemStack(Material.ENCHANTED_GOLDEN_APPLE));
        set(13, d(Material.MUSHROOM_STEW, "&6Soupe de Champignons ×8", 60, "&7Kit Soup — 8 soupes"), 60, new ItemStack(Material.MUSHROOM_STEW, 8));
        set(14, strengthDisplay(150), 150, strengthPot());
        set(15, speedDisplay(120), 120, speedPot());

        /* ── Équipements ── */
        inv.setItem(19, section("&d&lÉQUIPEMENTS"));
        set(20, d(Material.IRON_SWORD, "&fÉpée de Fer Sharp.II", 80, "&7Tranchant II"), 80,
            ench(new ItemStack(Material.IRON_SWORD), Enchantment.SHARPNESS, 2));
        set(21, d(Material.DIAMOND_SWORD, "&bÉpée Diamant Sharp.III", 250, "&7Tranchant III"), 250,
            ench(new ItemStack(Material.DIAMOND_SWORD), Enchantment.SHARPNESS, 3));
        set(22, d(Material.DIAMOND_CHESTPLATE, "&bKit Diamant Prot.II", 300, "&7Protection II"), 300,
            ench(new ItemStack(Material.DIAMOND_HELMET), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.DIAMOND_CHESTPLATE), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.DIAMOND_LEGGINGS), Enchantment.PROTECTION, 2),
            ench(new ItemStack(Material.DIAMOND_BOOTS), Enchantment.PROTECTION, 2));
        set(23, d(Material.BOW, "&aArc Puissance II + Recul II", 150, "&7Puissance II, Recul II"), 150,
            ench(ench(new ItemStack(Material.BOW), Enchantment.POWER, 2), Enchantment.PUNCH, 2));
        set(24, d(Material.ARROW, "&eFlèches ×32", 30), 30, new ItemStack(Material.ARROW, 32));

        /* ── Utilitaires ── */
        inv.setItem(28, section("&d&lUTILITAIRES"));
        set(29, d(Material.ENDER_PEARL, "&5Perle d'Ender ×4", 80, "&7Téléportation tactique"), 80, new ItemStack(Material.ENDER_PEARL, 4));
        set(30, d(Material.COOKED_BEEF, "&eSteak ×16", 40), 40, new ItemStack(Material.COOKED_BEEF, 16));
        set(31, d(Material.TOTEM_OF_UNDYING, "&dTotem", 700, "&7Une chance supplémentaire"), 700, new ItemStack(Material.TOTEM_OF_UNDYING));
        set(32, d(Material.SPLASH_POTION, "&cPotion Dégâts II (Splash)", 100, "&7Dégâts instantanés II"), 100, damagePot());
    }

    private ItemStack strengthPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.STRENGTH, 2400, 1), true); pot.setItemMeta(m); return pot;
    }
    private ItemStack strengthDisplay(int price) { return d(Material.POTION, "&cPotion de Force II", price, "&7Force II 2min"); }
    private ItemStack speedPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 2400, 1), true); pot.setItemMeta(m); return pot;
    }
    private ItemStack speedDisplay(int price) { return d(Material.POTION, "&aPotion de Vitesse II", price, "&7Vitesse II 2min"); }
    private ItemStack damagePot() {
        ItemStack pot = new ItemStack(Material.SPLASH_POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.INSTANT_DAMAGE, 1, 1), true); pot.setItemMeta(m); return pot;
    }

    private void set(int slot, ItemStack display, int price, ItemStack... reward) {
        inv.setItem(slot, display); entries.put(slot, new ShopEntry(price, reward));
    }
    public void handleClick(InventoryClickEvent e, EconomyManager eco) {
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ShopEntry entry = entries.get(e.getSlot());
        if (entry == null) return;
        if (!eco.removeBalance(player.getUniqueId(), entry.price())) {
            player.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + entry.price() + " $&c.")); return;
        }
        for (ItemStack item : entry.reward()) if (item != null) player.getInventory().addItem(item.clone());
        player.sendMessage(c("&a✔ Achat pour &6" + entry.price() + " $&a."));
    }
    @Override public Inventory getInventory() { return inv; }
    public void open(Player p) { p.openInventory(inv); }
    private ItemStack d(Material mat, String name, int price, String... desc) {
        ItemStack item = new ItemStack(mat); ItemMeta meta = item.getItemMeta(); meta.setDisplayName(c(name));
        List<String> lore = new ArrayList<>(Arrays.stream(desc).map(this::c).toList());
        lore.add(""); lore.add(c("&6Prix : &e" + price + " $")); lore.add(c("&7▶ &fClic pour acheter"));
        meta.setLore(lore); meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); item.setItemMeta(meta); return item;
    }
    private ItemStack section(String name) { ItemStack i = new ItemStack(Material.GRAY_STAINED_GLASS_PANE); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); i.setItemMeta(m); return i; }
    private ItemStack header(Material mat, String name, String desc) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); m.setLore(List.of(c(desc))); m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); i.setItemMeta(m); return i; }
    private ItemStack border(Material mat) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(" "); i.setItemMeta(m); return i; }
    private ItemStack ench(ItemStack item, Enchantment e, int lvl) { item.addUnsafeEnchantment(e, lvl); return item; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
