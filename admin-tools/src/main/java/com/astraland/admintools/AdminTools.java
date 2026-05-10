package com.astraland.admintools;

import com.astraland.admintools.command.StaffCommand;
import com.astraland.admintools.gui.PlayerActionsGUI;
import com.astraland.admintools.gui.PlayerInventoryGUI;
import com.astraland.admintools.gui.PlayerListGUI;
import com.astraland.admintools.gui.PlayerStatsGUI;
import com.astraland.admintools.listener.ChatInputListener;
import com.astraland.admintools.listener.GUIListener;
import com.astraland.admintools.session.AdminSession;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AdminTools extends JavaPlugin {

    private static AdminTools instance;

    private PlayerListGUI      playerListGUI;
    private PlayerActionsGUI   playerActionsGUI;
    private PlayerInventoryGUI playerInventoryGUI;
    private PlayerStatsGUI     playerStatsGUI;

    private final Map<UUID, AdminSession> sessions = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;
        saveDefaultConfig();

        this.playerListGUI      = new PlayerListGUI(this);
        this.playerActionsGUI   = new PlayerActionsGUI(this);
        this.playerInventoryGUI = new PlayerInventoryGUI(this);
        this.playerStatsGUI     = new PlayerStatsGUI(this);

        getCommand("staff").setExecutor(new StaffCommand(this));

        getServer().getPluginManager().registerEvents(new GUIListener(this), this);
        getServer().getPluginManager().registerEvents(new ChatInputListener(this), this);

        getLogger().info("================================");
        getLogger().info(" AstraLand - AdminTools chargé !");
        getLogger().info(" Commande : /staff");
        getLogger().info(" Permission : astraland.staff");
        getLogger().info("================================");
    }

    @Override
    public void onDisable() {
        sessions.clear();
        getLogger().info("AstraLand - AdminTools désactivé.");
    }

    public AdminSession getSession(Player player) {
        return sessions.computeIfAbsent(player.getUniqueId(), k -> new AdminSession());
    }

    public AdminSession getSessionIfExists(Player player) {
        return sessions.get(player.getUniqueId());
    }

    public void clearSession(Player player) {
        sessions.remove(player.getUniqueId());
    }

    public static AdminTools getInstance()             { return instance; }
    public PlayerListGUI getPlayerListGUI()            { return playerListGUI; }
    public PlayerActionsGUI getPlayerActionsGUI()      { return playerActionsGUI; }
    public PlayerInventoryGUI getPlayerInventoryGUI()  { return playerInventoryGUI; }
    public PlayerStatsGUI getPlayerStatsGUI()          { return playerStatsGUI; }
}
