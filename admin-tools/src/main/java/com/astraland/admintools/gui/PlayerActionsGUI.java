package com.astraland.admintools.gui;

import com.astraland.admintools.AdminTools;
import com.astraland.admintools.gui.holder.PlayerActionsHolder;
import com.astraland.admintools.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerActionsGUI {

    private final AdminTools plugin;

    public PlayerActionsGUI(AdminTools plugin) {
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

        Inventory inv = Bukkit.createInventory(new PlayerActionsHolder(), 45,
                ItemBuilder.c("&8» &b" + target.getName() + " &8- &7Actions «"));

        ItemStack gray = ItemBuilder.glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 45; i++) inv.setItem(i, gray);

        inv.setItem(4, ItemBuilder.skull(target,
                "&7Monde : &e" + target.getWorld().getName(),
                "&7Mode   : &e" + target.getGameMode().name().toLowerCase(),
                "&7Vie    : &c" + String.format("%.1f", target.getHealth()) + " &7/ &c" + String.format("%.1f", target.getMaxHealth()),
                "&7Faim   : &6" + target.getFoodLevel() + " &7/ &620",
                "&7Niveau : &a" + target.getLevel(),
                "&7Ping   : &e" + target.getPing() + "ms"
        ));

        inv.setItem(10, ItemBuilder.make(Material.CHEST,
                "&e&lInventaire",
                "&7Ouvrir et modifier l'inventaire",
                "&7de &b" + target.getName(),
                "",
                "&aCliquez pour ouvrir"
        ));

        inv.setItem(12, ItemBuilder.make(Material.NETHER_STAR,
                "&d&lStatistiques",
                "&7Modifier la vie, la faim,",
                "&7l'XP, le mode de jeu, etc.",
                "",
                "&aCliquez pour ouvrir"
        ));

        inv.setItem(14, ItemBuilder.make(Material.FIREWORK_ROCKET,
                "&6&lViens sur Discord &8(Écran + Particules)",
                "&7Affiche le message &6Viens sur Discord",
                "&7sur l'écran de &b" + target.getName(),
                "&7avec des &dparticules &7autour d'eux",
                "",
                "&aCliquez pour envoyer"
        ));

        inv.setItem(16, ItemBuilder.make(Material.WRITABLE_BOOK,
                "&6&lViens sur Discord &8(Chat public)",
                "&7Diffuse le message &6Viens sur Discord",
                "&7dans le chat &fpublic &7pour tous les joueurs",
                "",
                "&aCliquez pour diffuser"
        ));

        inv.setItem(28, ItemBuilder.make(Material.BARRIER,
                "&c&lVider l'inventaire",
                "&7Supprime tous les items de",
                "&7l'inventaire de &b" + target.getName(),
                "",
                "&c&lAttention : action irréversible !",
                "&aCliquez pour vider"
        ));

        inv.setItem(31, ItemBuilder.make(Material.PAPER,
                "&b&lMessage personnalisé &8(Écran)",
                "&7Envoyer un message personnalisé",
                "&7sur l'écran de &b" + target.getName(),
                "",
                "&aCliquez pour saisir le message"
        ));

        inv.setItem(33, ItemBuilder.make(Material.PAPER,
                "&7&lMessage personnalisé &8(Chat public)",
                "&7Diffuser un message personnalisé",
                "&7dans le chat &fpublic",
                "",
                "&aCliquez pour saisir le message"
        ));

        inv.setItem(40, ItemBuilder.make(Material.ARROW,
                "&7&l◀ Retour",
                "&7Retourner à la liste des joueurs"
        ));

        admin.openInventory(inv);
    }

    public static int getSlotInventory()     { return 10; }
    public static int getSlotStats()         { return 12; }
    public static int getSlotDiscordScreen() { return 14; }
    public static int getSlotDiscordChat()   { return 16; }
    public static int getSlotClearInv()      { return 28; }
    public static int getSlotCustomScreen()  { return 31; }
    public static int getSlotCustomChat()    { return 33; }
    public static int getSlotBack()          { return 40; }
}
