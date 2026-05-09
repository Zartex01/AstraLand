package com.astraland.oneblock.models;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class OneBlockIsland {

    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private Location blockLocation;
    private Location home;
    private long blocksBroken;
    private Phase currentPhase;

    public OneBlockIsland(UUID owner, Location blockLocation) {
        this.owner = owner;
        this.blockLocation = blockLocation;
        this.home = blockLocation.clone().add(0, 2, 0);
        this.blocksBroken = 0;
        this.currentPhase = Phase.PLAINES;
    }

    public void incrementBlocks() {
        blocksBroken++;
        currentPhase = Phase.getPhase(blocksBroken);
    }

    public boolean isOwner(UUID uuid) { return uuid.equals(owner); }
    public boolean isMember(UUID uuid) { return uuid.equals(owner) || members.contains(uuid); }
    public void addMember(UUID uuid) { members.add(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }

    public UUID getOwner() { return owner; }
    public Set<UUID> getMembers() { return members; }
    public Location getBlockLocation() { return blockLocation; }
    public void setBlockLocation(Location blockLocation) { this.blockLocation = blockLocation; }
    public Location getHome() { return home; }
    public void setHome(Location home) { this.home = home; }
    public long getBlocksBroken() { return blocksBroken; }
    public void setBlocksBroken(long n) { this.blocksBroken = n; this.currentPhase = Phase.getPhase(n); }
    public Phase getCurrentPhase() { return currentPhase; }
}
