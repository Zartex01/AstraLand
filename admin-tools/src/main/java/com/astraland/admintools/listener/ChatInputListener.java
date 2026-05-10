package com.astraland.admintools.listener;

import com.astraland.admintools.AdminTools;
import com.astraland.admintools.session.AdminSession;
import com.astraland.admintools.session.PendingInput;
import com.astraland.admintools.util.ItemBuilder;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class ChatInputListener implements Listener {

    private final AdminTools plugin;

    public ChatInputListener(AdminTools plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        Player admin = event.getPlayer();
        AdminSession session = plugin.getSessionIfExists(admin);
        if (session == null || !session.isWaitingInput()) return;

        event.setCancelled(true);
        String input = event.getMessage().trim();
        PendingInput pending = session.getPendingInput();
        UUID targetUUID = session.getTargetPlayerUUID();
        session.clearPendingInput();

        if (input.equalsIgnoreCase("annuler") || input.equalsIgnoreCase("cancel")) {
            admin.sendMessage(ItemBuilder.c("&cSaisie annulée."));
            Bukkit.getScheduler().runTask(plugin, () -> reopenAfterCancel(admin, pending, targetUUID));
            return;
        }

        Bukkit.getScheduler().runTask(plugin, () -> processInput(admin, session, pending, targetUUID, input));
    }

    private void processInput(Player admin, AdminSession session, PendingInput pending, UUID targetUUID, String input) {
        switch (pending) {
            case FILTER_TEXT -> {
                session.setFilterText(input);
                session.setPage(0);
                admin.sendMessage(ItemBuilder.c("&aFiltre appliqué : &e\"" + input + "\""));
                plugin.getPlayerListGUI().open(admin);
            }
            case HEALTH -> applyDouble(admin, targetUUID, input, pending, "vie", 0.0, (t, v) -> {
                double maxHealth = t.getMaxHealth();
                double clamped = Math.min(v, maxHealth);
                t.setHealth(clamped);
                admin.sendMessage(ItemBuilder.c("&a✔ Vie de &b" + t.getName() + " &adéfinie à &c" + String.format("%.1f", clamped)));
                t.sendMessage(ItemBuilder.c("&7[Admin] &cVotre vie a été modifiée."));
                plugin.getPlayerStatsGUI().open(admin, targetUUID);
            });
            case MAX_HEALTH -> applyDouble(admin, targetUUID, input, pending, "vie maximum", 1.0, (t, v) -> {
                double clamped = Math.max(1.0, Math.min(v, 1024.0));
                t.setMaxHealth(clamped);
                if (t.getHealth() > clamped) t.setHealth(clamped);
                admin.sendMessage(ItemBuilder.c("&a✔ Vie maximum de &b" + t.getName() + " &adéfinie à &6" + String.format("%.1f", clamped)));
                plugin.getPlayerStatsGUI().open(admin, targetUUID);
            });
            case FOOD -> applyInt(admin, targetUUID, input, pending, "faim", (t, v) -> {
                int clamped = Math.max(0, Math.min(v, 20));
                t.setFoodLevel(clamped);
                admin.sendMessage(ItemBuilder.c("&a✔ Faim de &b" + t.getName() + " &adéfinie à &e" + clamped));
                plugin.getPlayerStatsGUI().open(admin, targetUUID);
            });
            case XP_LEVEL -> applyInt(admin, targetUUID, input, pending, "niveau XP", (t, v) -> {
                int clamped = Math.max(0, v);
                t.setLevel(clamped);
                admin.sendMessage(ItemBuilder.c("&a✔ Niveau XP de &b" + t.getName() + " &adéfini à &a" + clamped));
                plugin.getPlayerStatsGUI().open(admin, targetUUID);
            });
            case SEND_SCREEN_MSG -> {
                Player target = Bukkit.getPlayer(targetUUID);
                if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
                target.sendTitle(ItemBuilder.c("&f" + input), "", 10, 60, 20);
                target.sendActionBar(ItemBuilder.c("&7[Admin] " + input));
                admin.sendMessage(ItemBuilder.c("&a✔ Message affiché sur l'écran de &b" + target.getName()));
                plugin.getPlayerActionsGUI().open(admin, targetUUID);
            }
            case SEND_CHAT_MSG -> {
                Bukkit.broadcastMessage(ItemBuilder.c("&8[&6Admin&8] &f" + input));
                admin.sendMessage(ItemBuilder.c("&a✔ Message diffusé dans le chat public."));
                Player target = Bukkit.getPlayer(targetUUID);
                if (target != null) plugin.getPlayerActionsGUI().open(admin, targetUUID);
                else plugin.getPlayerListGUI().open(admin);
            }
        }
    }

    private void reopenAfterCancel(Player admin, PendingInput pending, UUID targetUUID) {
        if (pending == PendingInput.FILTER_TEXT) {
            plugin.getPlayerListGUI().open(admin);
        } else if (targetUUID != null) {
            if (pending == PendingInput.SEND_SCREEN_MSG || pending == PendingInput.SEND_CHAT_MSG) {
                plugin.getPlayerActionsGUI().open(admin, targetUUID);
            } else {
                plugin.getPlayerStatsGUI().open(admin, targetUUID);
            }
        } else {
            plugin.getPlayerListGUI().open(admin);
        }
    }

    private void applyDouble(Player admin, UUID targetUUID, String input, PendingInput pending,
                             String label, double min, StatDoubleApplier applier) {
        try {
            double value = Double.parseDouble(input);
            if (value < min) {
                admin.sendMessage(ItemBuilder.c("&cValeur trop petite. Minimum : &e" + min));
                return;
            }
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            applier.apply(target, value);
        } catch (NumberFormatException e) {
            admin.sendMessage(ItemBuilder.c("&cValeur invalide. Entrez un nombre pour &e" + label + "&c."));
        }
    }

    private void applyInt(Player admin, UUID targetUUID, String input, PendingInput pending,
                          String label, StatIntApplier applier) {
        try {
            int value = Integer.parseInt(input);
            Player target = Bukkit.getPlayer(targetUUID);
            if (target == null) { admin.sendMessage(ItemBuilder.c("&cJoueur hors ligne.")); return; }
            applier.apply(target, value);
        } catch (NumberFormatException e) {
            admin.sendMessage(ItemBuilder.c("&cValeur invalide. Entrez un entier pour &e" + label + "&c."));
        }
    }

    @FunctionalInterface
    interface StatDoubleApplier {
        void apply(Player target, double value);
    }

    @FunctionalInterface
    interface StatIntApplier {
        void apply(Player target, int value);
    }
}
