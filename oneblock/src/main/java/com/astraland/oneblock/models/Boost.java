package com.astraland.oneblock.models;

import org.bukkit.Material;

public class Boost {

    public enum BoostType {
        SMALL("Petit Boost", "×1.5 sur tous les gains", Material.EXPERIENCE_BOTTLE, 1.5, 5 * 60 * 1000L, 750),
        MEDIUM("Boost Moyen", "×2 sur tous les gains", Material.BLAZE_POWDER, 2.0, 15 * 60 * 1000L, 2000),
        LARGE("Grand Boost", "×2.5 sur tous les gains", Material.NETHER_STAR, 2.5, 30 * 60 * 1000L, 5000),
        MEGA("MEGA Boost", "×3 sur tous les gains (30 min)", Material.BEACON, 3.0, 30 * 60 * 1000L, 15000);

        private final String displayName;
        private final String description;
        private final Material icon;
        private final double multiplier;
        private final long durationMs;
        private final int cost;

        BoostType(String displayName, String description, Material icon,
                  double multiplier, long durationMs, int cost) {
            this.displayName = displayName;
            this.description = description;
            this.icon = icon;
            this.multiplier = multiplier;
            this.durationMs = durationMs;
            this.cost = cost;
        }

        public String getDisplayName() { return displayName; }
        public String getDescription() { return description; }
        public Material getIcon() { return icon; }
        public double getMultiplier() { return multiplier; }
        public long getDurationMs() { return durationMs; }
        public int getCost() { return cost; }

        public String formatDuration() {
            long mins = durationMs / 60000;
            return mins + " min";
        }
    }

    private final BoostType type;
    private final long expiryTime;

    public Boost(BoostType type) {
        this.type = type;
        this.expiryTime = System.currentTimeMillis() + type.getDurationMs();
    }

    public Boost(BoostType type, long expiryTime) {
        this.type = type;
        this.expiryTime = expiryTime;
    }

    public boolean isActive() { return System.currentTimeMillis() < expiryTime; }
    public long getSecondsRemaining() { return Math.max(0, (expiryTime - System.currentTimeMillis()) / 1000); }
    public double getMultiplier() { return isActive() ? type.getMultiplier() : 1.0; }
    public BoostType getType() { return type; }
    public long getExpiryTime() { return expiryTime; }

    public String formatRemaining() {
        long secs = getSecondsRemaining();
        long m = secs / 60;
        long s = secs % 60;
        return m + "m " + s + "s";
    }
}
