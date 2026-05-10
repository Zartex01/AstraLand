package com.astraland.oneblock.listeners;

import com.astraland.oneblock.OneBlock;
import com.astraland.oneblock.managers.OneBlockManager;
import com.astraland.oneblock.models.OneBlockIsland;
import com.astraland.oneblock.models.Phase;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class OneBlockListener implements Listener {

    private final OneBlock plugin;

    public OneBlockListener(OneBlock plugin) { this.plugin = plugin; }

    private String c(String s) { return ChatColor.translateAlternateColorCodes('&', s); }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        String worldName = plugin.getConfig().getString("oneblock.world", "world_oneblock");
        if (!event.getBlock().getWorld().getName().equals(worldName)) return;

        OneBlockManager om = plugin.getOneBlockManager();
        OneBlockIsland island = om.getIsland(player.getUniqueId());

        if (island == null) {
            event.setCancelled(true);
            player.sendMessage(c("&cTu n'as pas d'île OneBlock."));
            return;
        }

        if (!event.getBlock().getLocation().equals(island.getBlockLocation())) {
            if (!island.isMember(player.getUniqueId())) {
                event.setCancelled(true);
                player.sendMessage(c("&cCette zone ne t'appartient pas."));
            }
            return;
        }

        int reward = plugin.getConfig().getInt("economy.block-reward", 2);
        plugin.getEconomyManager().addBalance(player.getUniqueId(), reward);

        Phase phaseBefore = island.getCurrentPhase();
        om.regenerateBlock(island);
        Phase phaseAfter = island.getCurrentPhase();

        if (!phaseBefore.equals(phaseAfter)) {
            String msg = c(phaseAfter.getColor() + "&lNouvelle phase débloquée : " + phaseAfter.getDisplayName() + " !");
            island.getMembers().forEach(uuid -> {
                Player p = Bukkit.getPlayer(uuid);
                if (p != null) p.sendMessage(msg);
            });
        }

        event.setDropItems(true);
        om.saveAll();
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        String worldName = plugin.getConfig().getString("oneblock.world", "world_oneblock");
        if (!event.getBlock().getWorld().getName().equals(worldName)) return;

        OneBlockManager om = plugin.getOneBlockManager();
        OneBlockIsland island = om.getIsland(player.getUniqueId());

        if (island == null || !island.isMember(player.getUniqueId())) {
            event.setCancelled(true);
            player.sendMessage(c("&cTu ne peux pas placer de blocs ici."));
        }
    }
}
