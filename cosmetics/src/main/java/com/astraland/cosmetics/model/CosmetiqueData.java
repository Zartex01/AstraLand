package com.astraland.cosmetics.model;

import java.util.List;

public class CosmetiqueData {

    private final String id;
    private final String nom;
    private final List<String> description;
    private final String item;
    private final boolean enchante;
    private final int slot;
    private final String permission;
    private final String commandeJoueur;
    private final String commandeConsole;

    public CosmetiqueData(String id, String nom, List<String> description, String item,
                          boolean enchante, int slot, String permission,
                          String commandeJoueur, String commandeConsole) {
        this.id              = id;
        this.nom             = nom;
        this.description     = description;
        this.item            = item;
        this.enchante        = enchante;
        this.slot            = slot;
        this.permission      = permission;
        this.commandeJoueur  = commandeJoueur;
        this.commandeConsole = commandeConsole;
    }

    public String getId()              { return id; }
    public String getNom()             { return nom; }
    public List<String> getDescription() { return description; }
    public String getItem()            { return item; }
    public boolean isEnchante()        { return enchante; }
    public int getSlot()               { return slot; }
    public String getPermission()      { return permission; }
    public String getCommandeJoueur()  { return commandeJoueur; }
    public String getCommandeConsole() { return commandeConsole; }

    public boolean hasPermission()     { return permission != null && !permission.isEmpty(); }
    public boolean hasCommandeJoueur() { return commandeJoueur != null && !commandeJoueur.isEmpty(); }
    public boolean hasCommandeConsole(){ return commandeConsole != null && !commandeConsole.isEmpty(); }
}
