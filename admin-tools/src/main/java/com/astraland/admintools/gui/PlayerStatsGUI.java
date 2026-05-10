package com.astraland.admintools.gui;

import com.astraland.admintools.AdminTools;
import com.astraland.admintools.gui.holder.PlayerStatsHolder;
import com.astraland.admintools.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.UUID;

public class PlayerStatsGUI {

    private final AdminTools plugin;

    public PlayerStatsGUI(AdminTools plugin) {
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

        Inventory inv = Bukkit.createInventory(new PlayerStatsHolder(targetUUID), 45,
                ItemBuilder.c("&8» &bStats de &e" + target.getName() + " «"));

        ItemStack gray = ItemBuilder.glass(Material.GRAY_STAINED_GLASS_PANE);
        for (int i = 0; i < 45; i++) inv.setItem(i, gray);

        inv.setItem(4, ItemBuilder.skull(target,
                "&7Monde : &e" + target.getWorld().getName(),
                "&7Mode   : &e" + target.getGameMode().name().toLowerCase(),
                "&7Vie    : &c" + String.format("%.1f", target.getHealth()) + " / " + String.format("%.1f", target.getMaxHealth()),
                "&7Faim   : &6" + target.getFoodLevel() + " / 20",
                "&7Niveau XP : &a" + target.getLevel()
        ));

        inv.setItem(10, ItemBuilder.make(Material.REDSTONE,
                "&c&lVie",
                "&7Valeur actuelle : &c" + String.format("%.1f", target.getHealth()) + " / " + String.format("%.1f", target.getMaxHealth()),
                "&7Min : 0  |  Max : &c" + String.format("%.1f", target.getMaxHealth()),
                "",
                "&aCliquez pour modifier"
        ));

        inv.setItem(12, ItemBuilder.make(Material.GOLDEN_APPLE,
                "&6&lVie maximum",
                "&7Valeur actuelle : &6" + String.format("%.1f", target.getMaxHealth()),
                "&7Recommandé : &620.0",
                "",
                "&aCliquez pour modifier"
        ));

        inv.setItem(14, ItemBuilder.make(Material.BREAD,
                "&e&lFaim",
                "&7Valeur actuelle : &e" + target.getFoodLevel() + " / 20",
                "&7Entre &e0 &7et &e20",
                "",
                "&aCliquez pour modifier"
        ));

        inv.setItem(16, ItemBuilder.make(Material.EXPERIENCE_BOTTLE,
                "&a&lNiveau XP",
                "&7Valeur actuelle : &a" + target.getLevel() + " niveau(x)",
                "&7XP dans niveau : &a" + String.format("%.0f%%", target.getExp() * 100),
                "",
                "&aCliquez pour modifier"
        ));

        GameMode gm = target.getGameMode();
        Material gmMat = switch (gm) {
            case SURVIVAL  -> Material.GRASS_BLOCK;
            case CREATIVE  -> Material.COMMAND_BLOCK;
            case ADVENTURE -> Material.MAP;
            case SPECTATOR -> Material.ENDER_EYE;
        };
        inv.setItem(20, ItemBuilder.make(gmMat,
                "&b&lMode de jeu",
                "&7Actuel : &b" + gameModeLabel(gm),
                "",
                "&7Survie → Créatif → Aventure → Spectateur",
                "&aCliquez pour changer"
        ));

        boolean fly = target.getAllowFlight();
        inv.setItem(22, ItemBuilder.make(Material.FEATHER,
                "&9&lVol",
                "&7État actuel : " + (fly ? "&aActivé ✔" : "&cDésactivé ✗"),
                "",
                "&aCliquez pour " + (fly ? "&cdésactiver" : "&aactiver")
        ));

        boolean invul = target.isInvulnerable();
        inv.setItem(24, ItemBuilder.make(Material.SHIELD,
                "&7&lInvulnérabilité",
                "&7État actuel : " + (invul ? "&aActivée ✔" : "&cDésactivée ✗"),
                "",
                "&aCliquez pour " + (invul ? "&cdésactiver" : "&aactiver")
        ));

        inv.setItem(40, ItemBuilder.make(Material.ARROW,
                "&7&l◀ Retour",
                "&7Retourner aux actions du joueur"
        ));

        admin.openInventory(inv);
    }

    private String gameModeLabel(GameMode gm) {
        return switch (gm) {
            case SURVIVAL  -> "Survie";
            case CREATIVE  -> "Créatif";
            case ADVENTURE -> "Aventure";
            case SPECTATOR -> "Spectateur";
        };
    }

    public static int getSlotHealth()    { return 10; }
    public static int getSlotMaxHealth() { return 12; }
    public static int getSlotFood()      { return 14; }
    public static int getSlotXpLevel()   { return 16; }
    public static int getSlotGameMode()  { return 20; }
    public static int getSlotFly()       { return 22; }
    public static int getSlotGodMode()   { return 24; }
    public static int getSlotBack()      { return 40; }
}
