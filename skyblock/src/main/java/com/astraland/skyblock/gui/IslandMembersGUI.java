package com.astraland.skyblock.gui;

import com.astraland.skyblock.Skyblock;
import com.astraland.skyblock.managers.IslandManager;
import com.astraland.skyblock.models.Island;
import com.astraland.skyblock.models.IslandRole;
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

import java.util.*;

public class IslandMembersGUI implements InventoryHolder {

    private final Skyblock plugin;
    private final Island   island;
    private final Inventory inv;
    private final Map<Integer, UUID> slotToMember = new HashMap<>();
    private final boolean isOwner;

    public IslandMembersGUI(Skyblock plugin, Island island, Player viewer) {
        this.plugin   = plugin;
        this.island   = island;
        this.isOwner  = island.isOwner(viewer.getUniqueId());
        this.inv      = Bukkit.createInventory(this, 54, c("&e&l👥 Membres de l'île"));
        build();
    }

    private void build() {
        ItemStack border = glass(Material.BLUE_STAINED_GLASS_PANE);
        ItemStack dark   = glass(Material.BLACK_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, dark);
        for (int i = 0; i < 9; i++)  inv.setItem(i, border);
        for (int i = 45; i < 54; i++) inv.setItem(i, border);

        // Header
        inv.setItem(4, header());

        // Propriétaire en premier
        int slot = 10;
        slot = addMemberSlot(slot, island.getOwner(), IslandRole.OWNER);

        // Officiers
        for (UUID uid : island.getOfficers()) {
            if (slot >= 44) break;
            slot = addMemberSlot(slot, uid, IslandRole.OFFICER);
        }

        // Membres
        for (UUID uid : island.getMembers()) {
            if (slot >= 44) break;
            slot = addMemberSlot(slot, uid, IslandRole.MEMBER);
        }

        // Slots vides restants (invitation possible)
        int maxSlots = island.getMemberSlots() + 1;
        int used     = 1 + island.getMemberCount(); // +1 owner
        inv.setItem(slot, slotInfo(used, maxSlots));

        // Bouton fermer
        inv.setItem(49, closeBtn());
    }

    private int addMemberSlot(int slot, UUID uid, IslandRole role) {
        OfflinePlayer op = Bukkit.getOfflinePlayer(uid);
        String name = op.getName() != null ? op.getName() : "?";

        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta sm    = (SkullMeta) skull.getItemMeta();
        if (sm != null) {
            sm.setOwningPlayer(op);
            sm.setDisplayName(c(role.getPrefix() + " &f" + name));
            List<String> lore = new ArrayList<>();
            lore.add(c("&7Rôle : " + role.getDisplayName()));
            boolean online = Bukkit.getPlayer(uid) != null;
            lore.add(c("&7État : " + (online ? "&aEn ligne" : "&7Hors ligne")));
            if (isOwner && role != IslandRole.OWNER) {
                lore.add(c(""));
                if (role == IslandRole.MEMBER) lore.add(c("&e▶ Clic gauche &fpour promouvoir Officier"));
                if (role == IslandRole.OFFICER) lore.add(c("&e▶ Clic gauche &fpour rétrograder Membre"));
                lore.add(c("&c▶ Clic droit &fexpulser de l'île"));
            }
            sm.setLore(lore);
            skull.setItemMeta(sm);
        }
        slotToMember.put(slot, uid);
        inv.setItem(slot, skull);
        return slot + 1;
    }

    public void handleClick(InventoryClickEvent e, Player player) {
        e.setCancelled(true);
        int slot = e.getRawSlot();
        if (slot == 49) { player.closeInventory(); return; }

        UUID targetUid = slotToMember.get(slot);
        if (targetUid == null) return;
        if (!isOwner && !island.getRole(player.getUniqueId()).canKickMembers()) {
            player.sendMessage(c("&cTu n'as pas la permission."));
            return;
        }
        if (island.isOwner(targetUid)) {
            player.sendMessage(c("&cTu ne peux pas modifier le statut du propriétaire."));
            return;
        }

        IslandManager im = plugin.getIslandManager();

        if (e.isLeftClick()) {
            IslandRole current = island.getRole(targetUid);
            if (current == IslandRole.MEMBER) {
                island.setRole(targetUid, IslandRole.OFFICER);
                im.saveAll();
                player.sendMessage(c("&a✔ Promu Officier."));
                player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 0.5f, 1.2f);
                Player target = Bukkit.getPlayer(targetUid);
                if (target != null) target.sendMessage(c("&8[&eSkyblock&8] &e" + player.getName() + " &at'a promu &eOfficier &a!"));
            } else if (current == IslandRole.OFFICER) {
                island.setRole(targetUid, IslandRole.MEMBER);
                im.saveAll();
                player.sendMessage(c("&7✔ Rétrogradé Membre."));
                player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.8f);
                Player target = Bukkit.getPlayer(targetUid);
                if (target != null) target.sendMessage(c("&8[&eSkyblock&8] &e" + player.getName() + " &at'a rétrogradé &7Membre&a."));
            }
            new IslandMembersGUI(plugin, island, player).open(player);
        } else if (e.isRightClick()) {
            if (!island.isOwner(player.getUniqueId()) && island.getRole(player.getUniqueId()) != IslandRole.OFFICER) {
                player.sendMessage(c("&cSeuls les Officiers et le Propriétaire peuvent expulser.")); return;
            }
            // Officier peut expulser les membres, pas les autres officiers
            if (island.getRole(targetUid) == IslandRole.OFFICER && !island.isOwner(player.getUniqueId())) {
                player.sendMessage(c("&cSeul le propriétaire peut expulser un officier.")); return;
            }
            im.removeMember(island, targetUid);
            Player target = Bukkit.getPlayer(targetUid);
            if (target != null) {
                if (plugin.isInPluginWorld(target)) target.teleport(target.getWorld().getSpawnLocation());
                target.sendMessage(c("&cTu as été expulsé de l'île."));
            }
            player.sendMessage(c("&a✔ Joueur expulsé."));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_BASS, 0.5f, 0.7f);
            new IslandMembersGUI(plugin, island, player).open(player);
        }
    }

    private ItemStack header() {
        int used = 1 + island.getMemberCount();
        int max  = island.getMemberSlots() + 1;
        ItemStack it = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(c("&e&l👥 Membres de l'île"));
            m.setLore(List.of(
                c("&7Slots occupés : &f" + used + " &8/ &f" + max),
                c(""),
                c("&6★ &fPropriétaire &8— &7Contrôle total"),
                c("&eO &fOfficier &8— &7Peut inviter/expulser"),
                c("&7M &fMembre &8— &7Accès standard"),
                c(""),
                c("&7/is invite <joueur> &8— &fInviter")
            ));
            it.setItemMeta(m);
        }
        return it;
    }

    private ItemStack slotInfo(int used, int max) {
        boolean full = used >= max;
        ItemStack it = new ItemStack(full ? Material.RED_STAINED_GLASS_PANE : Material.GREEN_STAINED_GLASS_PANE);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) {
            m.setDisplayName(c(full ? "&cÎle pleine (" + used + "/" + max + ")" : "&aSlot libre (" + used + "/" + max + ")"));
            m.setLore(List.of(full ? c("&7Achète des slots via &e/is upgrades") : c("&7/is invite <joueur>")));
            it.setItemMeta(m);
        }
        return it;
    }

    private ItemStack closeBtn() {
        ItemStack it = new ItemStack(Material.BARRIER);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) { m.setDisplayName(c("&cFermer")); it.setItemMeta(m); }
        return it;
    }

    private ItemStack glass(Material mat) {
        ItemStack it = new ItemStack(mat);
        ItemMeta  m  = it.getItemMeta();
        if (m != null) { m.setDisplayName(" "); it.setItemMeta(m); }
        return it;
    }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    public void open(Player p) { p.openInventory(inv); }
    @Override public Inventory getInventory() { return inv; }
}
