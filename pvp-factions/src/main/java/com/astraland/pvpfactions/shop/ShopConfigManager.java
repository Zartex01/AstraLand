package com.astraland.pvpfactions.shop;

import com.astraland.pvpfactions.PvpFactions;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ShopConfigManager {

    private final PvpFactions plugin;
    private final List<ShopCategoryData> categories = new ArrayList<>();
    private File shopFile;

    public ShopConfigManager(PvpFactions plugin) {
        this.plugin = plugin;
        reload();
    }

    public void reload() {
        categories.clear();
        shopFile = new File(plugin.getDataFolder(), "shop.yml");
        if (!shopFile.exists()) {
            plugin.saveResource("shop.yml", false);
        }
        YamlConfiguration cfg = YamlConfiguration.loadConfiguration(shopFile);

        ConfigurationSection cats = cfg.getConfigurationSection("categories");
        if (cats == null) return;

        for (String catId : cats.getKeys(false)) {
            ConfigurationSection catSec = cats.getConfigurationSection(catId);
            if (catSec == null) continue;

            String display     = catSec.getString("display", "&f" + catId);
            String iconStr     = catSec.getString("icon", "CHEST");
            String description = catSec.getString("description", "");
            Material icon = parseMaterial(iconStr, Material.CHEST);

            List<ShopItemData> items = new ArrayList<>();
            List<?> rawItems = catSec.getList("items");
            if (rawItems != null) {
                for (Object raw : rawItems) {
                    if (!(raw instanceof Map<?, ?> map)) continue;
                    ShopItemData data = parseItem(map);
                    if (data != null) items.add(data);
                }
            }
            categories.add(new ShopCategoryData(catId, display, icon, description, items));
        }
    }

    @SuppressWarnings("unchecked")
    private ShopItemData parseItem(Map<?, ?> map) {
        try {
            String name    = str(map, "name", "Item");
            int buy        = num(map, "buy",  0);
            int sell       = num(map, "sell", 0);
            Object loreObj = map.get("lore");
            List<String> loreList = loreObj instanceof List<?> l
                ? l.stream().map(Object::toString).toList() : List.of();
            String[] lore  = loreList.toArray(new String[0]);

            String type    = str(map, "type", "NORMAL").toUpperCase();
            ItemStack reward;
            Material icon;

            switch (type) {
                case "POTION" -> {
                    boolean splash  = Boolean.parseBoolean(str(map, "splash", "false"));
                    String effect   = str(map, "effect", "SPEED");
                    int duration    = num(map, "duration", 600);
                    int amplifier   = num(map, "amplifier", 0);
                    reward = buildPotion(splash, effect, duration, amplifier);
                    icon   = splash ? Material.SPLASH_POTION : Material.POTION;
                }
                case "BOOK" -> {
                    String enchName = str(map, "enchantment", "SHARPNESS");
                    int level       = num(map, "level", 1);
                    reward = buildBook(enchName, level);
                    icon   = Material.ENCHANTED_BOOK;
                }
                default -> {
                    String matStr = str(map, "material", "STONE");
                    Material mat  = parseMaterial(matStr, Material.STONE);
                    int amount    = num(map, "amount", 1);
                    reward = new ItemStack(mat, amount);
                    applyEnchants(reward, map);
                    icon = mat;
                }
            }
            return new ShopItemData(name, icon, buy, sell, lore, reward);
        } catch (Exception e) {
            plugin.getLogger().warning("[Shop] Erreur parsing item : " + e.getMessage());
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    private void applyEnchants(ItemStack item, Map<?, ?> map) {
        Object enchObj = map.get("enchants");
        if (!(enchObj instanceof Map<?, ?> enchMap)) return;
        ItemMeta meta = item.getItemMeta();
        for (Map.Entry<?, ?> entry : enchMap.entrySet()) {
            Enchantment ench = Enchantment.getByKey(
                org.bukkit.NamespacedKey.minecraft(entry.getKey().toString().toLowerCase()));
            if (ench == null) continue;
            int lvl = Integer.parseInt(entry.getValue().toString());
            meta.addEnchant(ench, lvl, true);
        }
        item.setItemMeta(meta);
    }

    private ItemStack buildPotion(boolean splash, String effectName, int duration, int amplifier) {
        ItemStack pot = new ItemStack(splash ? Material.SPLASH_POTION : Material.POTION);
        org.bukkit.inventory.meta.PotionMeta m = (org.bukkit.inventory.meta.PotionMeta) pot.getItemMeta();
        PotionEffectType type = PotionEffectType.getByName(effectName);
        if (type == null) type = PotionEffectType.SPEED;
        m.addCustomEffect(new PotionEffect(type, duration, amplifier), true);
        pot.setItemMeta(m);
        return pot;
    }

    private ItemStack buildBook(String enchName, int level) {
        ItemStack book = new ItemStack(Material.ENCHANTED_BOOK);
        EnchantmentStorageMeta meta = (EnchantmentStorageMeta) book.getItemMeta();
        Enchantment ench = Enchantment.getByKey(
            org.bukkit.NamespacedKey.minecraft(enchName.toLowerCase()));
        if (ench != null) meta.addStoredEnchant(ench, level, true);
        book.setItemMeta(meta);
        return book;
    }

    private Material parseMaterial(String name, Material fallback) {
        try { return Material.valueOf(name.toUpperCase()); }
        catch (Exception e) { return fallback; }
    }

    private String str(Map<?, ?> m, String key, String def) {
        Object v = m.get(key);
        return v != null ? v.toString() : def;
    }

    private int num(Map<?, ?> m, String key, int def) {
        Object v = m.get(key);
        if (v == null) return def;
        try { return Integer.parseInt(v.toString()); }
        catch (NumberFormatException e) { return def; }
    }

    public List<ShopCategoryData> getCategories() { return categories; }

    /**
     * Retourne un Map Material → prix à l'item (pour /sell all).
     * Seuls les items NORMAUX (récompense = ItemStack simple) et vendables sont inclus.
     * Si un même material apparaît plusieurs fois, on garde le prix le plus élevé.
     */
    public Map<Material, Integer> getSellPriceMap() {
        Map<Material, Integer> map = new java.util.HashMap<>();
        for (ShopCategoryData cat : categories) {
            for (ShopItemData item : cat.items()) {
                if (!item.isSellable()) continue;
                ItemStack reward = item.reward();
                if (reward == null) continue;
                Material mat = reward.getType();
                int pricePerItem = (int) Math.floor((double) item.sellPrice() / Math.max(1, reward.getAmount()));
                if (pricePerItem <= 0) continue;
                // Garde le meilleur prix si le même material est listé plusieurs fois
                map.merge(mat, pricePerItem, Math::max);
            }
        }
        return map;
    }
}
