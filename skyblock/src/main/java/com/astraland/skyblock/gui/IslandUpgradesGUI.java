package com.astraland.skyblock.gui;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.managers.EconomyManager;
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
import java.util.Locale;

public class IslandUpgradesGUI implements InventoryHolder {

    private static final int COST_FLY            = 100_000;
    private static final int COST_KEEP_INV        = 75_000;
    private static final int COST_MEMBER_SLOT     = 25_000;
    private static final int MAX_MEMBER_UPGRADES  = 3;

    private final Island   island;
    private final Skyblock plugin;
    private final Inventory inv;

    public IslandUpgradesGUI(Island island, Skyblock plugin) {
        this.island = island;
        this.plugin = plugin;
        this.inv    = Bukkit.createInventory(this, 36, c("&8✦ &d&lAméliorations d'île"));
        build();
    }

    private void build() {
        inv.clear();
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 36; i++) inv.setItem(i, border);

        // ── Titre ──────────────────────────────────────────────────────────────
        ItemStack title = new ItemStack(Material.NETHER_STAR);
        ItemMeta tm = title.getItemMeta();
        if (tm != null) {
            tm.setDisplayName(c("&d&l✦ Améliorations permanentes"));
            tm.setLore(List.of(
                c("&7Ces améliorations sont liées à ton île."),
                c("&7Elles s'appliquent à tous les membres !"),
                c(""),
                c("&7Solde : &6" + fmt(plugin.getEconomyManager().getBalance(island.getOwner())) + " $")
            ));
            title.setItemMeta(tm);
        }
        inv.setItem(4, title);

        // ── Vol sur l'île ──────────────────────────────────────────────────────
        boolean hasFly = island.hasFlyUpgrade();
        ItemStack fly = new ItemStack(hasFly ? Material.FEATHER : Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta fm = fly.getItemMeta();
        if (fm != null) {
            fm.setDisplayName(c(hasFly ? "&a✔ Vol sur l'île" : "&e✦ Vol sur l'île"));
            List<String> lore = new ArrayList<>();
            lore.add(c("&7Vole librement sur ta propre île !"));
            lore.add(c("&7Le vol se désactive hors de ton île."));
            lore.add(c(""));
            if (hasFly) {
                lore.add(c("&a✔ Déjà débloqué !"));
            } else {
                int bal = plugin.getEconomyManager().getBalance(island.getOwner());
                lore.add(c("&7Coût : &6" + fmt(COST_FLY) + " $"));
                lore.add(c("&7Ton solde : &e" + fmt(bal) + " $"));
                lore.add(bal >= COST_FLY ? c("&a▶ Cliquer pour acheter !") : c("&c✗ Fonds insuffisants"));
            }
            fm.setLore(lore);
            fly.setItemMeta(fm);
        }
        inv.setItem(10, fly);

        // ── Keep Inventory ─────────────────────────────────────────────────────
        boolean hasKeep = island.hasKeepInventoryUpgrade();
        ItemStack keep = new ItemStack(hasKeep ? Material.TOTEM_OF_UNDYING : Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta km = keep.getItemMeta();
        if (km != null) {
            km.setDisplayName(c(hasKeep ? "&a✔ Inventaire sauvegardé" : "&e✦ Inventaire sauvegardé"));
            List<String> lore = new ArrayList<>();
            lore.add(c("&7Garde ton inventaire à ta mort"));
            lore.add(c("&7si tu meurs sur ta propre île !"));
            lore.add(c(""));
            if (hasKeep) {
                lore.add(c("&a✔ Déjà débloqué !"));
            } else {
                int bal = plugin.getEconomyManager().getBalance(island.getOwner());
                lore.add(c("&7Coût : &6" + fmt(COST_KEEP_INV) + " $"));
                lore.add(c("&7Ton solde : &e" + fmt(bal) + " $"));
                lore.add(bal >= COST_KEEP_INV ? c("&a▶ Cliquer pour acheter !") : c("&c✗ Fonds insuffisants"));
            }
            km.setLore(lore);
            keep.setItemMeta(km);
        }
        inv.setItem(12, keep);

        // ── Slots membres supplémentaires ──────────────────────────────────────
        int memberUpgrades = island.getMemberSlotsUpgrade();
        boolean maxed = memberUpgrades >= MAX_MEMBER_UPGRADES;
        ItemStack member = new ItemStack(maxed ? Material.LIME_CONCRETE : Material.PLAYER_HEAD);
        ItemMeta mm = member.getItemMeta();
        if (mm != null) {
            mm.setDisplayName(c(maxed ? "&a✔ Slots membres (MAX)" : "&e✦ Slots membres +2"));
            List<String> lore = new ArrayList<>();
            lore.add(c("&7Augmente le nombre de membres de ton île."));
            lore.add(c("&7Base : &f3 membres &8| &7Actuel : &a" + (3 + memberUpgrades * 2)));
            lore.add(c("&7Améliorations : &e" + memberUpgrades + " &8/ &e" + MAX_MEMBER_UPGRADES));
            lore.add(c(""));
            if (maxed) {
                lore.add(c("&a✔ Maximum atteint !"));
            } else {
                int nextCost = COST_MEMBER_SLOT * (memberUpgrades + 1);
                int bal = plugin.getEconomyManager().getBalance(island.getOwner());
                lore.add(c("&7Prochain palier : +2 slots pour &6" + fmt(nextCost) + " $"));
                lore.add(c("&7Ton solde : &e" + fmt(bal) + " $"));
                lore.add(bal >= nextCost ? c("&a▶ Cliquer pour acheter !") : c("&c✗ Fonds insuffisants"));
            }
            mm.setLore(lore);
            member.setItemMeta(mm);
        }
        inv.setItem(14, member);

        // ── Info générateur (redirection) ──────────────────────────────────────
        ItemStack gen = new ItemStack(Material.COAL_ORE);
        ItemMeta gm = gen.getItemMeta();
        if (gm != null) {
            gm.setDisplayName(c("&b⚙ Générateur &8- Nv." + island.getGeneratorLevel() + "/7"));
            gm.setLore(List.of(
                c("&7Améliore ton générateur de cobblestone"),
                c("&7pour obtenir des minerais rares !"),
                c(""),
                c("&7▶ Cliquer pour ouvrir le générateur")
            ));
            gen.setItemMeta(gm);
        }
        inv.setItem(16, gen);

        // ── Retour ─────────────────────────────────────────────────────────────
        ItemStack back = new ItemStack(Material.ARROW);
        ItemMeta bm = back.getItemMeta();
        if (bm != null) { bm.setDisplayName(c("&c← Fermer")); back.setItemMeta(bm); }
        inv.setItem(31, back);
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();

        if (!island.isOwner(player.getUniqueId())) {
            player.sendMessage(c("&cSeul le propriétaire peut acheter des améliorations."));
            return;
        }

        EconomyManager eco = plugin.getEconomyManager();
        int bal = eco.getBalance(island.getOwner());

        switch (slot) {
            case 10 -> { // Vol
                if (island.hasFlyUpgrade()) {
                    player.sendMessage(c("&cTu as déjà le vol sur ton île !")); return;
                }
                if (bal < COST_FLY) {
                    player.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + fmt(COST_FLY) + " $")); return;
                }
                eco.removeBalance(island.getOwner(), COST_FLY);
                island.setFlyUpgrade(true);
                plugin.getIslandManager().saveAll();
                player.setAllowFlight(true);
                player.setFlying(true);
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
                player.sendMessage(c("&a✔ Vol sur l'île débloqué ! Tu peux maintenant voler sur ton île."));
                build();
            }
            case 12 -> { // Keep inventory
                if (island.hasKeepInventoryUpgrade()) {
                    player.sendMessage(c("&cTu as déjà l'inventaire sauvegardé !")); return;
                }
                if (bal < COST_KEEP_INV) {
                    player.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + fmt(COST_KEEP_INV) + " $")); return;
                }
                eco.removeBalance(island.getOwner(), COST_KEEP_INV);
                island.setKeepInventoryUpgrade(true);
                plugin.getIslandManager().saveAll();
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
                player.sendMessage(c("&a✔ Inventaire sauvegardé débloqué ! Tu garderas ton inv si tu meurs sur ton île."));
                build();
            }
            case 14 -> { // Member slots
                int upgrades = island.getMemberSlotsUpgrade();
                if (upgrades >= MAX_MEMBER_UPGRADES) {
                    player.sendMessage(c("&cAméliorations de slots au maximum !")); return;
                }
                int cost = COST_MEMBER_SLOT * (upgrades + 1);
                if (bal < cost) {
                    player.sendMessage(c("&c✗ Fonds insuffisants ! Il te faut &e" + fmt(cost) + " $")); return;
                }
                eco.removeBalance(island.getOwner(), cost);
                island.setMemberSlotsUpgrade(upgrades + 1);
                island.setMemberSlots(3 + (upgrades + 1) * 2);
                plugin.getIslandManager().saveAll();
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.8f, 1.0f);
                player.sendMessage(c("&a✔ Slots membres améliorés ! Ton île accepte maintenant &e" + island.getMemberSlots() + " &amembres."));
                build();
            }
            case 16 -> { // Open generator
                player.closeInventory();
                new IslandGeneratorGUI(island, plugin).open(player);
            }
            case 31 -> player.closeInventory();
        }
    }

    private ItemStack glass(Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) { m.setDisplayName(" "); it.setItemMeta(m); }
        return it;
    }

    private String fmt(long v)  { return NumberFormat.getInstance(Locale.FRENCH).format(v); }
    private String c(String s)  { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p)  { p.openInventory(inv); }

    @Override
    public Inventory getInventory() { return inv; }
}
