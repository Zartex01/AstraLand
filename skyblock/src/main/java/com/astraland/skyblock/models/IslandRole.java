package com.astraland.skyblock.models;

public enum IslandRole {

    OWNER   ("&6Propriétaire", "&6★", true,  true,  true,  true,  true),
    OFFICER ("&eOfficier",     "&eO", true,  true,  true,  true,  false),
    MEMBER  ("&7Membre",       "&7M", true,  false, false, false, false);

    private final String displayName;
    private final String prefix;
    private final boolean canBuild;
    private final boolean canInvite;
    private final boolean canKickMembers;
    private final boolean canSettings;
    private final boolean canUpgrade;

    IslandRole(String displayName, String prefix, boolean canBuild,
               boolean canInvite, boolean canKickMembers,
               boolean canSettings, boolean canUpgrade) {
        this.displayName     = displayName;
        this.prefix          = prefix;
        this.canBuild        = canBuild;
        this.canInvite       = canInvite;
        this.canKickMembers  = canKickMembers;
        this.canSettings     = canSettings;
        this.canUpgrade      = canUpgrade;
    }

    public String getDisplayName()  { return displayName; }
    public String getPrefix()       { return prefix; }
    public boolean canBuild()       { return canBuild; }
    public boolean canInvite()      { return canInvite; }
    public boolean canKickMembers() { return canKickMembers; }
    public boolean canSettings()    { return canSettings; }
    public boolean canUpgrade()     { return canUpgrade; }
}
