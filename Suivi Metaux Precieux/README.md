# Suivi Métaux — application Android

Application Android native (Kotlin + Jetpack Compose) pour suivre un portefeuille de métaux
précieux (or, argent, platine, palladium) : cours en direct, valorisation du portefeuille,
plus-value, historique des cours, alertes de prix.

## Fonctionnalités

- **Portefeuille** : ajout d'avoirs (lingot, pièce, bijou, autre) avec poids en grammes, date
  d'achat, et photo optionnelle.
- **Valorisation** : valeur actuelle et plus-value (%) calculées à partir du cours en direct.
  Le prix d'achat peut être saisi manuellement, ou calculé automatiquement à partir du cours
  historique mis en cache (voir limite ci-dessous).
- **Cours en direct** : Or, Argent, Platine, Palladium, affichables en EUR ou USD (bascule dans
  le tableau de bord et les réglages).
- **Historique des cours** : graphique par métal, sur 7 / 30 / 90 / 365 jours.
- **Répartition du portefeuille** : graphique en anneau par métal.
- **Alertes de prix** : notification quand un cours dépasse ou descend sous un seuil défini.
- **Sauvegarde** : export / import des avoirs au format JSON (aucune donnée envoyée en ligne,
  tout reste sur l'appareil).

## Architecture

```
app/src/main/java/com/preciousmetals/tracker/
├── domain/model/       modèles métier (Metal, Holding, PortfolioSummary, PriceAlert…)
├── data/
│   ├── local/           Room (entités, DAO, base de données)
│   ├── remote/           Retrofit (gold-api.com, frankfurter.app) + DTO
│   ├── preferences/      DataStore (devise, fréquence de rafraîchissement, taux de change…)
│   ├── repository/       agrège local + remote (PriceRepository, HoldingRepository, …)
│   └── export/           export/import JSON
├── work/                 WorkManager : rafraîchissement périodique + vérification des alertes
├── ui/                   un package par écran (dashboard, addholding, history, alerts, settings)
│                         + theme, navigation, composants partagés (graphiques maison en Canvas)
├── AppContainer.kt       conteneur d'injection de dépendances "fait main" (pas de Hilt)
└── MainActivity.kt / SuiviMetauxApp.kt
```

Pas de framework d'injection de dépendances (Hilt/Dagger) : `AppContainer` construit et
partage les dépendances, exposé via un `CompositionLocal` (`LocalAppContainer`). Pas de
bibliothèque de graphiques externe : les graphiques (anneau, courbe) sont dessinés directement
avec `Canvas` en Compose.

## Sources de données (gratuites, sans clé API)

- **Cours en direct** : [gold-api.com](https://gold-api.com) — Or/Argent/Platine/Palladium en
  USD, pas de clé requise.
- **Taux de change USD → EUR** : [frankfurter.app](https://www.frankfurter.app) (Banque
  centrale européenne), pas de clé requise.

### Limite connue : calcul automatique du prix d'achat

Il n'existe pas d'API gratuite et sans clé fournissant l'historique des cours des métaux
précieux. L'application construit donc son propre historique localement, à chaque
rafraîchissement (par défaut toutes les 6 h, réglable). Résultat :

- Pour un achat effectué **après** l'installation de l'app, le calcul automatique du prix
  d'achat (option "Calcul auto") fonctionne dès que l'historique atteint cette date.
- Pour un achat **antérieur** à l'installation, aucun cours historique n'est disponible : il
  faut saisir le prix réellement payé (option "Prix payé"). L'écran d'ajout d'un avoir explique
  ce point et bascule automatiquement l'utilisateur vers cette option si besoin.

Pour aller plus loin, `PriceRepository` peut être étendu avec un fournisseur d'historique tiers
(ex. metals-api.com, metalpriceapi.com) nécessitant une clé API, sans changer le reste de
l'application.

## Compiler le projet

Ce projet a été généré dans un environnement sans SDK Android installé (le wrapper Gradle est
prêt, mais aucune compilation Android complète n'a pu être exécutée ici). Pour builder :

1. Ouvrir le dossier dans **Android Studio** (Koala/2024.1 ou plus récent recommandé).
2. Laisser Android Studio télécharger le SDK/build-tools nécessaires (compileSdk 35).
3. Synchroniser Gradle (`./gradlew build` ou bouton "Sync" dans l'IDE).
4. Lancer sur un émulateur ou un appareil (API 26+ / Android 8.0+).

Aucune clé API à configurer : l'application fonctionne dès l'installation.

## Prochaines pistes (non implémentées)

- Widget écran d'accueil affichant le cours de l'or.
- Fournisseur d'historique tiers avec clé API optionnelle (voir ci-dessus).
- Sauvegarde automatique chiffrée (Google Drive) en plus de l'export manuel.
