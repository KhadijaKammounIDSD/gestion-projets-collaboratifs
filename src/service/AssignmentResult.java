package service;

import classes.Alert;
import classes.Member;
import classes.Task;
import classes.Project;
import classes.Team;
import classes.Skill;
import classes.MemberSkill;

import java.util.*;

/**
 * Classe pour stocker et structurer les résultats d'une affectation de tâches
 */
public class AssignmentResult {

    private List<String> messages;
    private Map<Task, Member> successfulAssignments;
    private Map<Task, String> failedAssignments;
    private List<Alert> alerts;
    private double averageLoad;
    private double loadStandardDeviation;

    public AssignmentResult() {
        this.messages = new ArrayList<>();
        this.successfulAssignments = new HashMap<>();
        this.failedAssignments = new HashMap<>();
        this.alerts = new ArrayList<>();
        this.averageLoad = 0.0;
        this.loadStandardDeviation = 0.0;
    }

    public void addMessage(String message) {
        this.messages.add(message);
    }

    public void addSuccessfulAssignment(Task task, Member member) {
        this.successfulAssignments.put(task, member);
    }

    public void addFailedAssignment(Task task, String reason) {
        this.failedAssignments.put(task, reason);
    }

    public void addAlert(Alert alert) {
        this.alerts.add(alert);
    }

    public List<String> getMessages() {
        return messages;
    }

    public Map<Task, Member> getSuccessfulAssignments() {
        return successfulAssignments;
    }

    public Map<Task, String> getFailedAssignments() {
        return failedAssignments;
    }

    public List<Alert> getAlerts() {
        return alerts;
    }

    public double getAverageLoad() {
        return averageLoad;
    }

    public void setAverageLoad(double averageLoad) {
        this.averageLoad = averageLoad;
    }

    public double getLoadStandardDeviation() {
        return loadStandardDeviation;
    }

    public void setLoadStandardDeviation(double loadStandardDeviation) {
        this.loadStandardDeviation = loadStandardDeviation;
    }

    public int getSuccessCount() {
        return successfulAssignments.size();
    }

    public int getFailureCount() {
        return failedAssignments.size();
    }

    public boolean hasAlerts() {
        return !alerts.isEmpty();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("=== RÉSULTAT DE L'AFFECTATION ===\n\n");

        sb.append("Tâches assignées avec succès: ").append(getSuccessCount()).append("\n");
        sb.append("Tâches non assignées: ").append(getFailureCount()).append("\n");
        sb.append("Alertes générées: ").append(alerts.size()).append("\n\n");

        if (!messages.isEmpty()) {
            sb.append("Messages:\n");
            for (String msg : messages) {
                sb.append("  ").append(msg).append("\n");
            }
            sb.append("\n");
        }

        if (!alerts.isEmpty()) {
            sb.append("Alertes:\n");
            for (Alert alert : alerts) {
                sb.append("  [").append(alert.getSeverityLevel()).append("] ")
                        .append(alert.getType()).append(": ")
                        .append(alert.getMessage()).append("\n");
            }
        }

        return sb.toString();
    }
}
