package service;

import classes.Alert;
import classes.Member;
import classes.Task;
import classes.Project;
import classes.Team;
import classes.Skill;
import classes.MemberSkill;

import java.util.*;
import java.time.LocalDate;

/**
 * Service d'affectation automatique et intelligente des tâches
 * Algorithme heuristique basé sur :
 * - Compétences requises vs compétences disponibles
 * - Charge de travail actuelle des membres
 * - Disponibilité des membres
 * - Priorités des tâches
 * - Équilibrage de la charge dans l'équipe
 */
public class TaskAssignmentService {

    // Seuil de surcharge (en heures)
    private static final double WEEKLY_HOURS = 40.0; // Standard work week
    private static final double MAX_LOAD_THRESHOLD = 160.0; // 4 semaines * 40h (for current_load)
    private static final double OPTIMAL_LOAD = 120.0; // Charge optimale

    private List<Member> members;
    private List<Task> tasks;
    private AlertService alertService;

    public TaskAssignmentService() {
        this.members = new ArrayList<>();
        this.tasks = new ArrayList<>();
        this.alertService = new AlertService();
    }

    public TaskAssignmentService(List<Member> members, List<Task> tasks) {
        this.members = members;
        this.tasks = tasks;
        this.alertService = new AlertService();
    }

    /**
     * ALGORITHME PRINCIPAL D'AFFECTATION AUTOMATIQUE
     * Stratégie : Trier les tâches par priorité, puis affecter chacune au membre le
     * plus qualifié
     * et le moins chargé
     */
    public AssignmentResult assignTasksAutomatically() {
        AssignmentResult result = new AssignmentResult();

        // ÉTAPE 1: Filtrer les tâches non assignées
        List<Task> unassignedTasks = getUnassignedTasks();

        if (unassignedTasks.isEmpty()) {
            result.addMessage("Aucune tâche à assigner.");
            return result;
        }

        // ÉTAPE 2: Trier les tâches par priorité (Haute → Moyenne → Basse)
        List<Task> sortedTasks = sortTasksByPriority(unassignedTasks);

        // ÉTAPE 3: Pour chaque tâche, trouver le meilleur membre
        for (Task task : sortedTasks) {
            Member bestMember = findBestMemberForTask(task);

            if (bestMember != null) {
                // Vérifier si le membre a assez d'heures restantes
                if (bestMember.getRemainingHours() >= task.getEstimatedDuration()) {
                    // Assigner la tâche
                    assignTaskToMember(task, bestMember);
                    result.addSuccessfulAssignment(task, bestMember);
                    result.addMessage("✓ Tâche '" + task.getName() + "' assignée à " +
                            bestMember.getFirstName() + " " + bestMember.getLastName() +
                            " (heures restantes: " + String.format("%.1f", bestMember.getRemainingHours()) + "h/" + 
                            bestMember.getWeeklyAvailability() + "h)");
                } else {
                    // Pas assez d'heures disponibles
                    result.addFailedAssignment(task,
                            "Heures insuffisantes: tous les membres qualifiés n'ont pas assez d'heures disponibles");
                    result.addMessage("✗ Tâche '" + task.getName() + "' non assignée: heures insuffisantes (requis: " +
                            task.getEstimatedDuration() + "h)");

                    // Générer une alerte
                    Alert alert = alertService.createOverloadAlert(bestMember, task);
                    result.addAlert(alert);
                }
            } else {
                // Aucun membre qualifié trouvé
                result.addFailedAssignment(task, "Aucun membre avec les compétences requises");
                result.addMessage("✗ Tâche '" + task.getName() + "' non assignée: aucun membre qualifié disponible");
            }
        }

        // ÉTAPE 4: Vérifier l'équilibre des charges
        checkLoadBalance(result);

        return result;
    }

    /**
     * Trouve le meilleur membre pour une tâche donnée
     * Critères (par ordre de priorité):
     * 1. Disponibilité (remaining hours > 0)
     * 2. Possession des compétences requises (niveau suffisant)
     * 3. Plus d'heures disponibles restantes
     * 4. Niveau de compétence le plus élevé
     */
    private Member findBestMemberForTask(Task task) {
        List<Member> qualifiedMembers = new ArrayList<>();

        // Filtrer les membres disponibles et qualifiés
        for (Member member : members) {
            // Check if member has enough remaining hours for the task
            if (member.isAvailable() && 
                member.getRemainingHours() >= task.getEstimatedDuration() &&
                hasRequiredSkills(member, task)) {
                qualifiedMembers.add(member);
            }
        }

        if (qualifiedMembers.isEmpty()) {
            return null;
        }

        // Trier par heures restantes décroissantes (prioriser ceux qui ont plus de temps)
        qualifiedMembers.sort((m1, m2) -> Double.compare(m2.getRemainingHours(), m1.getRemainingHours()));

        // Retourner le membre avec le plus d'heures disponibles
        return qualifiedMembers.get(0);
    }

    /**
     * Vérifie si un membre possède les compétences requises pour une tâche
     * Compare les compétences du membre avec celles requises par la tâche
     */
    private boolean hasRequiredSkills(Member member, Task task) {
        // Si la tâche n'a pas de compétences requises, le membre est qualifié
        if (task.getRequiredSkillIds() == null || task.getRequiredSkillIds().isEmpty()) {
            return true;
        }

        // Si le membre n'a pas de compétences, retourner false
        if (member.getMemberSkills() == null || member.getMemberSkills().isEmpty()) {
            return false;
        }

        // Obtenir les IDs des compétences du membre
        List<Integer> memberSkillIds = new ArrayList<>();
        for (MemberSkill ms : member.getMemberSkills()) {
            memberSkillIds.add(ms.getSkill().getId());
        }

        // Vérifier que le membre a TOUTES les compétences requises par la tâche
        for (Integer requiredSkillId : task.getRequiredSkillIds()) {
            if (!memberSkillIds.contains(requiredSkillId)) {
                // Membre n'a pas une compétence requise
                return false;
            }
        }

        // Membre a toutes les compétences requises
        return true;
    }

    /**
     * Assigne une tâche à un membre et met à jour sa charge et disponibilité
     */
    private void assignTaskToMember(Task task, Member member) {
        // Ajouter la tâche aux tâches assignées du membre
        if (!member.getAssignedTasks().contains(task)) {
            member.getAssignedTasks().add(task);
        }

        // Mettre à jour la charge de travail totale
        double newLoad = member.getCurrentLoad() + task.getEstimatedDuration();
        member.setCurrentLoad(newLoad);

        // Calculate remaining hours from weekly availability (no incremental subtraction)
        double remainingHours = member.getWeeklyAvailability() - newLoad;
        member.setRemainingHours(remainingHours);
        member.setAvailable(remainingHours > 0);
    }

    /**
     * Réaffecte une tâche urgente en cours de projet
     * Algorithme: trouve le membre avec la plus petite charge qui a les compétences
     */
    public AssignmentResult reassignUrgentTask(Task urgentTask) {
        AssignmentResult result = new AssignmentResult();

        // Marquer la tâche comme haute priorité
        urgentTask.setPriority("Haute");

        // Trouver le meilleur membre
        Member bestMember = findBestMemberForTask(urgentTask);

        if (bestMember != null) {
            assignTaskToMember(urgentTask, bestMember);
            result.addSuccessfulAssignment(urgentTask, bestMember);
            result.addMessage("✓ Tâche urgente '" + urgentTask.getName() + "' réaffectée à " +
                    bestMember.getFirstName() + " " + bestMember.getLastName());

            // Générer une alerte
            Alert alert = alertService.createUrgentTaskAlert(urgentTask, bestMember);
            result.addAlert(alert);
        } else {
            result.addFailedAssignment(urgentTask, "Aucun membre disponible pour cette tâche urgente");
            result.addMessage("✗ Impossible d'affecter la tâche urgente");
        }

        return result;
    }

    /**
     * Détecte les membres en surcharge
     */
    public List<Alert> detectOverloadedMembers() {
        List<Alert> alerts = new ArrayList<>();

        for (Member member : members) {
            if (member.getCurrentLoad() > MAX_LOAD_THRESHOLD) {
                Alert alert = alertService.createOverloadAlert(member, null);
                alerts.add(alert);
            }
        }

        return alerts;
    }

    /**
     * Vérifie l'équilibre des charges dans l'équipe
     */
    private void checkLoadBalance(AssignmentResult result) {
        if (members.isEmpty())
            return;

        double totalLoad = members.stream()
                .mapToDouble(Member::getCurrentLoad)
                .sum();
        double averageLoad = totalLoad / members.size();

        // Calculer l'écart-type
        double variance = members.stream()
                .mapToDouble(m -> Math.pow(m.getCurrentLoad() - averageLoad, 2))
                .average()
                .orElse(0.0);
        double standardDeviation = Math.sqrt(variance);

        result.setAverageLoad(averageLoad);
        result.setLoadStandardDeviation(standardDeviation);

        // Si l'écart-type est élevé, l'équilibre est mauvais
        if (standardDeviation > 30.0) {
            result.addMessage("⚠ Déséquilibre de charge détecté (écart-type: " +
                    String.format("%.2f", standardDeviation) + "h)");

            Alert alert = alertService.createLoadImbalanceAlert(averageLoad, standardDeviation);
            result.addAlert(alert);
        } else {
            result.addMessage("✓ Charge bien équilibrée entre les membres (écart-type: " +
                    String.format("%.2f", standardDeviation) + "h)");
        }
    }

    /**
     * Récupère les tâches non assignées
     */
    private List<Task> getUnassignedTasks() {
        List<Task> unassigned = new ArrayList<>();

        for (Task task : tasks) {
            boolean isAssigned = false;

            for (Member member : members) {
                if (member.getAssignedTasks().contains(task)) {
                    isAssigned = true;
                    break;
                }
            }

            if (!isAssigned) {
                unassigned.add(task);
            }
        }

        return unassigned;
    }

    /**
     * Trie les tâches par priorité (Haute → Moyenne → Basse)
     */
    private List<Task> sortTasksByPriority(List<Task> tasks) {
        List<Task> sorted = new ArrayList<>(tasks);

        sorted.sort((t1, t2) -> {
            int priority1 = getPriorityValue(t1.getPriority());
            int priority2 = getPriorityValue(t2.getPriority());
            return Integer.compare(priority2, priority1); // Ordre décroissant
        });

        return sorted;
    }

    /**
     * Convertit une priorité en valeur numérique
     */
    private int getPriorityValue(String priority) {
        if (priority == null)
            return 0;

        switch (priority.toLowerCase()) {
            case "haute":
            case "high":
                return 3;
            case "moyenne":
            case "medium":
                return 2;
            case "basse":
            case "low":
                return 1;
            default:
                return 0;
        }
    }

    /**
     * Génère un rapport de répartition
     */
    public String generateAssignmentReport() {
        StringBuilder report = new StringBuilder();
        report.append("=== RAPPORT DE RÉPARTITION DES TÂCHES ===\n\n");

        for (Member member : members) {
            report.append(member.getFirstName()).append(" ").append(member.getLastName()).append(":\n");
            report.append("  - Charge actuelle: ").append(member.getCurrentLoad()).append("h\n");
            report.append("  - Disponibilité hebdomadaire: ").append(member.getWeeklyAvailability()).append("h\n");
            report.append("  - Heures restantes: ").append(String.format("%.1f", member.getRemainingHours())).append("h\n");
            report.append("  - Disponible: ").append(member.isAvailable() ? "Oui" : "Non").append("\n");
            report.append("  - Tâches assignées: ").append(member.getAssignedTasks().size()).append("\n");

            if (!member.getAssignedTasks().isEmpty()) {
                for (Task task : member.getAssignedTasks()) {
                    report.append("    • ").append(task.getName())
                            .append(" (").append(task.getEstimatedDuration()).append("h)\n");
                }
            }
            report.append("\n");
        }

        return report.toString();
    }

    // Getters et setters
    public List<Member> getMembers() {
        return members;
    }

    public void setMembers(List<Member> members) {
        this.members = members;
    }

    public List<Task> getTasks() {
        return tasks;
    }

    public void setTasks(List<Task> tasks) {
        this.tasks = tasks;
    }
}
