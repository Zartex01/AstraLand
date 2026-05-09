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

## Commandes

- **Compiler tous les modules** : `mvn compile`
- **Générer tous les JARs** : `mvn package`
- **Nettoyer et reconstruire** : `mvn clean package`

Les JARs sont générés dans le dossier `target/` de chaque module.

## Environnement

- Java 21
- Maven 3.8.6
- Paper API 1.21.4-R0.1-SNAPSHOT

## User Preferences

- Langue : Français
- Projet : Plugin Minecraft multi-modules (1.21.4 Paper)
- Un .jar par mode de jeu
