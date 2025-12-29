package classes;

import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import com.google.gson.annotations.SerializedName;

public class Project {
    @SerializedName("id")
    private int id;

    @SerializedName("name")
    private String name;

    @SerializedName("description")
    private String description;

    @SerializedName(value = "startDate", alternate = { "start_date" })
    private LocalDate startDate;

    @SerializedName(value = "endDate", alternate = { "end_date" })
    private LocalDate endDate;

    @SerializedName("status")
    private String status;

    public Project() {
        this.id = 0;
        this.name = "";
        this.description = "";
        this.startDate = null;
        this.endDate = null;
        this.status = "";
    }

    public Project(int id, String name, String description, LocalDate startDate, LocalDate endDate, String status) {
        this.id = id;
        this.name = name;
        this.description = description;
        setStartDate(startDate);
        setEndDate(endDate);
        this.status = status;
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

    public LocalDate getStartDate() {
        return startDate;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
        if (this.endDate != null && this.startDate != null && this.endDate.isBefore(this.startDate)) {
            this.endDate = this.startDate;
        }
    }

    public LocalDate getEndDate() {
        return endDate;
    }

    public void setEndDate(LocalDate endDate) {
        if (endDate != null && this.startDate != null && endDate.isBefore(this.startDate)) {
            throw new IllegalArgumentException("endDate cannot be before startDate");
        }
        this.endDate = endDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status != null ? status : "";
    }

    public long getDurationDays() {
        if (startDate == null || endDate == null)
            return -1;
        return ChronoUnit.DAYS.between(startDate, endDate);
    }

    @Override
    public String toString() {
        return "Project{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", startDate=" + startDate +
                ", endDate=" + endDate +
                ", status='" + status + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Project project = (Project) o;
        return id == project.id && Objects.equals(name, project.name)
                && Objects.equals(description, project.description) && Objects.equals(startDate, project.startDate)
                && Objects.equals(endDate, project.endDate) && Objects.equals(status, project.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, description, startDate, endDate, status);
    }
}
