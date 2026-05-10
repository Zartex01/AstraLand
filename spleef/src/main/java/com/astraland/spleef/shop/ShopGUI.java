package com.astraland.spleef.shop;

import com.astraland.spleef.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.ClickType;
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
    private record ShopEntry(int price, int sellPrice, ItemStack[] reward) {}

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
        inv.setItem(4, header(Material.SNOW_BLOCK, "&b&lSpleef Shop", "&7Améliore ta pelle et ta mobilité ! | &aClic gauche &7: Acheter | &6Clic droit &7: Vendre"));

        /* ── Pelles ── */
        inv.setItem(10, section("&b&lPELLES"));
        set(11, d(Material.IRON_SHOVEL, "&fPelle de Fer Eff.II", 60, 0, "&7Efficacité II"), 60, 0,
            ench(new ItemStack(Material.IRON_SHOVEL), Enchantment.EFFICIENCY, 2));
        set(12, d(Material.DIAMOND_SHOVEL, "&bPelle Diamant Eff.III", 120, 0, "&7Efficacité III"), 120, 0,
            ench(new ItemStack(Material.DIAMOND_SHOVEL), Enchantment.EFFICIENCY, 3));
        set(13, d(Material.DIAMOND_SHOVEL, "&5Pelle Diamant Eff.V", 250, 0, "&7Efficacité V + Sans Brisure III"), 250, 0,
            ench(ench(new ItemStack(Material.DIAMOND_SHOVEL), Enchantment.EFFICIENCY, 5), Enchantment.UNBREAKING, 3));
        set(14, d(Material.NETHERITE_SHOVEL, "&dPelle Nétherite Ultime", 500, 0, "&7Eff.V + Sans Brisure III + Soie Tactile"), 500, 0,
            ench(ench(ench(new ItemStack(Material.NETHERITE_SHOVEL), Enchantment.EFFICIENCY, 5), Enchantment.UNBREAKING, 3), Enchantment.SILK_TOUCH, 1));

        /* ── Bottes ── */
        inv.setItem(19, section("&b&lBOTTES & ARMURES"));
        set(20, d(Material.IRON_BOOTS, "&fBottes de Fer Chute.IV", 100, 0, "&7Chute de plumes IV"), 100, 0,
            ench(new ItemStack(Material.IRON_BOOTS), Enchantment.FEATHER_FALLING, 4));
        set(21, d(Material.DIAMOND_BOOTS, "&bBottes Diamant Vitesse", 200, 0, "&7Légèreté III + Chute de plumes IV"), 200, 0,
            ench(ench(new ItemStack(Material.DIAMOND_BOOTS), Enchantment.DEPTH_STRIDER, 3), Enchantment.FEATHER_FALLING, 4));

        /* ── Potions & Misc ── */
        inv.setItem(28, section("&b&lPOTIONS & MISC"));
        set(29, d(Material.POTION, "&aPotion de Vitesse II", 80, 0, "&7Vitesse II 1min30"), 80, 0, speedPot());
        set(30, d(Material.POTION, "&ePotion de Sauts II", 70, 0, "&7Sauts II 1min30"), 70, 0, jumpPot());
        set(31, d(Material.SNOWBALL, "&fBoules de Neige ×16", 20, 10, "&7Repousse les joueurs"), 20, 10, new ItemStack(Material.SNOWBALL, 16));
        set(32, d(Material.GOLDEN_APPLE, "&6Pomme Dorée", 150, 0, "&7Soins d'urgence"), 150, 0, new ItemStack(Material.GOLDEN_APPLE));
        set(33, d(Material.FEATHER, "&fPlumes ×16", 30, 15, "&7Légèreté supplémentaire"), 30, 15, new ItemStack(Material.FEATHER, 16));
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

    private void set(int slot, ItemStack display, int price, int sellPrice, ItemStack... reward) {
        inv.setItem(slot, display); entries.put(slot, new ShopEntry(price, sellPrice, reward));
    }

    public void handleClick(InventoryClickEvent e, EconomyManager eco) {
        e.setCancelled(true);
        if (!(e.getWhoClicked() instanceof Player player)) return;
        ShopEntry entry = entries.get(e.getSlot());
        if (entry == null) return;

        if (e.getClick() == ClickType.RIGHT || e.getClick() == ClickType.SHIFT_RIGHT) {
            handleSell(player, eco, entry);
        } else {
            handleBuy(player, eco, entry);
        }
    }

    private void handleBuy(Player player, EconomyManager eco, ShopEntry entry) {
        if (!eco.removeBalance(player.getUniqueId(), entry.price())) {
            player.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + entry.price() + " $&c.")); return;
        }
        for (ItemStack item : entry.reward()) if (item != null) player.getInventory().addItem(item.clone());
        player.sendMessage(c("&a✔ Achat pour &6" + entry.price() + " $&a. &7Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
    }

    private void handleSell(Player player, EconomyManager eco, ShopEntry entry) {
        if (entry.sellPrice() <= 0 || entry.reward().length != 1) {
            player.sendMessage(c("&c✗ Cet item n'est pas vendable.")); return;
        }
        ItemStack ref = entry.reward()[0];
        Material mat = ref.getType();
        int needed = ref.getAmount();
        int inInv = countInInventory(player, mat);
        if (inInv == 0) {
            player.sendMessage(c("&c✗ Tu n'as pas de &f" + mat.name().toLowerCase().replace('_', ' ') + " &cà vendre.")); return;
        }
        int toSell = Math.min(inInv, needed);
        int gained = (int) Math.floor((double) entry.sellPrice() / needed * toSell);
        removeFromInventory(player, mat, toSell);
        eco.addBalance(player.getUniqueId(), gained);
        player.sendMessage(c("&e💰 Vendu : &f" + toSell + "x &7pour &6" + gained + " $ &8| &7Solde : &e" + eco.getBalance(player.getUniqueId()) + " $"));
    }

    private int countInInventory(Player player, Material mat) {
        int count = 0;
        for (ItemStack item : player.getInventory().getContents())
            if (item != null && item.getType() == mat) count += item.getAmount();
        return count;
    }

    private void removeFromInventory(Player player, Material mat, int amount) {
        int remaining = amount;
        ItemStack[] contents = player.getInventory().getContents();
        for (int i = 0; i < contents.length && remaining > 0; i++) {
            ItemStack item = contents[i];
            if (item == null || item.getType() != mat) continue;
            if (item.getAmount() <= remaining) { remaining -= item.getAmount(); player.getInventory().setItem(i, null); }
            else { item.setAmount(item.getAmount() - remaining); remaining = 0; }
        }
    }

    @Override public Inventory getInventory() { return inv; }
    public void open(Player p) { p.openInventory(inv); }

    private ItemStack d(Material mat, String name, int price, int sellPrice, String... desc) {
        ItemStack item = new ItemStack(mat); ItemMeta meta = item.getItemMeta();
        meta.setDisplayName(c(name));
        List<String> lore = new ArrayList<>(Arrays.stream(desc).map(this::c).toList());
        lore.add("");
        lore.add(c("&a🛒 Achat : &e" + price + " $"));
        if (sellPrice > 0) lore.add(c("&6💰 Vente : &e" + sellPrice + " $"));
        lore.add("");
        lore.add(c("&a▶ Clic gauche &fpour acheter"));
        if (sellPrice > 0) lore.add(c("&6▶ Clic droit &fpour vendre"));
        meta.setLore(lore); meta.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        item.setItemMeta(meta); return item;
    }
    private ItemStack section(String name) { ItemStack i = new ItemStack(Material.GRAY_STAINED_GLASS_PANE); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); i.setItemMeta(m); return i; }
    private ItemStack header(Material mat, String name, String desc) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(c(name)); m.setLore(List.of(c(desc))); m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); i.setItemMeta(m); return i; }
    private ItemStack border(Material mat) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(" "); i.setItemMeta(m); return i; }
    private ItemStack ench(ItemStack item, Enchantment e, int lvl) { item.addUnsafeEnchantment(e, lvl); return item; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
