package com.astraland.cosmetics.manager;

import com.astraland.cosmetics.Cosmetics;
import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class SkinManager {

    private final Cosmetics plugin;

    public SkinManager(Cosmetics plugin) {
        this.plugin = plugin;
    }

    public void changeSkin(Player player, String username) {
        player.sendMessage(c("&7Récupération du skin de &e" + username + "&7..."));

        Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
            try {
                String uuid = fetchUUID(username);
                if (uuid == null) {
                    sync(() -> player.sendMessage(c("&cLe compte &e" + username + " &cn'existe pas.")));
                    return;
                }

                String[] textureData = fetchTextures(uuid);
                if (textureData == null) {
                    sync(() -> player.sendMessage(c("&cImpossible de récupérer les données du skin.")));
                    return;
                }

                String value     = textureData[0];
                String signature = textureData[1];

                sync(() -> applySkin(player, value, signature, username));

            } catch (Exception e) {
                sync(() -> player.sendMessage(c("&cErreur : " + e.getMessage())));
                plugin.getLogger().warning("Erreur SkinManager : " + e.getMessage());
            }
        });
    }

    private String fetchUUID(String username) throws Exception {
        HttpURLConnection conn = openConnection(
                "https://api.mojang.com/users/profiles/minecraft/" + username);
        if (conn.getResponseCode() != 200) return null;

        String body = readBody(conn);
        int start = body.indexOf("\"id\":\"") + 6;
        int end   = body.indexOf("\"", start);
        return (start > 5 && end > start) ? body.substring(start, end) : null;
    }

    private String[] fetchTextures(String uuid) throws Exception {
        HttpURLConnection conn = openConnection(
                "https://sessionserver.mojang.com/session/minecraft/profile/" + uuid + "?unsigned=false");
        if (conn.getResponseCode() != 200) return null;

        String body = readBody(conn);

        int vStart = body.indexOf("\"value\":\"") + 9;
        int vEnd   = body.indexOf("\"", vStart);
        if (vStart < 9 || vEnd < vStart) return null;
        String value = body.substring(vStart, vEnd);

        int sStart = body.indexOf("\"signature\":\"") + 13;
        int sEnd   = body.indexOf("\"", sStart);
        String signature = (sStart > 13 && sEnd > sStart) ? body.substring(sStart, sEnd) : "";

        return new String[]{value, signature};
    }

    private void applySkin(Player player, String value, String signature, String username) {
        if (!player.isOnline()) return;
        try {
            PlayerProfile profile = player.getPlayerProfile();
            profile.removeProperty("textures");
            profile.setProperty(new ProfileProperty("textures", value, signature));
            player.setPlayerProfile(profile);

            for (Player other : Bukkit.getOnlinePlayers()) {
                if (other.equals(player)) continue;
                other.hidePlayer(plugin, player);
                other.showPlayer(plugin, player);
            }

            player.sendMessage(c("&a✔ Skin changé en &e" + username + " &a!"));
            player.sendMessage(c("&8(Reconnectez-vous pour voir votre propre skin)"));

        } catch (Exception e) {
            player.sendMessage(c("&cErreur lors de l'application du skin."));
            plugin.getLogger().warning("Erreur applySkin : " + e.getMessage());
        }
    }

    private HttpURLConnection openConnection(String urlStr) throws Exception {
        URL url = new URL(urlStr);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setConnectTimeout(6000);
        conn.setReadTimeout(6000);
        conn.setRequestProperty("User-Agent", "AstraLand-Plugin/1.0");
        return conn;
    }

    private String readBody(HttpURLConnection conn) throws Exception {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
            String line;
            while ((line = reader.readLine()) != null) sb.append(line);
        }
        return sb.toString();
    }

    private void sync(Runnable task) {
        Bukkit.getScheduler().runTask(plugin, task);
    }

    private String c(String text) {
        return ChatColor.translateAlternateColorCodes('&', text);
    }
}
