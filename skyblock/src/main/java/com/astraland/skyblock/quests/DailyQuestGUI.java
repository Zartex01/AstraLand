package com.astraland.skyblock.quests;

import com.astraland.skyblock.Skyblock;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class DailyQuestGUI implements InventoryHolder {

    private static final int[] QUEST_SLOTS = {10, 12, 14, 16, 28};

    private final Skyblock  plugin;
    private final UUID      uuid;
    private final Inventory inv;

    public DailyQuestGUI(Skyblock plugin, Player player) {
        this.plugin = plugin;
        this.uuid   = player.getUniqueId();
        this.inv    = Bukkit.createInventory(this, 54, c("&e&l⭐ Quêtes du Jour"));
        build(player);
    }

    private void build(Player player) {
        // Bordure
        ItemStack border  = glass(Material.YELLOW_STAINED_GLASS_PANE);
        ItemStack dark    = glass(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, dark);
        for (int i = 0; i < 9; i++)  inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);

        DailyQuestManager qm     = plugin.getQuestManager();
        List<DailyQuest>  quests = qm.getPlayerQuests(uuid);
        int completed = qm.countCompleted(uuid);
        int total     = quests.size();

        // Header
        inv.setItem(4, header(completed, total));

        // Quêtes
        for (int i = 0; i < quests.size() && i < QUEST_SLOTS.length; i++) {
            inv.setItem(QUEST_SLOTS[i], buildQuestItem(quests.get(i)));
        }

        // Fermer
        inv.setItem(49, closeBtn());
        // Reload
        inv.setItem(53, reloadBtn());
    }

    private ItemStack buildQuestItem(DailyQuest q) {
        DailyQuestManager qm   = plugin.getQuestManager();
        int  progress  = qm.getProgress(uuid, q.getId());
        boolean done   = qm.isCompleted(uuid, q.getId());
        int  target    = q.getTarget();

        Material mat = done ? Material.LIME_STAINED_GLASS_PANE : q.getIcon();
        ItemStack it  = new ItemStack(mat);
        ItemMeta  m   = it.getItemMeta();
        if (m == null) return it;

        m.setDisplayName(c(q.getDisplayName()));
        List<String> lore = new ArrayList<>();
        lore.add(c("&7" + q.getDescription()));
        lore.add(c(""));
        if (done) {
            lore.add(c("&a✔ Quête complétée !"));
        } else {
            int pct = Math.min(100, (int) ((double) progress / target * 100));
            String bar = buildBar(progress, target);
            lore.add(c("&7Progression : &f" + fmt(progress) + " &8/ &f" + fmt(target)));
            lore.add(c(bar + " &f" + pct + "%"));
        }
        lore.add(c(""));
        lore.add(c("&7Récompense : &6" + fmt(q.getRewardMoney()) + " $ &8+ &b" + q.getRewardXP() + " XP"));
        lore.add(c(""));
        lore.add(c("&8Type : " + typeLabel(q.getType())));
        m.setLore(lore);
        m.addItemFlags(ItemFlag.HIDE_ENCHANTS, ItemFlag.HIDE_ATTRIBUTES);
        it.setItemMeta(m);
        return it;
    }

    private String buildBar(int current, int max) {
        int filled = Math.min(20, (int) ((double) current / max * 20));
        StringBuilder sb = new StringBuilder("&7[");
        for (int i = 0; i < 20; i++) sb.append(i < filled ? "&a|" : "&8|");
        sb.append("&7]");
        return sb.toString();
    }

    private String typeLabel(DailyQuest.Type type) {
        return switch (type) {
            case BREAK_BLOCKS -> "&cCasser des blocs";
            case PLACE_BLOCKS -> "&aPlacer des blocs";
            case EARN_MONEY   -> "&6Gagner de l'argent";
            case KILL_MOBS    -> "&cTuer des monstres";
            case FISH         -> "&bPêcher";
            case GROW_CROPS   -> "&aRécolter";
        };
    }

    private ItemStack header(int done, int total) {
        ItemStack it = new ItemStack(Material.NETHER_STAR);
        ItemMeta  m  = it.getItemMeta();
        if (m == null) return it;
        m.setDisplayName(c("&e&l⭐ Quêtes du Jour"));
        m.setLore(List.of(
            c("&7Complétées : &a" + done + " &8/ &f" + total),
            c("&7Se renouvelle chaque jour à minuit."),
            c(""),
            c("&7Complète des quêtes pour gagner"),
            c("&7des pièces et de l'expérience !")
        ));
        it.setItemMeta(m);
        return it;
    }

    private ItemStack closeBtn() {
        ItemStack it = new ItemStack(Material.BARRIER);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) { m.setDisplayName(c("&cFermer")); it.setItemMeta(m); }
        return it;
    }

    private ItemStack reloadBtn() {
        ItemStack it = new ItemStack(Material.CLOCK);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(c("&7Actualiser"));
            m.setLore(List.of(c("&7Recharge l'affichage des quêtes")));
            it.setItemMeta(m);
        }
        return it;
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot == 49) { player.closeInventory(); return; }
        if (slot == 53) {
            // Rafraîchir
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 0.5f, 1f);
            new DailyQuestGUI(plugin, player).open(player);
        }
    }

    private ItemStack glass(Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) { m.setDisplayName(" "); it.setItemMeta(m); }
        return it;
    }

    private String fmt(int v)  { return NumberFormat.getInstance(Locale.FRENCH).format(v); }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p) { p.openInventory(inv); }
    @Override public Inventory getInventory() { return inv; }
}
