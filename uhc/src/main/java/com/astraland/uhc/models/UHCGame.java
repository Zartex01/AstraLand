package com.astraland.uhc.models;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class UHCGame {

    public enum State { WAITING, STARTING, INGAME, DEATHMATCH, FINISHED }

    private State state;
    private final Set<UUID> players = new HashSet<>();
    private final Set<UUID> alive = new HashSet<>();
    private final Map<UUID, Integer> kills = new HashMap<>();
    private boolean gracePeriod;
    private long startTime;
    private String scenario;

    public UHCGame() {
        this.state = State.WAITING;
        this.gracePeriod = true;
        this.scenario = "VANILLA";
    }

    public void addPlayer(UUID uuid) {
        players.add(uuid);
        alive.add(uuid);
        kills.put(uuid, 0);
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
        alive.remove(uuid);
    }

    public void killPlayer(UUID victim, UUID killer) {
        alive.remove(victim);
        if (killer != null) kills.merge(killer, 1, Integer::sum);
    }

    public boolean isAlive(UUID uuid) { return alive.contains(uuid); }
    public boolean isInGame(UUID uuid) { return players.contains(uuid); }
    public int getKills(UUID uuid) { return kills.getOrDefault(uuid, 0); }

    public UUID getWinner() {
        if (alive.size() == 1) return alive.iterator().next();
        return null;
    }

    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public Set<UUID> getPlayers() { return players; }
    public Set<UUID> getAlive() { return alive; }
    public Map<UUID, Integer> getKills() { return kills; }
    public boolean isGracePeriod() { return gracePeriod; }
    public void setGracePeriod(boolean gp) { this.gracePeriod = gp; }
    public long getStartTime() { return startTime; }
    public void setStartTime(long t) { this.startTime = t; }
    public String getScenario() { return scenario; }
    public void setScenario(String s) { this.scenario = s; }
}
