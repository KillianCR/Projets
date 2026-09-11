# Suivi Métaux — application Android

Application Android native (Kotlin + Jetpack Compose) pour suivre un portefeuille de métaux
précieux (or, argent, platine, palladium) : cours en direct, valorisation du portefeuille,
plus-value, historique des cours, alertes de prix.

## Fonctionnalités

- **Portefeuille** : ajout d'avoirs (lingot, pièce, bijou, autre) avec poids en grammes, date
  d'achat, et photo optionnelle. Recherche (nom, métal, type) et tri (récent / valeur /
  plus-value) dès que le portefeuille compte plusieurs avoirs. Suppression avec confirmation et
  "Annuler" via un snackbar (aucune suppression accidentelle irréversible).
- **Valorisation** : valeur actuelle et plus-value (%) calculées à partir du cours en direct.
  Le prix d'achat peut être saisi manuellement, ou calculé automatiquement à partir du cours
  historique mis en cache (voir "Historique des cours" ci-dessous).
- **Cours en direct** : Or, Argent, Platine, Palladium. Un bandeau défilant sous le titre du
  tableau de bord affiche le logo, le prix/g et une mini-courbe (7 jours) de chaque métal ;
  toucher un métal ouvre son graphique d'historique. Affichage en EUR ou USD, bascule discrète
  en haut à droite.
- **Historique des cours** : vrai graphique interactif par métal (courbe lissée, dégradé,
  grille, axes, infobulle au toucher/glisser), sur 7 / 30 / 90 / 365 jours, avec ~5 ans de
  profondeur grâce au rechargement automatique décrit ci-dessous.
- **Répartition du portefeuille** : graphique en anneau par métal.
- **Alertes de prix** : notification quand un cours dépasse ou descend sous un seuil défini.
- **Widget écran d'accueil** : cours de l'or, l'argent, le platine et le palladium (prix/g et
  variation vs veille) directement sur l'écran d'accueil, sans ouvrir l'app ; se rafraîchit avec
  chaque actualisation en arrière-plan. Toucher le widget ouvre l'application.
- **Verrouillage biométrique** (optionnel, dans Réglages) : empreinte, visage ou code de
  l'appareil requis à l'ouverture et à chaque retour au premier plan, pour protéger la
  valorisation de votre patrimoine en cas de téléphone déverrouillé laissé sans surveillance.
- **Sauvegarde** : export des avoirs en JSON (réimportable) ou CSV (pour tableur), et import
  JSON. Aucune donnée envoyée en ligne, tout reste sur l'appareil.

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
│                         (déclenche aussi le rafraîchissement du widget)
├── ui/                   un package par écran (dashboard, addholding, history, alerts, settings)
│                         + theme, navigation, composants partagés (graphiques maison en Canvas),
│                         lock (verrouillage biométrique via AppLockGate)
├── widget/               widget écran d'accueil (Jetpack Glance) affichant les 4 cours
├── AppContainer.kt       conteneur d'injection de dépendances "fait main" (pas de Hilt)
└── MainActivity.kt / SuiviMetauxApp.kt
```

Pas de framework d'injection de dépendances (Hilt/Dagger) : `AppContainer` construit et
partage les dépendances, exposé via un `CompositionLocal` (`LocalAppContainer`). Pas de
bibliothèque de graphiques externe : les graphiques (anneau, courbe, sparkline) sont dessinés
directement avec `Canvas` en Compose, avec dégradé, grille et infobulle interactive.

## Design

Look fintech sombre (dark-first), inspiré d'une référence visuelle fournie par l'utilisateur :
solde en gros chiffres, actions rapides en pilules, barre de navigation flottante arrondie
(icône + pilule colorée sur l'onglet actif), cartes arrondies. Palette entièrement recalculée
et validée avec le skill dataviz de Claude (bandes de luminosité/chroma OKLCH, séparation CVD
Delta E, contraste WCAG) plutôt que choisie à l'œil — voir `ui/theme/Color.kt` pour le détail
et la méthode. Icônes unifiées sur la famille "Outlined" de Material Icons (seul le bouton
d'ajout garde un remplissage, pour l'emphase).

## Sources de données (gratuites, sans compte ni clé API)

- **Cours en direct** : [gold-api.com](https://gold-api.com) — Or/Argent/Platine/Palladium en
  USD, pas de clé requise.
- **Taux de change USD → EUR** : [frankfurter.app](https://www.frankfurter.app) (Banque
  centrale européenne), pas de clé requise.
- **Historique des cours** : flux public (non officiel) de graphiques de
  [Yahoo Finance](https://finance.yahoo.com), utilisé pour un rechargement ponctuel d'environ
  5 ans de cours quotidiens par métal (contrats à terme GC=F, SI=F, PL=F, PA=F comme proxy du
  cours au comptant). Pas de clé, pas de compte.

Ces trois API "gratuites" (metalpriceapi.com, unirateapi.com, metal-sentinel.com, l'API
Commodity d'API Ninjas…) demandent toutes une inscription pour obtenir une clé, même sur leur
offre gratuite. Le flux Yahoo Finance évite complètement cette étape, au prix d'être non
documenté officiellement : Google/Yahoo peuvent le modifier ou limiter le débit sans préavis, en
particulier depuis une IP partagée (datacenter, VPN). C'est pourquoi le rechargement de
l'historique est **best-effort** :

- Il se lance automatiquement une fois, au premier démarrage de l'app.
- En cas d'échec (ex. limitation temporaire), rien ne s'affiche à l'utilisateur : l'app
  réessaiera au prochain lancement, et un bouton "Recharger l'historique" dans Réglages permet
  de relancer la récupération manuellement à tout moment.
- Que le rechargement réussisse ou non, la saisie manuelle du prix payé ("Prix payé") reste
  toujours disponible pour valoriser un achat.

### Calcul automatique du prix d'achat

- Pour un achat couvert par l'historique (5 dernières années environ, ou toute date après
  l'installation de l'app grâce au cache local qui se construit à chaque rafraîchissement),
  le calcul automatique du prix d'achat (option "Calcul auto") fonctionne directement.
- Pour un achat plus ancien, ou si le rechargement de l'historique a échoué, il faut saisir le
  prix réellement payé (option "Prix payé"). L'écran d'ajout d'un avoir explique ce point et
  bascule automatiquement l'utilisateur vers cette option si besoin.

Pour aller plus loin, `PriceRepository` peut être étendu avec un fournisseur d'historique tiers
avec clé API (ex. metalpriceapi.com) pour une source plus officielle et fiable, sans changer le
reste de l'application.

## Compiler le projet

Ce projet a été généré dans un environnement sans SDK Android installé (le wrapper Gradle est
prêt, mais aucune compilation Android complète n'a pu être exécutée ici). Pour builder :

1. Ouvrir le dossier dans **Android Studio** (Koala/2024.1 ou plus récent recommandé).
2. Laisser Android Studio télécharger le SDK/build-tools nécessaires (compileSdk 35).
3. Synchroniser Gradle (`./gradlew build` ou bouton "Sync" dans l'IDE).
4. Lancer sur un émulateur ou un appareil (API 26+ / Android 8.0+).

Aucune clé API à configurer : l'application fonctionne dès l'installation.

## Prochaines pistes (non implémentées)

- Fournisseur d'historique tiers avec clé API optionnelle (voir ci-dessus).
- Sauvegarde automatique chiffrée (Google Drive) en plus de l'export manuel.
- Widget configurable (choix des métaux affichés, taille).
