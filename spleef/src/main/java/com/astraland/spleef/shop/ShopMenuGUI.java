package com.astraland.spleef.shop;

import com.astraland.spleef.managers.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ShopMenuGUI implements InventoryHolder {

    private static final int[] CAT_SLOTS = {20, 22, 24};

    private final Inventory inv;
    private final Map<Integer, ShopCategoryData> categorySlots = new HashMap<>();
    private final List<ShopCategoryData> categories;
    private final EconomyManager eco;

    public ShopMenuGUI(Player player, EconomyManager eco) {
        this.eco = eco;
        this.categories = buildCategories();
        inv = Bukkit.createInventory(this, 54, c("&8&l✦ &b&lSHOP &8&l✦ &7Spleef"));
        build(player);
    }

    private void build(Player player) {
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE, " ");
        ItemStack accent = glass(Material.LIGHT_BLUE_STAINED_GLASS_PANE, " ");
        for (int i = 0; i < 54; i++) inv.setItem(i, border);
        for (int i = 0; i < 9; i++) inv.setItem(i, accent);
        for (int i = 45; i < 54; i++) inv.setItem(i, accent);
        inv.setItem(4, makeHeader());
        inv.setItem(49, makeBalance(eco.getBalance(player.getUniqueId())));
        for (int i = 0; i < categories.size() && i < CAT_SLOTS.length; i++) {
            ShopCategoryData cat = categories.get(i);
            inv.setItem(CAT_SLOTS[i], makeCategoryBtn(cat));
            categorySlots.put(CAT_SLOTS[i], cat);
        }
    }

    private List<ShopCategoryData> buildCategories() {
        return List.of(
            new ShopCategoryData("pelles", "&b&l🔧 Pelles", Material.DIAMOND_SHOVEL,
                "&7Pelles améliorées pour creuser rapidement", List.of(
                new ShopItemData("&fPelle de Fer Eff.II", Material.IRON_SHOVEL, 60, 25, new String[]{"&7Efficacité II"}, e(it(Material.IRON_SHOVEL, 1), Enchantment.EFFICIENCY, 2)),
                new ShopItemData("&bPelle Diamant Eff.III", Material.DIAMOND_SHOVEL, 120, 50, new String[]{"&7Efficacité III"}, e(it(Material.DIAMOND_SHOVEL, 1), Enchantment.EFFICIENCY, 3)),
                new ShopItemData("&5Pelle Diamant Eff.V SB.III", Material.DIAMOND_SHOVEL, 250, 100, new String[]{"&7Eff.V + Sans Brisure III"}, e(e(it(Material.DIAMOND_SHOVEL, 1), Enchantment.EFFICIENCY, 5), Enchantment.UNBREAKING, 3)),
                new ShopItemData("&dPelle Nétherite Ultime", Material.NETHERITE_SHOVEL, 500, 200, new String[]{"&7Eff.V + SB.III + Soie Tactile"}, e(e(e(it(Material.NETHERITE_SHOVEL, 1), Enchantment.EFFICIENCY, 5), Enchantment.UNBREAKING, 3), Enchantment.SILK_TOUCH, 1))
            )),
            new ShopCategoryData("bottes", "&b&l👟 Bottes & Mobilité", Material.DIAMOND_BOOTS,
                "&7Bottes et équipement de mobilité", List.of(
                new ShopItemData("&fBottes de Fer ChP.IV", Material.IRON_BOOTS, 100, 40, new String[]{"&7Chute de Plumes IV"}, e(it(Material.IRON_BOOTS, 1), Enchantment.FEATHER_FALLING, 4)),
                new ShopItemData("&bBottes Diamant Vitesse", Material.DIAMOND_BOOTS, 200, 80, new String[]{"&7Légèreté III + ChP.IV"}, e(e(it(Material.DIAMOND_BOOTS, 1), Enchantment.DEPTH_STRIDER, 3), Enchantment.FEATHER_FALLING, 4)),
                new ShopItemData("&fCasque de Fer", Material.IRON_HELMET, 40, 20, new String[]{"&7Protection de base"}, it(Material.IRON_HELMET, 1)),
                new ShopItemData("&fPlastron de Fer", Material.IRON_CHESTPLATE, 60, 30, new String[]{"&7Protection de base"}, it(Material.IRON_CHESTPLATE, 1)),
                new ShopItemData("&fJambières de Fer", Material.IRON_LEGGINGS, 50, 25, new String[]{"&7Protection de base"}, it(Material.IRON_LEGGINGS, 1))
            )),
            new ShopCategoryData("divers", "&f&l❄ Potions & Divers", Material.SNOWBALL,
                "&7Potions, projectiles et consommables", List.of(
                new ShopItemData("&aPotion de Vitesse II", Material.POTION, 80, 25, new String[]{"&7Vitesse II 1min30"}, speedPot()),
                new ShopItemData("&ePotion de Sauts II", Material.POTION, 70, 25, new String[]{"&7Sauts II 1min30"}, jumpPot()),
                new ShopItemData("&fBoules de Neige ×16", Material.SNOWBALL, 20, 10, new String[]{"&7Repousse les joueurs"}, it(Material.SNOWBALL, 16)),
                new ShopItemData("&6Pomme Dorée", Material.GOLDEN_APPLE, 150, 60, new String[]{"&7Soins d'urgence"}, it(Material.GOLDEN_APPLE, 1)),
                new ShopItemData("&dPomme Enchantée", Material.ENCHANTED_GOLDEN_APPLE, 700, 200, new String[]{"&7Puissance maximale"}, it(Material.ENCHANTED_GOLDEN_APPLE, 1)),
                new ShopItemData("&fPlumes ×16", Material.FEATHER, 30, 15, new String[]{"&7Légèreté supplémentaire"}, it(Material.FEATHER, 16)),
                new ShopItemData("&dTotem de Résurrection", Material.TOTEM_OF_UNDYING, 500, 200, new String[]{"&7Survie assurée"}, it(Material.TOTEM_OF_UNDYING, 1)),
                new ShopItemData("&fSteak ×8", Material.COOKED_BEEF, 25, 12, new String[]{"&7Regain de faim"}, it(Material.COOKED_BEEF, 8))
            ))
        );
    }

    private static ItemStack speedPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, 1800, 1), true); pot.setItemMeta(m); return pot;
    }
    private static ItemStack jumpPot() {
        ItemStack pot = new ItemStack(Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        m.addCustomEffect(new PotionEffect(PotionEffectType.JUMP_BOOST, 1800, 1), true); pot.setItemMeta(m); return pot;
    }

    private ItemStack makeHeader() {
        ItemStack item = new ItemStack(Material.SNOW_BLOCK); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&b&l✦ Shop Spleef ✦"));
        m.setLore(List.of(c("&7Améliore ta pelle et ta mobilité !"), c(""), c("&a▶ Clic gauche &f: Acheter"), c("&6▶ Clic droit &f: Vendre")));
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES); item.setItemMeta(m); return item;
    }

    private ItemStack makeBalance(int balance) {
        ItemStack item = new ItemStack(Material.SUNFLOWER); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c("&e&lTon Solde : &6" + balance + " $"));
        m.setLore(List.of(c("&7/balance &8— &7voir ton solde"), c("&7/pay &8— &7payer un joueur")));
        item.setItemMeta(m); return item;
    }

    private ItemStack makeCategoryBtn(ShopCategoryData cat) {
        ItemStack item = new ItemStack(cat.icon()); ItemMeta m = item.getItemMeta();
        m.setDisplayName(c(cat.displayName()));
        m.setLore(List.of(c(cat.description()), c(""), c("&f" + cat.items().size() + " &7articles"), c(""), c("&7▶ &fClic pour ouvrir")));
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES, ItemFlag.HIDE_ADDITIONAL_TOOLTIP, ItemFlag.HIDE_ARMOR_TRIM);
        item.setItemMeta(m); return item;
    }

    public ShopCategoryData getCategoryAt(int slot) { return categorySlots.get(slot); }
    public EconomyManager getEconomyManager() { return eco; }
    public void open(Player p) { p.openInventory(inv); }
    @Override public Inventory getInventory() { return inv; }

    private ItemStack glass(Material mat, String name) { ItemStack i = new ItemStack(mat); ItemMeta m = i.getItemMeta(); m.setDisplayName(name); i.setItemMeta(m); return i; }
    private static ItemStack it(Material mat, int amt) { return new ItemStack(mat, amt); }
    private static ItemStack e(ItemStack item, Enchantment ench, int lvl) { item.addUnsafeEnchantment(ench, lvl); return item; }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }
}
