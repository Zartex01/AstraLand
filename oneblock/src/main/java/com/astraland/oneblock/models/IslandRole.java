package com.astraland.oneblock.models;

public enum IslandRole {
    OWNER(3, "Propriétaire", "&6★"),
    CO_OWNER(2, "Co-propriétaire", "&eDiamond"),
    MEMBER(1, "Membre", "&aGold"),
    VISITOR(0, "Visiteur", "&7Iron");

    private final int level;
    private final String displayName;
    private final String prefix;

    IslandRole(int level, String displayName, String prefix) {
        this.level = level;
        this.displayName = displayName;
        this.prefix = prefix;
    }

    public int getLevel() { return level; }
    public String getDisplayName() { return displayName; }
    public String getPrefix() { return prefix; }

    public boolean canManageMembers() { return level >= 2; }
    public boolean canAccessBank() { return level >= 2; }
    public boolean canModifySettings() { return level >= 3; }
}
