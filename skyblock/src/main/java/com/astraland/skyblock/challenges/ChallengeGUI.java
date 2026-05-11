package com.astraland.skyblock.challenges;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;

public class ChallengeGUI implements InventoryHolder {

    private static final int PAGE_SIZE = 45;

    private final Skyblock plugin;
    private final Player   player;
    private final Island   island;
    private final Challenge.Category filter;
    private int page;

    private Inventory inv;

    public ChallengeGUI(Skyblock plugin, Player player, Island island, Challenge.Category filter, int page) {
        this.plugin  = plugin;
        this.player  = player;
        this.island  = island;
        this.filter  = filter;
        this.page    = page;
        build();
    }

    private void build() {
        String title = c("&8⚔ &a&lDéfis — " + categoryLabel());
        this.inv = Bukkit.createInventory(this, 54, title);

        ChallengeManager cm = plugin.getChallengeManager();
        int balance = plugin.getEconomyManager().getBalance(player.getUniqueId());

        List<Challenge> list = cm.getAllChallenges().stream()
            .filter(ch -> filter == null || ch.getCategory() == filter)
            .toList();

        int start = page * PAGE_SIZE;
        int end   = Math.min(start + PAGE_SIZE, list.size());

        for (int i = start; i < end; i++) {
            Challenge ch = list.get(i);
            boolean done = cm.isCompleted(island.getOwner(), ch.getId());
            boolean can  = cm.meetsCondition(island.getOwner(), ch, island, balance);
            inv.setItem(i - start, buildItem(ch, done, can, balance));
        }

        // Bottom bar
        ItemStack border = glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);

        // Category buttons
        inv.setItem(45, catBtn(null,                          Material.NETHER_STAR,   "&fTous les défis"));
        inv.setItem(46, catBtn(Challenge.Category.PROGRESSION, Material.OAK_SAPLING,   "&aProgression"));
        inv.setItem(47, catBtn(Challenge.Category.ECONOMIE,    Material.GOLD_INGOT,    "&6Économie"));
        inv.setItem(48, catBtn(Challenge.Category.CONSTRUCTION, Material.IRON_PICKAXE, "&7Construction"));
        inv.setItem(49, catBtn(Challenge.Category.SOCIAL,      Material.PLAYER_HEAD,   "&bSocial"));

        // Stats
        int total     = cm.getAllChallenges().size();
        int completed = cm.countCompleted(island.getOwner());
        ItemStack stat = new ItemStack(Material.BOOK);
        ItemMeta sm = stat.getItemMeta();
        if (sm != null) {
            sm.setDisplayName(c("&e&lProgression"));
            sm.setLore(List.of(
                c("&7Défis complétés : &a" + completed + " &8/ &f" + total),
                c("&7" + (total - completed) + " défis restants"),
                c(""),
                c("&7Clique sur un défi claimable &a(en vert)"),
                c("&7pour récupérer ta récompense !")
            ));
            stat.setItemMeta(sm);
        }
        inv.setItem(53, stat);

        // Navigation
        if (page > 0) {
            ItemStack prev = new ItemStack(Material.ARROW);
            ItemMeta pm = prev.getItemMeta();
            if (pm != null) { pm.setDisplayName(c("&7◀ Page précédente")); prev.setItemMeta(pm); }
            inv.setItem(51, prev);
        }
        if (end < list.size()) {
            ItemStack next = new ItemStack(Material.ARROW);
            ItemMeta nm = next.getItemMeta();
            if (nm != null) { nm.setDisplayName(c("&7Page suivante ▶")); next.setItemMeta(nm); }
            inv.setItem(52, next);
        }
    }

    private ItemStack buildItem(Challenge ch, boolean done, boolean canClaim, int balance) {
        Material mat = done ? Material.LIME_STAINED_GLASS_PANE : (canClaim ? ch.getIcon() : Material.RED_STAINED_GLASS_PANE);
        ItemStack it = new ItemStack(done ? Material.LIME_STAINED_GLASS_PANE : (canClaim ? ch.getIcon() : Material.RED_STAINED_GLASS_PANE));
        if (done) it.setType(Material.LIME_STAINED_GLASS_PANE);
        else if (canClaim) it.setType(ch.getIcon());
        else it.setType(Material.RED_STAINED_GLASS_PANE);

        ItemMeta meta = it.getItemMeta();
        if (meta == null) return it;

        meta.setDisplayName(c(ch.getDisplayName()) + (done ? c(" &a✔") : canClaim ? c(" &e⚡") : ""));
        List<String> lore = new ArrayList<>();
        lore.add(c("&7" + ch.getDescription()));
        lore.add(c(""));

        // Progress
        lore.add(c("&7Condition : " + conditionText(ch)));

        lore.add(c(""));
        lore.add(c("&7Récompense : &6" + fmt(ch.getRewardMoney()) + " $ &8+ &b" + ch.getRewardXP() + " XP"));
        lore.add(c(""));

        if (done) {
            lore.add(c("&a✔ Défi complété !"));
        } else if (canClaim) {
            lore.add(c("&e⚡ Clique pour réclamer la récompense !"));
        } else {
            lore.add(c("&c✗ Condition non remplie"));
        }

        meta.setLore(lore);
        it.setItemMeta(meta);
        return it;
    }

    private String conditionText(Challenge ch) {
        String req = "&6" + fmt(ch.getRequiredValue());
        return switch (ch.getType()) {
            case ISLAND_LEVEL     -> "Niveau d'île " + req;
            case GENERATOR_LEVEL  -> "Générateur niveau " + req;
            case BLOCKS_BROKEN    -> fmt(ch.getRequiredValue()) + " blocs cassés";
            case BALANCE          -> req + " &7pièces en banque perso";
            case ISLAND_VALUE     -> req + " &7pts de valeur d'île";
            case MEMBER_COUNT     -> req + " &7membre(s) sur l'île";
            case BANK_BALANCE     -> req + " &7pièces en banque d'île";
        };
    }

    private ItemStack catBtn(Challenge.Category cat, Material mat, String name) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(c(name));
            boolean active = (filter == cat);
            m.setLore(List.of(active ? c("&a▶ Filtre actif") : c("&7Cliquer pour filtrer")));
            it.setItemMeta(m);
        }
        return it;
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot < 0 || slot >= 54) return;

        ChallengeManager cm = plugin.getChallengeManager();
        int balance = plugin.getEconomyManager().getBalance(player.getUniqueId());

        // Category buttons
        if (slot == 45) { openCat(null);                          return; }
        if (slot == 46) { openCat(Challenge.Category.PROGRESSION); return; }
        if (slot == 47) { openCat(Challenge.Category.ECONOMIE);    return; }
        if (slot == 48) { openCat(Challenge.Category.CONSTRUCTION);return; }
        if (slot == 49) { openCat(Challenge.Category.SOCIAL);      return; }

        // Navigation
        if (slot == 51 && page > 0) {
            new ChallengeGUI(plugin, player, island, filter, page - 1).open(player);
            return;
        }
        if (slot == 52) {
            List<Challenge> list = cm.getAllChallenges().stream()
                .filter(ch -> filter == null || ch.getCategory() == filter).toList();
            if ((page + 1) * PAGE_SIZE < list.size()) {
                new ChallengeGUI(plugin, player, island, filter, page + 1).open(player);
            }
            return;
        }

        // Challenge slots 0-44
        if (slot >= 45) return;

        List<Challenge> list = cm.getAllChallenges().stream()
            .filter(ch -> filter == null || ch.getCategory() == filter)
            .toList();

        int idx = page * PAGE_SIZE + slot;
        if (idx >= list.size()) return;

        Challenge ch = list.get(idx);

        if (cm.isCompleted(island.getOwner(), ch.getId())) {
            player.sendMessage(c("&8[&a&lDéfis&8] &cTu as déjà complété ce défi."));
            return;
        }

        if (!cm.meetsCondition(island.getOwner(), ch, island, balance)) {
            player.sendMessage(c("&8[&a&lDéfis&8] &cCondition non remplie : " + conditionText(ch)));
            return;
        }

        // Claim!
        cm.complete(island.getOwner(), ch.getId());
        plugin.getEconomyManager().addBalance(player.getUniqueId(), ch.getRewardMoney());
        player.giveExpLevels(ch.getRewardXP() / 50);
        player.giveExp(ch.getRewardXP() % 50 * 7);

        player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.2f);
        player.sendMessage(c("&8[&a&lDéfis&8] &a✔ Défi &e" + c(ch.getDisplayName()) + " &acomplété ! +&6"
            + fmt(ch.getRewardMoney()) + " $ &a+&b" + ch.getRewardXP() + " XP"));

        // Refresh GUI
        build();
        player.openInventory(inv);
    }

    private void openCat(Challenge.Category cat) {
        new ChallengeGUI(plugin, player, island, cat, 0).open(player);
    }

    private String conditionText(Challenge ch) {
        return switch (ch.getType()) {
            case ISLAND_LEVEL     -> "Niveau d'île >= " + ch.getRequiredValue();
            case GENERATOR_LEVEL  -> "Générateur >= niveau " + ch.getRequiredValue();
            case BLOCKS_BROKEN    -> fmt(ch.getRequiredValue()) + " blocs cassés";
            case BALANCE          -> fmt(ch.getRequiredValue()) + " pièces";
            case ISLAND_VALUE     -> fmt(ch.getRequiredValue()) + " pts de valeur";
            case MEMBER_COUNT     -> ch.getRequiredValue() + " membre(s)";
            case BANK_BALANCE     -> fmt(ch.getRequiredValue()) + " $ en banque";
        };
    }

    private String categoryLabel() {
        if (filter == null) return "Tous";
        return switch (filter) {
            case PROGRESSION  -> "Progression";
            case ECONOMIE     -> "Économie";
            case CONSTRUCTION -> "Construction";
            case SOCIAL       -> "Social";
        };
    }

    private ItemStack glass(Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) { m.setDisplayName(" "); it.setItemMeta(m); }
        return it;
    }

    private String fmt(long v) { return NumberFormat.getInstance(java.util.Locale.FRENCH).format(v); }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p) { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }
}
