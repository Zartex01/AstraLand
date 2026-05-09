package com.astraland.spleef.models;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SpleefGame {

    public enum State { WAITING, COUNTDOWN, INGAME, FINISHED }

    private final String name;
    private State state;
    private final Set<UUID> players = new HashSet<>();
    private final Set<UUID> eliminated = new HashSet<>();
    private Location spawn;
    private Location lobby;
    private int minPlayers;
    private int maxPlayers;
    private int countdown;

    public SpleefGame(String name, int minPlayers, int maxPlayers) {
        this.name = name;
        this.state = State.WAITING;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
        this.countdown = 20;
    }

    public boolean addPlayer(UUID uuid) {
        if (players.size() >= maxPlayers) return false;
        return players.add(uuid);
    }

    public void removePlayer(UUID uuid) { players.remove(uuid); }

    public void eliminate(UUID uuid) {
        players.remove(uuid);
        eliminated.add(uuid);
    }

    public boolean isAlive(UUID uuid) { return players.contains(uuid); }
    public boolean isInGame(UUID uuid) { return players.contains(uuid) || eliminated.contains(uuid); }

    public UUID getWinner() {
        if (players.size() == 1) return players.iterator().next();
        return null;
    }

    public String getName() { return name; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public Set<UUID> getPlayers() { return players; }
    public Set<UUID> getEliminated() { return eliminated; }
    public Location getSpawn() { return spawn; }
    public void setSpawn(Location spawn) { this.spawn = spawn; }
    public Location getLobby() { return lobby; }
    public void setLobby(Location lobby) { this.lobby = lobby; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public int getCountdown() { return countdown; }
    public void setCountdown(int c) { this.countdown = c; }
}
