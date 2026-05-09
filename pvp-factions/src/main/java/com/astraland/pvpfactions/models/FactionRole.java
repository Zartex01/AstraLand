package com.astraland.pvpfactions.models;

public enum FactionRole {
    MEMBER("Membre"),
    OFFICER("Officier"),
    LEADER("Chef");

    private final String display;

    FactionRole(String display) { this.display = display; }

    public String getDisplay() { return display; }
}
