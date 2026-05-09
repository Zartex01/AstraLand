package com.astraland.duels.models;

import org.bukkit.Location;

import java.util.UUID;

public class DuelMatch {

    private final UUID player1;
    private final UUID player2;
    private final String kit;
    private final Location arena1;
    private final Location arena2;
    private boolean finished;
    private UUID winner;
    private final long startTime;

    public DuelMatch(UUID player1, UUID player2, String kit, Location arena1, Location arena2) {
        this.player1 = player1;
        this.player2 = player2;
        this.kit = kit;
        this.arena1 = arena1;
        this.arena2 = arena2;
        this.finished = false;
        this.startTime = System.currentTimeMillis();
    }

    public boolean hasPlayer(UUID uuid) {
        return uuid.equals(player1) || uuid.equals(player2);
    }

    public UUID getOpponent(UUID uuid) {
        if (uuid.equals(player1)) return player2;
        if (uuid.equals(player2)) return player1;
        return null;
    }

    public Location getSpawn(UUID uuid) {
        return uuid.equals(player1) ? arena1 : arena2;
    }

    public void finish(UUID winner) {
        this.winner = winner;
        this.finished = true;
    }

    public UUID getPlayer1() { return player1; }
    public UUID getPlayer2() { return player2; }
    public String getKit() { return kit; }
    public boolean isFinished() { return finished; }
    public UUID getWinner() { return winner; }
    public long getStartTime() { return startTime; }
}
