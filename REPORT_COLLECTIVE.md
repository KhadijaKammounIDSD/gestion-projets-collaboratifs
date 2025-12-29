# Rapport Global - Plateforme de Gestion de Projets Collaboratifs

## 1. Répartition des Tâches

| Tâche | Étudiant 1 | Étudiant 2 | Étudiant 3 | Charge Totale (h) |
|-------|------------|------------|------------|-------------------|
| Conception BDD (schéma, tables) | 4h | 2h | — | 6h |
| Backend Java (classes modèles) | 6h | — | 4h | 10h |
| DAOs (accès données) | 2h | 8h | 2h | 12h |
| Algorithme d'affectation | — | 6h | 8h | 14h |
| API REST (Servlets) | 4h | 4h | 4h | 12h |
| Frontend (HTML/CSS/JS) | 8h | 2h | — | 10h |
| Intégration FullCalendar | — | 4h | 4h | 8h |
| Tests et débogage | 3h | 3h | 4h | 10h |
| Documentation | 2h | 2h | 2h | 6h |
| **Total par membre** | **29h** | **31h** | **28h** | **88h** |

> *À compléter avec les noms réels des étudiants et les heures effectives.*

---

## 2. Difficultés Rencontrées et Solutions

| Difficulté | Solution Apportée |
|------------|-------------------|
| Gestion des packages Java (`package classes;` vs imports) | Ajout systématique des déclarations de package et mise à jour des imports dans tous les fichiers. |
| Compatibilité Java 8 (`String.repeat()` non disponible) | Création d'une méthode helper `repeat(String, int)` compatible Java 8. |
| Build manuel sans gestionnaire de dépendances | Migration vers Maven avec `pom.xml` configuré pour toutes les dépendances. |
| Équilibrage de la charge de travail dans l'algorithme | Implémentation d'un algorithme heuristique qui sélectionne le membre avec la charge la plus faible parmi les candidats qualifiés. |
| Gestion des tâches urgentes en cours de projet | Ajout d'une fonctionnalité de réaffectation urgente qui peut redistribuer des tâches. |

---

## 3. Outils et Choix Technologiques

### Technologies Utilisées
| Catégorie | Technologie | Justification |
|-----------|-------------|---------------|
| **Langage** | Java 8+ | Imposé par le projet, maturité et documentation abondante. |
| **Build** | Maven | Gestion automatique des dépendances, cycle de vie standard. |
| **Serveur Web** | Jetty (via Maven plugin) | Léger, facile à intégrer pour le développement. |
| **Base de données** | MySQL | Robuste, open-source, bien documentée. |
| **Frontend** | HTML5 / CSS3 / JavaScript | Technologies web standards, pas de framework lourd. |
| **Timeline** | FullCalendar | Bibliothèque spécialisée pour la visualisation de planning. |
| **JSON** | Gson (Google) | Sérialisation/désérialisation simple et efficace. |

### Étapes de Réalisation
1. **Analyse** : Étude du cahier des charges et modélisation de la BDD.
2. **Conception** : Définition des classes Java et de l'architecture MVC.
3. **Développement** : Implémentation progressive (BDD → DAO → Services → Servlets → Frontend).
4. **Tests** : Vérification unitaire via `TestTaskAssignment` et tests manuels API.
5. **Intégration** : Liaison frontend/backend et corrections.

---

## 4. Bilan du Projet

### Niveau d'Atteinte des Objectifs

| Fonctionnalité | Objectif | Réalisé | % |
|----------------|----------|---------|---|
| Gestion des membres et équipes | CRUD complet | ✅ | 100% |
| Gestion des tâches | CRUD + assignation | ✅ | 100% |
| Gestion des compétences | CRUD + association membres | ✅ | 100% |
| Algorithme d'affectation automatique | Heuristique avec équilibrage | ✅ | 100% |
| Détection de surcharge | Alertes automatiques | ✅ | 100% |
| Visualisation timeline | FullCalendar interactif | ✅ | 95% |
| API REST | Endpoints fonctionnels | ✅ | 100% |
| **Score global** | | | **~98%** |

### Compétences Acquises
- **Techniques** : Java EE, JDBC, API REST, Maven, architecture MVC.
- **Algorithmique** : Conception d'heuristiques d'affectation, équilibrage de charge.
- **Outils** : Gestion de projet avec Maven, débogage, tests unitaires.
- **Collaboration** : Travail en équipe, versioning, communication.

---

*Rapport rédigé le 28 décembre 2024.*
