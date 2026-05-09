package com.astraland.bedwars.models;

import org.bukkit.Location;

import java.util.*;

public class Arena {

    private final String name;
    private GameState state;
    private final Map<String, BedwarsTeam> teams = new LinkedHashMap<>();
    private final Map<UUID, String> playerTeam = new HashMap<>();
    private Location lobby;
    private int countdown;
    private int maxPlayers;
    private int minPlayers;

    public Arena(String name, int minPlayers, int maxPlayers) {
        this.name = name;
        this.state = GameState.WAITING;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.countdown = 30;

        teams.put("RED", new BedwarsTeam("RED", "&c"));
        teams.put("BLUE", new BedwarsTeam("BLUE", "&9"));
        teams.put("GREEN", new BedwarsTeam("GREEN", "&a"));
        teams.put("YELLOW", new BedwarsTeam("YELLOW", "&e"));
    }

    public boolean addPlayer(UUID uuid) {
        if (getPlayerCount() >= maxPlayers) return false;
        BedwarsTeam smallest = teams.values().stream()
            .min(Comparator.comparingInt(t -> t.getPlayers().size()))
            .orElse(null);
        if (smallest == null) return false;
        smallest.addPlayer(uuid);
        playerTeam.put(uuid, smallest.getName());
        return true;
    }

    public void removePlayer(UUID uuid) {
        String teamName = playerTeam.remove(uuid);
        if (teamName != null) {
            BedwarsTeam t = teams.get(teamName);
            if (t != null) t.removePlayer(uuid);
        }
    }

    public BedwarsTeam getPlayerTeam(UUID uuid) {
        String t = playerTeam.get(uuid);
        return t == null ? null : teams.get(t);
    }

    public int getPlayerCount() {
        return teams.values().stream().mapToInt(t -> t.getPlayers().size()).sum();
    }

    public List<BedwarsTeam> getAliveTeams() {
        List<BedwarsTeam> alive = new ArrayList<>();
        for (BedwarsTeam t : teams.values()) {
            if (!t.getPlayers().isEmpty() || t.isBedAlive()) alive.add(t);
        }
        return alive;
    }

    public boolean isInArena(UUID uuid) { return playerTeam.containsKey(uuid); }

    public String getName() { return name; }
    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public Map<String, BedwarsTeam> getTeams() { return teams; }
    public Map<UUID, String> getPlayerTeamMap() { return playerTeam; }
    public Location getLobby() { return lobby; }
    public void setLobby(Location lobby) { this.lobby = lobby; }
    public int getCountdown() { return countdown; }
    public void setCountdown(int countdown) { this.countdown = countdown; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
}
