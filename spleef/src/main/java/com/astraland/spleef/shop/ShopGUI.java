package com.astraland.spleef.shop;

import com.astraland.spleef.managers.EconomyManager;
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
            ChatColor.translateAlternateColorCodes('&', "&f&l❄ &bShop Spleef &f&l❄"));
        build();
    }

    private void build() {
        ItemStack border = border(Material.LIGHT_BLUE_STAINED_GLASS_PANE);
        for (int i = 0; i < 9; i++) inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);
        for (int r = 1; r <= 4; r++) { inv.setItem(r * 9, border); inv.setItem(r * 9 + 8, border); }
        inv.setItem(4, header(Material.SNOW_BLOCK, "&b&lSpleef Shop", "&7Améliore ta pelle et ta mobilité !"));

        /* ── Pelles ── */
        inv.setItem(10, section("&b&lPELLES"));
        set(11, d(Material.IRON_SHOVEL, "&fPelle de Fer Eff.II", 60, "&7Efficacité II"), 60,
            ench(new ItemStack(Material.IRON_SHOVEL), Enchantment.EFFICIENCY, 2));
        set(12, d(Material.DIAMOND_SHOVEL, "&bPelle Diamant Eff.III", 120, "&7Efficacité III"), 120,
            ench(new ItemStack(Material.DIAMOND_SHOVEL), Enchantment.EFFICIENCY, 3));
        set(13, d(Material.DIAMOND_SHOVEL, "&5Pelle Diamant Eff.V", 250, "&7Efficacité V + Sans Brisure III"), 250,
            ench(ench(new ItemStack(Material.DIAMOND_SHOVEL), Enchantment.EFFICIENCY, 5), Enchantment.UNBREAKING, 3));
        set(14, d(Material.NETHERITE_SHOVEL, "&dPelle Nétherite Ultime", 500, "&7Eff.V + Sans Brisure III + Soie Tactile"), 500,
            ench(ench(ench(new ItemStack(Material.NETHERITE_SHOVEL), Enchantment.EFFICIENCY, 5), Enchantment.UNBREAKING, 3), Enchantment.SILK_TOUCH, 1));

        /* ── Bottes ── */
        inv.setItem(19, section("&b&lBOTTES & ARMURES"));
        set(20, d(Material.IRON_BOOTS, "&fBottes de Fer Chute.IV", 100, "&7Chute de plumes IV"), 100,
            ench(new ItemStack(Material.IRON_BOOTS), Enchantment.FEATHER_FALLING, 4));
        set(21, d(Material.DIAMOND_BOOTS, "&bBottes Diamant Vitesse", 200, "&7Légèreté III + Chute de plumes IV"), 200,
            ench(ench(new ItemStack(Material.DIAMOND_BOOTS), Enchantment.DEPTH_STRIDER, 3), Enchantment.FEATHER_FALLING, 4));

        /* ── Potions ── */
        inv.setItem(28, section("&b&lPOTIONS & MISC"));
        set(29, d(Material.POTION, "&aPotion de Vitesse II", 80, "&7Vitesse II 1min30"), 80, speedPot());
        set(30, d(Material.POTION, "&ePotion de Sauts II", 70, "&7Sauts II 1min30"), 70, jumpPot());
        set(31, d(Material.SNOWBALL, "&fBoules de Neige ×16", 20, "&7Repousse les joueurs"), 20, new ItemStack(Material.SNOWBALL, 16));
        set(32, d(Material.GOLDEN_APPLE, "&6Pomme Dorée", 150, "&7Soins d'urgence"), 150, new ItemStack(Material.GOLDEN_APPLE));
        set(33, d(Material.FEATHER, "&fPlumes ×16", 30, "&7Légèreté supplémentaire"), 30, new ItemStack(Material.FEATHER, 16));
    }

    private ItemStack speedPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 1800, 1), true); pot.setItemMeta(m); return pot;
    }
    private ItemStack jumpPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 1800, 1), true); pot.setItemMeta(m); return pot;
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
    private ItemStack section(String name) {
        ItemStack i = new ItemStack(Material.GRAY_STAINED_GLASS_PANE); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); i.setItemMeta(m); return i;
    }
    private ItemStack header(Material mat, String name, String desc) {
        ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); m.setLore(List.of(c(desc)));
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); i.setItemMeta(m); return i;
    }
    private ItemStack border(Material mat) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(" "); i.setItemMeta(m); return i; }
    private ItemStack ench(ItemStack item, Enchantment e, int lvl) { item.addUnsafeEnchantment(e, lvl); return item; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
