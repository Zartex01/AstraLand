package com.astraland.duels.models;

import java.util.UUID;

public class DuelRequest {

    private final UUID challenger;
    private final UUID challenged;
    private final String kit;
    private final long timestamp;

    public DuelRequest(UUID challenger, UUID challenged, String kit) {
        this.challenger = challenger;
        this.challenged = challenged;
        this.kit = kit;
        this.timestamp = System.currentTimeMillis();
    }

    public boolean isExpired(long timeoutMillis) {
        return System.currentTimeMillis() - timestamp > timeoutMillis;
    }

    public UUID getChallenger() { return challenger; }
    public UUID getChallenged() { return challenged; }
    public String getKit() { return kit; }
    public long getTimestamp() { return timestamp; }
}
