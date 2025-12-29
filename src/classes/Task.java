package classes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.List;
import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;

public class Task {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName(value = "estimatedDuration", alternate = { "estimated_duration" })
    private double estimatedDuration;

    @SerializedName(value = "plannedStartDate", alternate = { "planned_start_date" })
    private LocalDate plannedStartDate;

    @SerializedName(value = "plannedEndDate", alternate = { "planned_end_date" })
    private LocalDate plannedEndDate;

    @SerializedName("priority")
    private String priority;

    @SerializedName("status")
    private String status;

    @SerializedName(value = "assigneeId", alternate = { "assignee_id" })
    private int assigneeId;

    @SerializedName(value = "projectId", alternate = { "project_id" })
    private Integer projectId;

    @SerializedName(value = "dependencyIds", alternate = { "dependency_ids" })
    private List<Integer> dependencyIds = new ArrayList<>();

    @SerializedName(value = "requiredSkills", alternate = { "required_skills" })
    private List<Integer> requiredSkillIds = new ArrayList<>();

    public Task() {
        this.id = 0;
        this.name = "";
        this.description = "";
        this.estimatedDuration = 0.0;
        this.plannedStartDate = null;
        this.plannedEndDate = null;
        this.priority = "";
        this.status = "";
        this.assigneeId = 0;
        this.dependencyIds = new ArrayList<>();
    }

    public Task(int id, String name, String description, double estimatedDuration, LocalDate plannedStartDate,
            LocalDate plannedEndDate, String priority, String status) {
        this.id = id;
        this.name = name != null ? name : "";
        this.description = description != null ? description : "";
        this.estimatedDuration = estimatedDuration;
        setPlannedStartDate(plannedStartDate);
        setPlannedEndDate(plannedEndDate);
        this.priority = priority != null ? priority : "";
        this.status = status != null ? status : "";
        this.assigneeId = 0;
        this.dependencyIds = new ArrayList<>();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name != null ? name : "";
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description != null ? description : "";
    }

    public double getEstimatedDuration() {
        return estimatedDuration;
    }

    public void setEstimatedDuration(double estimatedDuration) {
        if (estimatedDuration < 0) {
            throw new IllegalArgumentException("estimatedDuration cannot be negative");
        }
        this.estimatedDuration = estimatedDuration;
    }

    public LocalDate getPlannedStartDate() {
        return plannedStartDate;
    }

    public void setPlannedStartDate(LocalDate plannedStartDate) {
        this.plannedStartDate = plannedStartDate;
        if (this.plannedEndDate != null && this.plannedStartDate != null
                && this.plannedEndDate.isBefore(this.plannedStartDate)) {
            this.plannedEndDate = this.plannedStartDate;
        }
    }

    public LocalDate getPlannedEndDate() {
        return plannedEndDate;
    }

    public void setPlannedEndDate(LocalDate plannedEndDate) {
        if (plannedEndDate != null && this.plannedStartDate != null && plannedEndDate.isBefore(this.plannedStartDate)) {
            throw new IllegalArgumentException("plannedEndDate cannot be before plannedStartDate");
        }
        this.plannedEndDate = plannedEndDate;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority != null ? priority : "";
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "";
    }

    public int getAssigneeId() {
        return assigneeId;
    }

    public void setAssigneeId(int assigneeId) {
        this.assigneeId = assigneeId;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public List<Integer> getDependencyIds() {
        return dependencyIds;
    }

    public void setDependencyIds(List<Integer> dependencyIds) {
        this.dependencyIds = dependencyIds != null ? dependencyIds : new ArrayList<>();
    }

    public List<Integer> getRequiredSkillIds() {
        return requiredSkillIds;
    }

    public void setRequiredSkillIds(List<Integer> requiredSkillIds) {
        this.requiredSkillIds = requiredSkillIds != null ? requiredSkillIds : new ArrayList<>();
    }

    public long getPlannedDurationDays() {
        if (plannedStartDate == null || plannedEndDate == null)
            return -1;
        return ChronoUnit.DAYS.between(plannedStartDate, plannedEndDate);
    }

    @Override
    public String toString() {
        return "Task{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", estimatedDuration=" + estimatedDuration +
                ", plannedStartDate=" + plannedStartDate +
                ", plannedEndDate=" + plannedEndDate +
                ", priority='" + priority + '\'' +
                ", status='" + status + '\'' +
                ", assigneeId=" + assigneeId +
                ", dependencyIds=" + dependencyIds +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Task task = (Task) o;
        return id == task.id && Double.compare(task.estimatedDuration, estimatedDuration) == 0
                && assigneeId == task.assigneeId && Objects.equals(name, task.name)
                && Objects.equals(description, task.description)
                && Objects.equals(plannedStartDate, task.plannedStartDate)
                && Objects.equals(plannedEndDate, task.plannedEndDate) && Objects.equals(priority, task.priority)
                && Objects.equals(status, task.status) && Objects.equals(dependencyIds, task.dependencyIds);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, estimatedDuration, plannedStartDate, plannedEndDate, priority,
                status, assigneeId, dependencyIds);
    }
}