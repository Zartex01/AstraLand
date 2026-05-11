package com.astraland.skyblock.models;

import org.bukkit.Location;

import java.util.*;

public class Island {

    private final UUID owner;
    private String name;

    private final Set<UUID> members      = new HashSet<>();
    private final Set<UUID> invited      = new HashSet<>();
    private final Set<UUID> coopPlayers  = new HashSet<>();
    private final Set<UUID> bannedPlayers = new HashSet<>();

    private Location home;
    private Location center;

    private int  level;
    private long value;
    private long blocksBroken;
    private final long createdAt;

    private boolean locked;
    private boolean pvpEnabled;
    private boolean warpEnabled;
    private String  warpName;
    private int     generatorLevel;
    private int     memberSlots;

    private boolean visitorsCanBuild;
    private boolean visitorsCanBreak;
    private boolean visitorsCanOpenChests;

    // ── Nouveaux champs ───────────────────────────────────────────────────────
    private boolean flyUpgrade;
    private boolean keepInventoryUpgrade;
    private int     memberSlotsUpgrade;   // nombre d'upgrades achetées (max 3)
    private long    bankBalance;          // Banque commune de l'île

    public Island(UUID owner, Location center) {
        this.owner         = owner;
        this.name          = "Île";
        this.center        = center;
        this.home          = center.clone().add(0, 2, 0);
        this.level         = 0;
        this.value         = 0;
        this.blocksBroken  = 0;
        this.createdAt     = System.currentTimeMillis();
        this.locked        = false;
        this.pvpEnabled    = false;
        this.warpEnabled   = false;
        this.warpName      = "";
        this.generatorLevel = 0;
        this.memberSlots   = 3;
        this.visitorsCanBuild       = false;
        this.visitorsCanBreak       = false;
        this.visitorsCanOpenChests  = false;
        this.flyUpgrade             = false;
        this.keepInventoryUpgrade   = false;
        this.memberSlotsUpgrade     = 0;
        this.bankBalance            = 0;
    }

    public boolean isOwner(UUID uuid)    { return uuid.equals(owner); }
    public boolean isMember(UUID uuid)   { return uuid.equals(owner) || members.contains(uuid) || coopPlayers.contains(uuid); }
    public boolean isInvited(UUID uuid)  { return invited.contains(uuid); }
    public boolean isBanned(UUID uuid)   { return bannedPlayers.contains(uuid); }
    public boolean isCoop(UUID uuid)     { return coopPlayers.contains(uuid); }

    public void addMember(UUID uuid)     { members.add(uuid);     invited.remove(uuid); }
    public void removeMember(UUID uuid)  { members.remove(uuid); }
    public void invite(UUID uuid)        { invited.add(uuid); }
    public void uninvite(UUID uuid)      { invited.remove(uuid); }
    public void addCoop(UUID uuid)       { coopPlayers.add(uuid); }
    public void removeCoop(UUID uuid)    { coopPlayers.remove(uuid); }
    public void banPlayer(UUID uuid)     { bannedPlayers.add(uuid); members.remove(uuid); coopPlayers.remove(uuid); invited.remove(uuid); }
    public void unbanPlayer(UUID uuid)   { bannedPlayers.remove(uuid); }

    public boolean isInsideIsland(Location loc, int size) {
        if (center == null || loc.getWorld() == null) return false;
        if (!loc.getWorld().equals(center.getWorld())) return false;
        double half = size / 2.0;
        return Math.abs(loc.getX() - center.getX()) <= half
            && Math.abs(loc.getZ() - center.getZ()) <= half;
    }

    public boolean canVisit()            { return !locked && warpEnabled; }

    // ── Getters / Setters standards ───────────────────────────────────────────

    public UUID    getOwner()            { return owner; }
    public String  getName()             { return name; }
    public void    setName(String n)     { this.name = n; }
    public Set<UUID> getMembers()        { return Collections.unmodifiableSet(members); }
    public Set<UUID> getCoopPlayers()    { return Collections.unmodifiableSet(coopPlayers); }
    public Set<UUID> getBannedPlayers()  { return Collections.unmodifiableSet(bannedPlayers); }
    public Set<UUID> getInvited()        { return Collections.unmodifiableSet(invited); }

    public Location getHome()            { return home; }
    public void     setHome(Location l)  { this.home = l; }
    public Location getCenter()          { return center; }
    public void     setCenter(Location l){ this.center = l; }

    public int  getLevel()               { return level; }
    public void setLevel(int l)          { this.level = l; }
    public long getValue()               { return value; }
    public void setValue(long v)         { this.value = Math.max(0, v); }
    public long getBlocksBroken()        { return blocksBroken; }
    public void addBlocksBroken(long n)  { this.blocksBroken += n; }

    public boolean isLocked()               { return locked; }
    public void    setLocked(boolean b)     { this.locked = b; }
    public boolean isPvpEnabled()           { return pvpEnabled; }
    public void    setPvpEnabled(boolean b) { this.pvpEnabled = b; }
    public boolean isWarpEnabled()          { return warpEnabled; }
    public void    setWarpEnabled(boolean b){ this.warpEnabled = b; }
    public String  getWarpName()            { return warpName; }
    public void    setWarpName(String n)    { this.warpName = n; }
    public int     getGeneratorLevel()      { return generatorLevel; }
    public void    setGeneratorLevel(int l) { this.generatorLevel = l; }
    public int     getMemberSlots()         { return memberSlots; }
    public void    setMemberSlots(int s)    { this.memberSlots = s; }
    public boolean isVisitorsCanBuild()     { return visitorsCanBuild; }
    public void    setVisitorsCanBuild(boolean b)      { this.visitorsCanBuild = b; }
    public boolean isVisitorsCanBreak()     { return visitorsCanBreak; }
    public void    setVisitorsCanBreak(boolean b)      { this.visitorsCanBreak = b; }
    public boolean isVisitorsCanOpenChests(){ return visitorsCanOpenChests; }
    public void    setVisitorsCanOpenChests(boolean b) { this.visitorsCanOpenChests = b; }
    public long    getCreatedAt()           { return createdAt; }
    public int     getMemberCount()         { return members.size(); }

    // ── Nouveaux getters/setters ──────────────────────────────────────────────

    public boolean hasFlyUpgrade()             { return flyUpgrade; }
    public void    setFlyUpgrade(boolean b)    { this.flyUpgrade = b; }

    public boolean hasKeepInventoryUpgrade()          { return keepInventoryUpgrade; }
    public void    setKeepInventoryUpgrade(boolean b) { this.keepInventoryUpgrade = b; }

    public int  getMemberSlotsUpgrade()           { return memberSlotsUpgrade; }
    public void setMemberSlotsUpgrade(int n)      { this.memberSlotsUpgrade = n; }

    public long getBankBalance()                  { return bankBalance; }
    public void setBankBalance(long v)            { this.bankBalance = Math.max(0, v); }

    public boolean depositToBank(long amount) {
        if (amount <= 0) return false;
        this.bankBalance += amount;
        return true;
    }

    public boolean withdrawFromBank(long amount) {
        if (amount <= 0 || bankBalance < amount) return false;
        this.bankBalance -= amount;
        return true;
    }
}
