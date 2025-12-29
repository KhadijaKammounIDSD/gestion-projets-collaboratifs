# Rapport Individuel

> **Nom** : [À compléter]  
> **Équipe** : [À compléter]  
> **Date** : 28 décembre 2024

---

## 1. Tâches Réalisées

### Tâches Individuelles
| Tâche | Description | Temps |
|-------|-------------|-------|
| Conception du schéma de base de données | Création des tables `task`, `member`, `skill`, `task_dependency` | 4h |
| Développement des classes DAO | Implémentation de `TaskDAO.java` et `MemberDAO.java` | 6h |
| Algorithme d'affectation | Développement de `TaskAssignmentService.java` | 8h |

### Tâches en Collaboration
| Tâche | Membre(s) concerné(s) | Ma contribution | Temps |
|-------|----------------------|-----------------|-------|
| API REST (Servlets) | Étudiant 2 | Développement de `TaskServlet` et `TaskAssignmentServlet` | 4h |
| Tests et validation | Étudiant 2, Étudiant 3 | Écriture de `TestTaskAssignment.java`, correction des bugs | 3h |
| Documentation | Toute l'équipe | Rédaction du README et commentaires code | 2h |

**Total personnel** : ~27h

---

## 2. Temps Consacré

| Activité | Temps estimé |
|----------|--------------|
| Analyse et conception | 4h |
| Développement backend | 14h |
| Tests et débogage | 5h |
| Documentation | 2h |
| Réunions d'équipe | 2h |
| **Total** | **27h** |

---

## 3. Outils et Technologies Utilisés

| Outil/Technologie | Utilisation | Justification |
|-------------------|-------------|---------------|
| **Java 8** | Langage principal | Imposé par le projet, bon support JDBC |
| **Maven** | Gestion de build | Automatisation des dépendances et du packaging |
| **MySQL** | Base de données | Robuste, gratuit, bonne intégration JDBC |
| **VS Code** | IDE | Léger, extensions Java disponibles |
| **Git** | Versioning | Collaboration et historique des modifications |
| **Gson** | JSON | Sérialisation simple pour l'API REST |

---

## 4. Difficultés Rencontrées et Solutions

### Difficulté 1 : Gestion des packages Java
**Problème** : Les classes du package `classes` n'étaient pas correctement importées dans les DAO et Servlets après restructuration.  
**Solution** : Ajout systématique de `package classes;` dans chaque fichier modèle et mise à jour de tous les imports (`import classes.Task;`, etc.).

### Difficulté 2 : Équilibrage de charge
**Problème** : L'algorithme initial assignait toutes les tâches au premier membre qualifié, créant des surcharges.  
**Solution** : Modification de la logique pour sélectionner le membre avec la charge actuelle la plus faible parmi les candidats qualifiés.

### Difficulté 3 : Compatibilité Java 8
**Problème** : Utilisation de `String.repeat()` qui n'existe qu'en Java 11+.  
**Solution** : Création d'une méthode helper compatible Java 8.

---

## 5. Évaluation du Travail en Équipe

### Communication
- **Points positifs** : Réunions régulières (2x/semaine), utilisation de Discord pour les échanges rapides.
- **Améliorations possibles** : Meilleure documentation des décisions techniques en temps réel.

### Répartition du Travail
- La répartition a été globalement équilibrée (~29h par membre en moyenne).
- Certaines tâches ont nécessité des ajustements en cours de projet (ex: plus de temps sur l'algorithme que prévu).

### Difficultés et Solutions
| Difficulté d'équipe | Solution adoptée |
|---------------------|------------------|
| Conflits de merge Git | Revues de code avant chaque merge |
| Dépendances entre tâches | Planning partagé avec jalons clairs |
| Disponibilités différentes | Flexibilité et communication accrue |

### Auto-évaluation
Ma contribution a été satisfaisante, notamment sur la partie algorithmique. J'aurais pu être plus proactif sur la documentation technique pendant le développement.

---

*Ce rapport contient 2 pages.*
