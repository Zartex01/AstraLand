package com.astraland.oneblock.models;

import org.bukkit.Location;

import java.util.*;

public class OneBlockIsland {

    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> pendingInvites = new HashSet<>();
    private Location blockLocation;
    private Location home;
    private long blocksBroken;
    private Phase currentPhase;

    private boolean pvpEnabled = false;
    private boolean visitorsAllowed = true;
    private boolean warpEnabled = false;
    private String warpName = "";

    private final Map<String, Integer> upgrades = new HashMap<>();
    private final Map<String, Long> challengeProgress = new HashMap<>();
    private final Set<String> completedChallenges = new HashSet<>();

    public OneBlockIsland(UUID owner, Location blockLocation) {
        this.owner = owner;
        this.blockLocation = blockLocation;
        this.home = blockLocation.clone().add(0, 2, 0);
        this.blocksBroken = 0;
        this.currentPhase = Phase.PLAINES;
        for (UpgradeType t : UpgradeType.values()) upgrades.put(t.getId(), 0);
    }

    public void incrementBlocks() {
        blocksBroken++;
        currentPhase = Phase.getPhase(blocksBroken);
        addChallengeProgress(IslandChallenge.ChallengeType.BLOCKS_BROKEN, 1);
    }

    public void addChallengeProgress(IslandChallenge.ChallengeType type, long amount) {
        for (IslandChallenge ch : IslandChallenge.values()) {
            if (ch.getType() != type) continue;
            if (completedChallenges.contains(ch.getId())) continue;
            long current = getChallengeProgress(ch);
            challengeProgress.put(ch.getId(), current + amount);
        }
    }

    public long getChallengeProgress(IslandChallenge ch) {
        switch (ch.getType()) {
            case BLOCKS_BROKEN: return blocksBroken;
            case PHASE_REACHED:
                Phase[] phases = Phase.values();
                for (int i = 0; i < phases.length; i++) if (phases[i] == currentPhase) return i + 1;
                return 1;
            default: return challengeProgress.getOrDefault(ch.getId(), 0L);
        }
    }

    public boolean isChallengeCompleted(IslandChallenge ch) {
        return completedChallenges.contains(ch.getId());
    }

    public boolean isChallengeClaimable(IslandChallenge ch) {
        return !completedChallenges.contains(ch.getId()) && getChallengeProgress(ch) >= ch.getTarget();
    }

    public void completeChallenge(IslandChallenge ch) {
        completedChallenges.add(ch.getId());
    }

    public int getUpgradeLevel(UpgradeType type) {
        return upgrades.getOrDefault(type.getId(), 0);
    }

    public void setUpgradeLevel(UpgradeType type, int level) {
        upgrades.put(type.getId(), level);
    }

    public long getIslandLevel() { return blocksBroken / 100; }

    public boolean isOwner(UUID uuid) { return uuid.equals(owner); }
    public boolean isMember(UUID uuid) { return uuid.equals(owner) || members.contains(uuid); }
    public void addMember(UUID uuid) { members.add(uuid); pendingInvites.remove(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); }

    public void invite(UUID uuid) { pendingInvites.add(uuid); }
    public boolean isInvited(UUID uuid) { return pendingInvites.contains(uuid); }
    public void removeInvite(UUID uuid) { pendingInvites.remove(uuid); }

    public UUID getOwner() { return owner; }
    public Set<UUID> getMembers() { return members; }
    public Set<UUID> getPendingInvites() { return pendingInvites; }
    public Location getBlockLocation() { return blockLocation; }
    public void setBlockLocation(Location blockLocation) { this.blockLocation = blockLocation; }
    public Location getHome() { return home; }
    public void setHome(Location home) { this.home = home; }
    public long getBlocksBroken() { return blocksBroken; }
    public void setBlocksBroken(long n) { this.blocksBroken = n; this.currentPhase = Phase.getPhase(n); }
    public Phase getCurrentPhase() { return currentPhase; }

    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean pvpEnabled) { this.pvpEnabled = pvpEnabled; }
    public boolean isVisitorsAllowed() { return visitorsAllowed; }
    public void setVisitorsAllowed(boolean visitorsAllowed) { this.visitorsAllowed = visitorsAllowed; }
    public boolean isWarpEnabled() { return warpEnabled; }
    public void setWarpEnabled(boolean warpEnabled) { this.warpEnabled = warpEnabled; }
    public String getWarpName() { return warpName; }
    public void setWarpName(String warpName) { this.warpName = warpName; }

    public Map<String, Integer> getUpgrades() { return upgrades; }
    public Map<String, Long> getChallengeProgressMap() { return challengeProgress; }
    public Set<String> getCompletedChallenges() { return completedChallenges; }
}
