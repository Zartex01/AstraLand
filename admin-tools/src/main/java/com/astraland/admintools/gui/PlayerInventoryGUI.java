package com.astraland.admintools.gui;

import com.astraland.admintools.AdminTools;
import com.astraland.admintools.gui.holder.PlayerInventoryHolder;
import com.astraland.admintools.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

import java.util.UUID;

public class PlayerInventoryGUI {

    private final AdminTools plugin;

    public PlayerInventoryGUI(AdminTools plugin) {
        this.plugin = plugin;
    }

    public void open(Player admin, UUID targetUUID) {
        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null) {
            admin.sendMessage(ItemBuilder.c("&cCe joueur n'est plus en ligne."));
            plugin.getPlayerListGUI().open(admin);
            return;
        }

        plugin.getSession(admin).setTargetPlayerUUID(targetUUID);

        Inventory inv = Bukkit.createInventory(new PlayerInventoryHolder(targetUUID), 54,
                ItemBuilder.c("&8» &bInventaire de &e" + target.getName() + " «"));

        ItemStack gray = ItemBuilder.glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 54; i++) inv.setItem(i, gray);

        PlayerInventory pi = target.getInventory();

        for (int slot = 0; slot < 36; slot++) {
            ItemStack item = pi.getItem(slot);
            if (item != null) inv.setItem(slot, item.clone());
        }

        placeArmor(inv, 36, pi.getHelmet());
        placeArmor(inv, 37, pi.getChestplate());
        placeArmor(inv, 38, pi.getLeggings());
        placeArmor(inv, 39, pi.getBoots());
        placeArmor(inv, 40, pi.getItemInOffHand());

        inv.setItem(41, ItemBuilder.make(Material.CYAN_STAINED_GLASS_PANE,
                "&b&lCasque (slot 36)"));
        inv.setItem(42, ItemBuilder.make(Material.CYAN_STAINED_GLASS_PANE,
                "&b&lPlastron (slot 37)"));
        inv.setItem(43, ItemBuilder.make(Material.CYAN_STAINED_GLASS_PANE,
                "&b&lJambières (slot 38)"));
        inv.setItem(44, ItemBuilder.make(Material.CYAN_STAINED_GLASS_PANE,
                "&b&lBottes (slot 39)"));

        inv.setItem(45, ItemBuilder.make(Material.ARROW,
                "&7&l◀ Retour",
                "&7Retourner aux actions du joueur",
                "&7(les modifications sont &asauvegardées&7)"
        ));

        inv.setItem(49, ItemBuilder.make(Material.LIME_STAINED_GLASS_PANE,
                "&a&lSauvegarde automatique",
                "&7Toutes vos modifications sont",
                "&7appliquées en temps réel."
        ));

        inv.setItem(53, ItemBuilder.make(Material.CYAN_STAINED_GLASS_PANE,
                "&b&lMain secondaire (slot 40)"));

        admin.openInventory(inv);
    }

    private void placeArmor(Inventory inv, int slot, ItemStack item) {
        if (item != null && item.getType() != Material.AIR) {
            inv.setItem(slot, item.clone());
        }
    }

    public void saveToPlayer(Player admin, Inventory editorInv, UUID targetUUID) {
        Player target = Bukkit.getPlayer(targetUUID);
        if (target == null) {
            admin.sendMessage(ItemBuilder.c("&cImpossible de sauvegarder : &e" + targetUUID + " &cn'est plus en ligne."));
            return;
        }

        PlayerInventory pi = target.getInventory();

        for (int slot = 0; slot < 36; slot++) {
            pi.setItem(slot, editorInv.getItem(slot));
        }

        pi.setHelmet(editorInv.getItem(36));
        pi.setChestplate(editorInv.getItem(37));
        pi.setLeggings(editorInv.getItem(38));
        pi.setBoots(editorInv.getItem(39));
        pi.setItemInOffHand(safeItem(editorInv.getItem(40)));

        target.updateInventory();
    }

    private ItemStack safeItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) return new ItemStack(Material.AIR);
        return item;
    }

    public static int getSlotBack() { return 45; }

    public static boolean isControlSlot(int slot) {
        return slot >= 41 && slot <= 54 && slot != 45;
    }
}
