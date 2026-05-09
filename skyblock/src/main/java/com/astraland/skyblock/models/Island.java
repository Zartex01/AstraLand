package com.astraland.skyblock.models;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class Island {

    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> invited = new HashSet<>();
    private Location home;
    private Location center;
    private int level;
    private long blocksBroken;
    private final long createdAt;

    public Island(UUID owner, Location center) {
        this.owner = owner;
        this.center = center;
        this.home = center.clone().add(0, 1, 0);
        this.level = 0;
        this.blocksBroken = 0;
        this.createdAt = System.currentTimeMillis();
    }

    public boolean isOwner(UUID uuid) { return uuid.equals(owner); }
    public boolean isMember(UUID uuid) { return uuid.equals(owner) || members.contains(uuid); }
    public boolean isInvited(UUID uuid) { return invited.contains(uuid); }

    public void addMember(UUID uuid) { members.add(uuid); invited.remove(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }
    public void invite(UUID uuid) { invited.add(uuid); }
    public void uninvite(UUID uuid) { invited.remove(uuid); }

    public boolean isInsideIsland(Location loc, int size) {
        if (center == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(center.getWorld())) return false;
        double half = size / 2.0;
        return Math.abs(loc.getX() - center.getX()) <= half
            && Math.abs(loc.getZ() - center.getZ()) <= half;
    }

    public UUID getOwner() { return owner; }
    public Set<UUID> getMembers() { return members; }
    public Set<UUID> getInvited() { return invited; }
    public Location getHome() { return home; }
    public void setHome(Location home) { this.home = home; }
    public Location getCenter() { return center; }
    public void setCenter(Location center) { this.center = center; }
    public int getLevel() { return level; }
    public void setLevel(int level) { this.level = level; }
    public long getBlocksBroken() { return blocksBroken; }
    public void addBlocksBroken(long n) { this.blocksBroken += n; }
    public long getCreatedAt() { return createdAt; }
}
