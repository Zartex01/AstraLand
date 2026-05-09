package com.astraland.pvpfactions.models;

import org.bukkit.Location;

import java.util.*;

public class Faction {

    private String name;
    private String tag;
    private String description;
    private String motd;
    private boolean open;
    private UUID leader;
    private final Map<UUID, FactionRole> members = new LinkedHashMap<>();
    private final Set<String> allies = new HashSet<>();
    private final Set<String> enemies = new HashSet<>();
    private double power;
    private final Set<String> claims = new HashSet<>();
    private Location home;
    private final Map<String, Location> warps = new LinkedHashMap<>();

    public Faction(String name, UUID leader) {
        this.name = name;
        this.tag = name.length() >= 4 ? name.substring(0, 4).toUpperCase() : name.toUpperCase();
        this.description = "Une faction AstraLand.";
        this.motd = null;
        this.open = false;
        this.leader = leader;
        this.power = 10.0;
        this.members.put(leader, FactionRole.LEADER);
    }

    public boolean isMember(UUID uuid) { return members.containsKey(uuid); }
    public boolean isOfficer(UUID uuid) {
        FactionRole r = members.get(uuid);
        return r == FactionRole.OFFICER || r == FactionRole.LEADER;
    }
    public boolean isLeader(UUID uuid) { return uuid.equals(leader); }

    public void addMember(UUID uuid) { members.put(uuid, FactionRole.MEMBER); }
    public void removeMember(UUID uuid) { members.remove(uuid); }

    public void promote(UUID uuid) {
        if (members.get(uuid) == FactionRole.MEMBER) members.put(uuid, FactionRole.OFFICER);
    }
    public void demote(UUID uuid) {
        if (members.get(uuid) == FactionRole.OFFICER) members.put(uuid, FactionRole.MEMBER);
    }

    public String getClaimKey(org.bukkit.Chunk chunk) {
        return chunk.getWorld().getName() + ":" + chunk.getX() + ":" + chunk.getZ();
    }
    public boolean hasClaim(org.bukkit.Chunk chunk) { return claims.contains(getClaimKey(chunk)); }
    public void addClaim(org.bukkit.Chunk chunk) { claims.add(getClaimKey(chunk)); }
    public void removeClaim(org.bukkit.Chunk chunk) { claims.remove(getClaimKey(chunk)); }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getMotd() { return motd; }
    public void setMotd(String motd) { this.motd = motd; }
    public boolean isOpen() { return open; }
    public void setOpen(boolean open) { this.open = open; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public Map<UUID, FactionRole> getMembers() { return members; }
    public Set<String> getAllies() { return allies; }
    public Set<String> getEnemies() { return enemies; }
    public double getPower() { return power; }
    public void setPower(double power) { this.power = power; }
    public void addPower(double amount) { this.power += amount; }
    public void removePower(double amount) { this.power = Math.max(0, this.power - amount); }
    public Set<String> getClaims() { return claims; }
    public Location getHome() { return home; }
    public void setHome(Location home) { this.home = home; }
    public Map<String, Location> getWarps() { return warps; }
    public int getMaxClaims() { return (int) Math.floor(power); }
}
