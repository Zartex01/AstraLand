package com.astraland.oneblock.models;

import org.bukkit.Location;

import java.util.*;

public class OneBlockIsland {

    private final UUID owner;
    private final Set<UUID> members = new HashSet<>();
    private final Set<UUID> coOwners = new HashSet<>();
    private final Set<UUID> pendingInvites = new HashSet<>();
    private Location blockLocation;
    private Location home;
    private long blocksBroken;
    private Phase currentPhase;

    private boolean pvpEnabled = false;
    private boolean visitorsAllowed = true;
    private boolean warpEnabled = false;
    private String warpName = "";
    private String motd = "";

    private long bankBalance = 0;
    private int prestige = 0;
    private long islandWorth = 0;

    private final Map<String, Integer> upgrades = new HashMap<>();
    private final Map<String, Long> challengeProgress = new HashMap<>();
    private final Set<String> completedChallenges = new HashSet<>();
    private final Map<String, Long> collections = new HashMap<>();
    private final Map<String, Integer> claimedMilestones = new HashMap<>();

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
            challengeProgress.put(ch.getId(), getChallengeProgress(ch) + amount);
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

    public boolean isChallengeCompleted(IslandChallenge ch) { return completedChallenges.contains(ch.getId()); }
    public boolean isChallengeClaimable(IslandChallenge ch) {
        return !completedChallenges.contains(ch.getId()) && getChallengeProgress(ch) >= ch.getTarget();
    }
    public void completeChallenge(IslandChallenge ch) { completedChallenges.add(ch.getId()); }

    public int getUpgradeLevel(UpgradeType type) { return upgrades.getOrDefault(type.getId(), 0); }
    public void setUpgradeLevel(UpgradeType type, int level) { upgrades.put(type.getId(), level); }

    public long getIslandLevel() { return blocksBroken / 100 + (long) prestige * 50; }

    public boolean isOwner(UUID uuid) { return uuid.equals(owner); }
    public boolean isCoOwner(UUID uuid) { return coOwners.contains(uuid); }
    public boolean isMember(UUID uuid) { return uuid.equals(owner) || coOwners.contains(uuid) || members.contains(uuid); }

    public IslandRole getRole(UUID uuid) {
        if (uuid.equals(owner)) return IslandRole.OWNER;
        if (coOwners.contains(uuid)) return IslandRole.CO_OWNER;
        if (members.contains(uuid)) return IslandRole.MEMBER;
        return IslandRole.VISITOR;
    }

    public void addMember(UUID uuid) { members.add(uuid); pendingInvites.remove(uuid); }
    public void removeMember(UUID uuid) { members.remove(uuid); coOwners.remove(uuid); }
    public void addCoOwner(UUID uuid) { coOwners.add(uuid); members.remove(uuid); }
    public void removeCoOwner(UUID uuid) { coOwners.remove(uuid); }

    public void invite(UUID uuid) { pendingInvites.add(uuid); }
    public boolean isInvited(UUID uuid) { return pendingInvites.contains(uuid); }
    public void removeInvite(UUID uuid) { pendingInvites.remove(uuid); }

    public void addToCollection(String materialName, long amount) {
        collections.merge(materialName, amount, Long::sum);
    }
    public long getCollection(String materialName) { return collections.getOrDefault(materialName, 0L); }

    public int getClaimedMilestone(String collectionId) { return claimedMilestones.getOrDefault(collectionId, -1); }
    public void setClaimedMilestone(String collectionId, int milestoneIndex) {
        claimedMilestones.put(collectionId, milestoneIndex);
    }

    public boolean depositToBank(int amount) { bankBalance += amount; return true; }
    public boolean withdrawFromBank(int amount) {
        if (bankBalance < amount) return false;
        bankBalance -= amount;
        return true;
    }

    public void addWorth(long amount) { islandWorth += amount; }
    public void removeWorth(long amount) { islandWorth = Math.max(0, islandWorth - amount); }

    public void doPrestige() {
        blocksBroken = 0;
        currentPhase = Phase.PLAINES;
        prestige++;
        challengeProgress.clear();
        completedChallenges.clear();
        for (UpgradeType t : UpgradeType.values()) upgrades.put(t.getId(), 0);
    }

    public double getPrestigeMultiplier() { return 1.0 + prestige * 0.10; }

    public UUID getOwner() { return owner; }
    public Set<UUID> getMembers() { return members; }
    public Set<UUID> getCoOwners() { return coOwners; }
    public Set<UUID> getPendingInvites() { return pendingInvites; }
    public Location getBlockLocation() { return blockLocation; }
    public void setBlockLocation(Location l) { this.blockLocation = l; }
    public Location getHome() { return home; }
    public void setHome(Location h) { this.home = h; }
    public long getBlocksBroken() { return blocksBroken; }
    public void setBlocksBroken(long n) { this.blocksBroken = n; this.currentPhase = Phase.getPhase(n); }
    public Phase getCurrentPhase() { return currentPhase; }
    public boolean isPvpEnabled() { return pvpEnabled; }
    public void setPvpEnabled(boolean v) { this.pvpEnabled = v; }
    public boolean isVisitorsAllowed() { return visitorsAllowed; }
    public void setVisitorsAllowed(boolean v) { this.visitorsAllowed = v; }
    public boolean isWarpEnabled() { return warpEnabled; }
    public void setWarpEnabled(boolean v) { this.warpEnabled = v; }
    public String getWarpName() { return warpName; }
    public void setWarpName(String v) { this.warpName = v; }
    public String getMotd() { return motd; }
    public void setMotd(String motd) { this.motd = motd; }
    public long getBankBalance() { return bankBalance; }
    public void setBankBalance(long v) { this.bankBalance = v; }
    public int getPrestige() { return prestige; }
    public void setPrestige(int v) { this.prestige = v; }
    public long getIslandWorth() { return islandWorth; }
    public void setIslandWorth(long v) { this.islandWorth = v; }
    public Map<String, Integer> getUpgrades() { return upgrades; }
    public Map<String, Long> getChallengeProgressMap() { return challengeProgress; }
    public Set<String> getCompletedChallenges() { return completedChallenges; }
    public Map<String, Long> getCollections() { return collections; }
    public Map<String, Integer> getClaimedMilestones() { return claimedMilestones; }

    public Set<UUID> getAllMemberUUIDs() {
        Set<UUID> all = new HashSet<>();
        all.add(owner);
        all.addAll(coOwners);
        all.addAll(members);
        return all;
    }
}
