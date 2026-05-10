# AstraLand - Plugin Minecraft 1.21.4

Projet multi-modules Maven pour le serveur Minecraft AstraLand (Paper 1.21.4).
Chaque module produit un `.jar` indépendant à déployer sur le serveur.

## Structure du projet

| Dossier | Plugin | Description |
|---|---|---|
| `startup/` | AstraLand-Startup.jar | Module principal de démarrage |
| `pvp-factions/` | AstraLand-PvpFactions.jar | PvP et Factions |
| `skyblock/` | AstraLand-Skyblock.jar | Mode Skyblock |
| `oneblock/` | AstraLand-OneBlock.jar | Mode OneBlock |
| `bedwars/` | AstraLand-Bedwars.jar | Mode Bedwars |
| `spleef/` | AstraLand-Spleef.jar | Mode Spleef |
| `uhc/` | AstraLand-UHC.jar | Mode UHC |
| `skywars/` | AstraLand-Skywars.jar | Mode Skywars |
| `build-battle/` | AstraLand-BuildBattle.jar | Mode Build Battle |
| `duels/` | AstraLand-Duels.jar | Mode Duels |
| `admin-tools/` | AstraLand-AdminTools.jar | Outils d'administration (gestion joueurs) |
| `cosmetics/` | AstraLand-Cosmetics.jar | Skins et Cosmétiques |

## Commandes

- **Compiler tous les modules** : `mvn compile`
- **Générer tous les JARs** : `mvn package`
- **Nettoyer et reconstruire** : `mvn clean package`

Les JARs sont générés dans le dossier `target/` de chaque module.

## Module Admin Tools

Plugin standalone à déposer dans `plugins/` de **tous** les serveurs.

### Commande
- `/staff` (alias : `/admin`, `/gestion`, `/playerlist`)
- Permission : `astraland.staff` (op par défaut)

### Fonctionnalités
1. **Liste des joueurs** — têtes des joueurs en ligne, paginée, avec filtres
2. **Filtres** — Contient / Commence par / Termine par / Exact (tous visibles simultanément)
3. **Actions sur un joueur** :
   - Voir et modifier son inventaire (drag & drop, items persistants)
   - Voir et modifier ses stats (vie, vie max, faim, XP, mode de jeu, vol, invulnérabilité)
   - Vider son inventaire
   - Afficher "Viens sur Discord" sur son écran avec particules
   - Diffuser "Viens sur Discord" dans le chat public
   - Envoyer un message personnalisé sur son écran
   - Diffuser un message personnalisé dans le chat public

### Configuration (`plugins/AstraLand-AdminTools/config.yml`)
```yaml
discord:
  lien: "discord.gg/astraland"
  message-ecran-titre: "&6&lViens sur Discord !"
  message-ecran-sous-titre: "&7{lien}"
  message-chat: "&8[&6AstraLand&8] &fViens sur Discord ! &7{lien}"
  particules: true
```

## Environnement

- Java 21
- Maven 3.8.6
- Paper API 1.21.4-R0.1-SNAPSHOT

## User Preferences

- Langue : Français
- Projet : Plugin Minecraft multi-modules (1.21.4 Paper)
- Un .jar par mode de jeu
