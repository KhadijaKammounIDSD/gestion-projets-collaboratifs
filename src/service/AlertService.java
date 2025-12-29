package service;

import classes.Alert;
import classes.Member;
import classes.Task;
import classes.Project;
import classes.Team;
import classes.Skill;
import classes.MemberSkill;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Service de gestion et génération d'alertes intelligentes
 * Détecte : surcharges, conflits, retards potentiels, déséquilibres
 */
public class AlertService {

    private static int alertIdCounter = 1;
    private List<Alert> alerts;

    public AlertService() {
        this.alerts = new ArrayList<>();
    }

    /**
     * Crée une alerte de surcharge pour un membre
     */
    public Alert createOverloadAlert(Member member, Task task) {
        String message;
        if (task != null) {
            message = "Surcharge détectée: " + member.getFirstName() + " " + member.getLastName() +
                    " a une charge de " + member.getCurrentLoad() + "h. " +
                    "Impossible d'ajouter la tâche '" + task.getName() + "'.";
        } else {
            message = "Surcharge détectée: " + member.getFirstName() + " " + member.getLastName() +
                    " a une charge excessive de " + member.getCurrentLoad() + "h.";
        }

        Alert alert = new Alert(
                alertIdCounter++,
                "Surcharge",
                message,
                LocalDate.now(),
                "Haute");

        alerts.add(alert);
        return alert;
    }

    /**
     * Crée une alerte pour une tâche urgente ajoutée
     */
    public Alert createUrgentTaskAlert(Task task, Member assignee) {
        String message = "Tâche urgente '" + task.getName() + "' ajoutée et affectée à " +
                assignee.getFirstName() + " " + assignee.getLastName() + ". " +
                "Nouvelle charge: " + assignee.getCurrentLoad() + "h.";

        Alert alert = new Alert(
                alertIdCounter++,
                "Tâche urgente",
                message,
                LocalDate.now(),
                "Haute");

        alerts.add(alert);
        return alert;
    }

    /**
     * Crée une alerte de déséquilibre de charge
     */
    public Alert createLoadImbalanceAlert(double averageLoad, double standardDeviation) {
        String message = "Déséquilibre de charge dans l'équipe. " +
                "Moyenne: " + String.format("%.2f", averageLoad) + "h, " +
                "Écart-type: " + String.format("%.2f", standardDeviation) + "h. " +
                "Recommandation: rééquilibrer les tâches.";

        Alert alert = new Alert(
                alertIdCounter++,
                "Déséquilibre",
                message,
                LocalDate.now(),
                "Moyenne");

        alerts.add(alert);
        return alert;
    }

    /**
     * Crée une alerte de retard potentiel
     */
    public Alert createDelayAlert(Task task, Member assignee) {
        String message = "Retard potentiel détecté pour la tâche '" + task.getName() + "' " +
                "assignée à " + assignee.getFirstName() + " " + assignee.getLastName() + ". " +
                "Date limite: " + task.getPlannedEndDate();

        Alert alert = new Alert(
                alertIdCounter++,
                "Retard potentiel",
                message,
                LocalDate.now(),
                "Haute");

        alerts.add(alert);
        return alert;
    }

    /**
     * Crée une alerte de conflit de compétences
     */
    public Alert createSkillConflictAlert(Task task) {
        String message = "Aucun membre disponible avec les compétences requises pour la tâche '" +
                task.getName() + "'. Priorité: " + task.getPriority();

        Alert alert = new Alert(
                alertIdCounter++,
                "Conflit de compétences",
                message,
                LocalDate.now(),
                "Haute");

        alerts.add(alert);
        return alert;
    }

    /**
     * Vérifie les retards potentiels sur les tâches
     */
    public List<Alert> checkPotentialDelays(List<Member> members) {
        List<Alert> delayAlerts = new ArrayList<>();
        LocalDate today = LocalDate.now();

        for (Member member : members) {
            for (Task task : member.getAssignedTasks()) {
                // Si la tâche a une date de fin et qu'elle approche
                if (task.getPlannedEndDate() != null) {
                    long daysUntilDeadline = java.time.temporal.ChronoUnit.DAYS.between(today,
                            task.getPlannedEndDate());

                    // Alerte si deadline dans moins de 7 jours et tâche pas terminée
                    if (daysUntilDeadline <= 7 && daysUntilDeadline >= 0 &&
                            !"Terminée".equalsIgnoreCase(task.getStatus())) {
                        Alert alert = createDelayAlert(task, member);
                        delayAlerts.add(alert);
                    }
                }
            }
        }

        return delayAlerts;
    }

    /**
     * Récupère toutes les alertes
     */
    public List<Alert> getAllAlerts() {
        return new ArrayList<>(alerts);
    }

    /**
     * Récupère les alertes par niveau de sévérité
     */
    public List<Alert> getAlertsBySeverity(String severity) {
        List<Alert> filtered = new ArrayList<>();

        for (Alert alert : alerts) {
            if (alert.getSeverityLevel().equalsIgnoreCase(severity)) {
                filtered.add(alert);
            }
        }

        return filtered;
    }

    /**
     * Efface toutes les alertes
     */
    public void clearAlerts() {
        alerts.clear();
    }

    /**
     * Compte le nombre d'alertes par type
     */
    public String getAlertSummary() {
        int high = 0, medium = 0, low = 0;

        for (Alert alert : alerts) {
            switch (alert.getSeverityLevel().toLowerCase()) {
                case "haute":
                case "high":
                    high++;
                    break;
                case "moyenne":
                case "medium":
                    medium++;
                    break;
                case "basse":
                case "low":
                    low++;
                    break;
            }
        }

        return "Alertes: " + high + " hautes, " + medium + " moyennes, " + low + " basses";
    }
}
