package classes;

import java.time.LocalDate;
import java.util.Objects;
import com.google.gson.annotations.SerializedName;

public class Alert {
    @SerializedName("id")
    private int id;

    @SerializedName("type")
    private String type;

    @SerializedName("message")
    private String message;

    @SerializedName(value = "issuedDate", alternate = { "issued_date" })
    private LocalDate issuedDate;

    @SerializedName(value = "severityLevel", alternate = { "severity_level" })
    private String severityLevel;

    public Alert() {
        this.id = 0;
        this.type = "";
        this.message = "";
        this.issuedDate = null;
        this.severityLevel = "";
    }

    public Alert(int id, String type, String message, LocalDate issuedDate, String severityLevel) {
        this.id = id;
        this.type = type != null ? type : "";
        this.message = message != null ? message : "";
        setIssuedDate(issuedDate);
        this.severityLevel = severityLevel != null ? severityLevel : "";
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type != null ? type : "";
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message != null ? message : "";
    }

    public LocalDate getIssuedDate() {
        return issuedDate;
    }

    public void setIssuedDate(LocalDate issuedDate) {
        if (issuedDate != null && issuedDate.isAfter(LocalDate.now())) {
            throw new IllegalArgumentException("issuedDate cannot be in the future");
        }
        this.issuedDate = issuedDate;
    }

    public String getSeverityLevel() {
        return severityLevel;
    }

    public void setSeverityLevel(String severityLevel) {
        this.severityLevel = severityLevel != null ? severityLevel : "";
    }

    @Override
    public String toString() {
        return "Alert{" +
                "id=" + id +
                ", type='" + type + '\'' +
                ", message='" + message + '\'' +
                ", issuedDate=" + issuedDate +
                ", severityLevel='" + severityLevel + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Alert alert = (Alert) o;
        return id == alert.id && Objects.equals(type, alert.type) && Objects.equals(message, alert.message)
                && Objects.equals(issuedDate, alert.issuedDate) && Objects.equals(severityLevel, alert.severityLevel);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, type, message, issuedDate, severityLevel);
    }
}