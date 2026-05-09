package com.astraland.bedwars.models;

import org.bukkit.Location;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class BedwarsTeam {

    private final String name;
    private final String color;
    private final Set<UUID> players = new HashSet<>();
    private boolean bedAlive;
    private Location spawn;
    private Location bedLocation;

    public BedwarsTeam(String name, String color) {
        this.name = name;
        this.color = color;
        this.bedAlive = true;
    }

    public boolean isEliminated() { return !bedAlive && players.isEmpty(); }

    public void addPlayer(UUID uuid) { players.add(uuid); }
    public void removePlayer(UUID uuid) { players.remove(uuid); }
    public boolean hasPlayer(UUID uuid) { return players.contains(uuid); }

    public String getName() { return name; }
    public String getColor() { return color; }
    public Set<UUID> getPlayers() { return players; }
    public boolean isBedAlive() { return bedAlive; }
    public void setBedAlive(boolean alive) { this.bedAlive = alive; }
    public Location getSpawn() { return spawn; }
    public void setSpawn(Location spawn) { this.spawn = spawn; }
    public Location getBedLocation() { return bedLocation; }
    public void setBedLocation(Location bedLocation) { this.bedLocation = bedLocation; }
}
