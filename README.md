# Convertisseur De Monnaies

> Projet Android — ECE Paris
> Auteur : **Mohamed GALY**
> Licence : MIT

---

## Description

Application Android de conversion de devises en temps réel.
Elle récupère les taux de change officiels publiés par la **Banque Centrale Européenne (BCE)** et permet à l'utilisateur de convertir un montant entre différentes monnaies du monde.

En cas d'absence de connexion internet, l'application bascule automatiquement en **mode hors ligne** et utilise les données préalablement enregistrées en local.

---

## Fonctionnalités

- Récupération en temps réel des taux de change depuis le flux XML de la BCE
- Conversion entre toutes les devises disponibles (base EUR = 1)
- Mode hors ligne : stockage local des taux via SQLite
- Synchronisation des données sur **Firebase Realtime Database**
- Géolocalisation par adresse : affichage du pays correspondant à une adresse saisie
- Affichage de la liste complète des taux de change disponibles
- Indicateur visuel du mode de connexion (en ligne / hors ligne)

---

## Architecture

```
app/
└── src/main/java/com/example/eceandroidproject/
    ├── MainActivity.java        — Activité principale : conversion et géolocalisation
    ├── ListActivity.java        — Activité secondaire : liste des taux de change
    ├── DownloadFileTask.java    — Tâche asynchrone de téléchargement du flux BCE
    ├── DB_Sqlite.java           — Gestionnaire base de données SQLite locale
    └── FireBaseManager.java     — Synchronisation Firebase Realtime Database
```

---

## Technologies utilisées

| Technologie | Rôle |
|---|---|
| Java | Langage principal |
| Android SDK (API 16+) | Plateforme cible |
| AsyncTask | Téléchargement asynchrone du flux XML |
| XML Parser (DOM) | Parsing du flux BCE |
| SQLite | Persistance locale des taux |
| Firebase Realtime Database | Synchronisation cloud |
| Android Geocoder | Géocodage d'adresses |
| AlertDialog | Retours utilisateur |

---

## Source des données

Les taux de change sont récupérés depuis le flux XML officiel de la BCE :
`https://www.ecb.europa.eu/stats/eurofxref/eurofxref-daily.xml`

L'EUR est utilisé comme devise de référence (taux = 1,0).
La conversion s'effectue selon la formule :

```
résultat = montant ÷ taux(devise source) × taux(devise cible)
```

---

## Installation

1. Cloner le dépôt :
   ```bash
   git clone https://github.com/votre-utilisateur/Convertisseur-De-Monnaies.git
   ```
2. Ouvrir le projet dans **Android Studio**
3. Ajouter votre fichier `google-services.json` (Firebase) dans `app/`
4. Compiler et exécuter sur un émulateur ou un appareil Android (API 16 minimum)

---

## Licence

Ce projet est distribué sous licence **MIT**.
Voir le fichier [LICENSE](LICENSE) pour plus de détails.
