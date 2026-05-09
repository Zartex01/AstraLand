package com.astraland.buildbattle.models;

import org.bukkit.Location;

import java.util.*;

public class BBGame {

    public enum State { WAITING, COUNTDOWN, BUILDING, VOTING, FINISHED }

    private final String name;
    private State state;
    private final Set<UUID> players = new HashSet<>();
    private final Map<UUID, Integer> votes = new HashMap<>();
    private final Map<UUID, Integer> scores = new HashMap<>();
    private String currentTheme;
    private UUID currentBuilder;
    private final List<UUID> buildOrder = new ArrayList<>();
    private int buildOrderIndex;
    private Location lobby;
    private int minPlayers;
    private int maxPlayers;

    public BBGame(String name, int minPlayers, int maxPlayers) {
        this.name = name;
        this.state = State.WAITING;
        this.minPlayers = minPlayers;
        this.maxPlayers = maxPlayers;
    }

    public boolean addPlayer(UUID uuid) {
        if (players.size() >= maxPlayers) return false;
        players.add(uuid);
        scores.put(uuid, 0);
        return true;
    }

    public void removePlayer(UUID uuid) {
        players.remove(uuid);
        scores.remove(uuid);
        buildOrder.remove(uuid);
    }

    public void startBuildOrder() {
        buildOrder.clear();
        buildOrder.addAll(players);
        Collections.shuffle(buildOrder);
        buildOrderIndex = 0;
        nextBuilder();
    }

    public void nextBuilder() {
        if (buildOrderIndex < buildOrder.size()) {
            currentBuilder = buildOrder.get(buildOrderIndex++);
        } else {
            currentBuilder = null;
        }
    }

    public void addVote(UUID voter, int score) { votes.put(voter, score); }

    public void tallyVotes() {
        int total = votes.values().stream().mapToInt(Integer::intValue).sum();
        int count = votes.size();
        if (count > 0 && currentBuilder != null) {
            int avg = (int) Math.round((double) total / count);
            scores.merge(currentBuilder, avg, Integer::sum);
        }
        votes.clear();
    }

    public UUID getTopScorer() {
        return scores.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .orElse(null);
    }

    public boolean isInGame(UUID uuid) { return players.contains(uuid); }

    public String getName() { return name; }
    public State getState() { return state; }
    public void setState(State state) { this.state = state; }
    public Set<UUID> getPlayers() { return players; }
    public Map<UUID, Integer> getVotes() { return votes; }
    public Map<UUID, Integer> getScores() { return scores; }
    public String getCurrentTheme() { return currentTheme; }
    public void setCurrentTheme(String theme) { this.currentTheme = theme; }
    public UUID getCurrentBuilder() { return currentBuilder; }
    public Location getLobby() { return lobby; }
    public void setLobby(Location lobby) { this.lobby = lobby; }
    public int getMinPlayers() { return minPlayers; }
    public int getMaxPlayers() { return maxPlayers; }
    public boolean hasMoreBuilders() { return buildOrderIndex < buildOrder.size(); }
}
