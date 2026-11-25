# PharmaGestion

Application desktop Java de gestion de pharmacie, avec Swing, FlatLaf, JDBC et Supabase PostgreSQL.

## Fonctionnalités

- Authentification BCrypt avec rôles administrateur et pharmacien
- Tableau de bord, alertes de stock et d'expiration
- Gestion des médicaments, catégories, clients, fournisseurs et utilisateurs
- Vente avec panier, remise, reçu et décrément FEFO des lots
- Approvisionnements, rapports et journal d'audit
- Sauvegarde/restauration SQL avec `pg_dump` et `psql`

## Prérequis

- Java 17 ou supérieur
- Maven 3.8 ou supérieur
- Un projet Supabase avec PostgreSQL actif
- Les outils PostgreSQL (`pg_dump` et `psql`) pour le module de sauvegarde

## Installation Supabase

1. Créez un projet dans Supabase et ouvrez **SQL Editor**.
2. Exécutez le contenu de `src/main/resources/sql/supabase_schema.sql`.
   Cela crée les tables, les catégories et un compte de démonstration `admin` / `admin123`.
3. Facultatif : exécutez `src/main/resources/sql/supabase_seed_100_medicaments.sql` pour charger les médicaments de démonstration.
4. Dans Supabase, ouvrez **Connect** et choisissez **Session pooler** pour un ordinateur/réseau IPv4. Prenez l'hôte, le port, la base et l'utilisateur fournis par le dashboard; activez SSL. Si votre réseau prend en charge IPv6, l'endpoint direct convient aussi. Évitez le **Transaction pooler** avec cette version JDBC.
5. Les champs hôte, port, base et utilisateur de `.env` sont déjà renseignés d'après le dashboard. Ajoutez uniquement le mot de passe de la base dans `PHARMACIE_DB_PASSWORD`. L'application lit ce fichier au démarrage; aucun `export` n'est requis.

Après avoir renseigné les trois valeurs, le schéma peut être créé depuis un terminal à la racine du projet avec `python3 scripts/setup_supabase.py` (outil `psql` requis). Le mot de passe reste dans `.env` et n'est pas affiché par le script.

```bash
PHARMACIE_DB_URL=jdbc:postgresql://HOST:PORT/postgres?sslmode=require
PHARMACIE_DB_USER=postgres.PROJECT_REF
PHARMACIE_DB_PASSWORD=VOTRE_MOT_DE_PASSE
```

Avec le Session pooler, utilisez son hôte, port et nom d'utilisateur exacts (souvent `postgres.<project-ref>`) affichés par Supabase. Les valeurs `HOST`, `PORT` et `PROJECT_REF` ci-dessus ne sont que des exemples. Le fichier `.env` est ignoré par Git pour ne pas publier le mot de passe.

La configuration accepte aussi les propriétés JVM `pharmacie.db.url`, `pharmacie.db.user` et `pharmacie.db.password`. Les variables d'environnement sont plus simples pour le développement local.

## Compiler et démarrer

Depuis le dossier du projet :

```bash
mvn clean package
java -jar target/pharmacie-gestion-1.0.0.jar
```

Ou depuis l'IDE, exécutez `src/main/java/com/pharmacie/Main.java` après avoir configuré les variables d'environnement dans le profil d'exécution.

## Identifiants de démonstration

| Identifiant | Mot de passe | Rôle |
|---|---|---|
| `admin` | `admin123` | ADMIN |

Changez immédiatement ce mot de passe après la première connexion. Les identifiants de la base ne doivent jamais être commités dans Git.

## Sécurité et déploiement

Cette version se connecte directement de l'application desktop au PostgreSQL Supabase en utilisant les identifiants de base configurés sur le poste. Ne distribuez pas un JAR contenant ces identifiants et ne partagez pas un compte administrateur de base. Pour déployer auprès de plusieurs utilisateurs, placez une API backend entre l'application et Supabase, puis appliquez des politiques d'accès adaptées.

## Migration depuis MySQL

Le code actuel utilise PostgreSQL. Les anciens scripts `create_database.sql`, `migration_v2_lots_audit.sql` et `seed_100_medicaments.sql` sont des scripts MySQL et ne doivent pas être exécutés dans Supabase. Le script Supabase crée un schéma neuf et le fichier de seed ne charge que les données de démonstration. Les données d'une ancienne base MySQL nécessitent une exportation/importation séparée; ne restaurez pas un dump MySQL directement avec `psql`.

