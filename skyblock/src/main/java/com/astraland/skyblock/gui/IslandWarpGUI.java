package com.astraland.skyblock.gui;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.models.Island;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class IslandWarpGUI implements InventoryHolder {

    private final Inventory inv;
    private final Skyblock plugin;
    private final Map<Integer, Island> warpSlots = new HashMap<>();

    public IslandWarpGUI(Skyblock plugin) {
        this.plugin = plugin;
        List<Island> warps = plugin.getIslandManager().getWarps();
        int size = Math.max(9, ((warps.size() / 9) + 1) * 9);
        if (size > 54) size = 54;
        this.inv = Bukkit.createInventory(this, size, c("&8🌍 &a&lWarps des Îles"));
        build(warps);
    }

    private void build(List<Island> warps) {
        inv.clear();
        warpSlots.clear();
        ItemStack border = glass(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < inv.getSize(); i++) inv.setItem(i, border);

        if (warps.isEmpty()) {
            ItemStack none = new ItemStack(Material.BARRIER);
            ItemMeta m = none.getItemMeta();
            if (m != null) { m.setDisplayName(c("&cAucun warp disponible")); m.setLore(List.of(c("&7Utilise &e/is setwarp &7pour créer un warp public."))); none.setItemMeta(m); }
            inv.setItem(inv.getSize() / 2, none);
            return;
        }

        int[] displaySlots = {10,11,12,13,14,15,16, 19,20,21,22,23,24,25, 28,29,30,31,32,33,34, 37,38,39,40,41,42,43};
        for (int i = 0; i < Math.min(warps.size(), displaySlots.length); i++) {
            Island isl  = warps.get(i);
            int slot    = displaySlots[i];
            if (slot >= inv.getSize()) continue;
            OfflinePlayer owner = Bukkit.getOfflinePlayer(isl.getOwner());
            String ownerName = owner.getName() != null ? owner.getName() : "Inconnu";

            ItemStack head = new ItemStack(Material.PLAYER_HEAD);
            SkullMeta sm = (SkullMeta) head.getItemMeta();
            if (sm != null) {
                sm.setOwningPlayer(owner);
                String displayName = isl.getWarpName().isBlank() ? ownerName : isl.getWarpName();
                sm.setDisplayName(c("&a" + displayName));
                List<String> lore = new ArrayList<>();
                lore.add(c("&7Propriétaire : &e" + ownerName));
                lore.add(c("&7Niveau île : &a" + isl.getLevel()));
                lore.add(c("&7Membres : &f" + (isl.getMemberCount() + 1)));
                if (isl.isPvpEnabled()) lore.add(c("&c⚔ PvP Activé"));
                lore.add(c(""));
                lore.add(c("&e▶ Cliquer pour visiter !"));
                sm.setLore(lore);
                head.setItemMeta(sm);
            }
            inv.setItem(slot, head);
            warpSlots.put(slot, isl);
        }
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        Island isl = warpSlots.get(slot);
        if (isl == null) return;
        if (isl.isBanned(player.getUniqueId())) {
            player.sendMessage(c("&cTu es banni de cette île."));
            return;
        }
        if (isl.getHome() == null) { player.sendMessage(c("&cCette île n'a pas de home défini.")); return; }
        player.closeInventory();
        player.teleport(isl.getHome());
        player.sendMessage(c("&aTéléporté au warp &e" + (isl.getWarpName().isBlank() ? "de l'île" : isl.getWarpName()) + "&a !"));
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 0.5f, 1.0f);
    }

    private ItemStack glass(Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta m = it.getItemMeta();
        if (m != null) { m.setDisplayName(" "); it.setItemMeta(m); }
        return it;
    }
    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p) { p.openInventory(inv); }
    @Override public Inventory getInventory() { return inv; }
}
