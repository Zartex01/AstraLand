package com.astraland.skywars.models;

import org.bukkit.Location;

import java.util.*;

public class SkywarsArena {

    public enum State { WAITING, COUNTDOWN, INGAME, FINISHED }

    private final String name;
    private State state;
    private final Map<UUID, String> playerKits = new HashMap<>();
    private final Set<UUID> players = new HashSet<>();
    private final List<Location> spawns = new ArrayList<>();
    private Location lobby;
    private int minPlayers;
    private int maxPlayers;
    private int countdown;

    public SkywarsArena(String name, int minPlayers, int maxPlayers) {
        this.name = name;
        this.state = State.WAITING;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.countdown = 30;
    }

    public boolean addPlayer(UUID uuid) {
        if (players.size() >= maxPlayers) return false;
        return players.add(uuid);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
        playerKits.remove(uuid);
    }

    public boolean isInArena(UUID uuid) { return players.contains(uuid); }

    public Location getSpawnFor(UUID uuid) {
        int idx = new ArrayList<>(players).indexOf(uuid);
        if (spawns.isEmpty()) return lobby;
        return spawns.get(idx % spawns.size());
    }

    public UUID getWinner() {
        if (players.size() == 1) return players.iterator().next();
        return null;
    }

    public String getName() { return name; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public Set<UUID> getPlayers() { return players; }
    public Map<UUID, String> getPlayerKits() { return playerKits; }
    public List<Location> getSpawns() { return spawns; }
    public Location getLobby() { return lobby; }
    public void setLobby(Location lobby) { this.lobby = lobby; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getCountdown() { return countdown; }
}
