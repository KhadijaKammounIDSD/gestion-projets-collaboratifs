package classes;

import java.util.ArrayList;
import com.google.gson.annotations.SerializedName;

public class Member {
	@SerializedName("id")
	private int id;

	@SerializedName(value = "firstName", alternate = { "first_name" })
	private String firstName;

	@SerializedName(value = "lastName", alternate = { "last_name" })
	private String lastName;

	@SerializedName("email")
	private String email;

	@SerializedName("password")
	private String password;

	@SerializedName("role")
	private String role;

	@SerializedName(value = "currentLoad", alternate = { "current_load" })
	private double currentLoad = 0.0;

	@SerializedName("available")
	private boolean available = true;

	// Weekly availability in hours (default 40h per week)
	@SerializedName(value = "weeklyAvailability", alternate = { "weekly_availability" })
	private double weeklyAvailability = 40.0;

	// Remaining hours available this week
	@SerializedName(value = "remainingHours", alternate = { "remaining_hours" })
	private double remainingHours = 40.0;

	@SerializedName(value = "memberSkills", alternate = { "member_skills" })
	private ArrayList<MemberSkill> memberSkills = new ArrayList<>();

	@SerializedName(value = "assignedTasks", alternate = { "assigned_tasks" })
	private ArrayList<Task> assignedTasks = new ArrayList<>();

	@SerializedName("team")
	private Team team;
	
	// Transient field for JSON deserialization when only teamId is sent
	@SerializedName(value = "teamId", alternate = { "team_id" })
	private Integer teamId;

	public Member() {
	}

	public Member(int id, String firstName, String lastName, String email, String password, String role, double currentLoad,
			boolean available, ArrayList<MemberSkill> memberSkills, ArrayList<Task> assignedTasks, Team team) {
		this.id = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.email = email;
		this.password = password;
		this.role = role;
		this.currentLoad = currentLoad;
		this.available = available;
		this.memberSkills = memberSkills != null ? memberSkills : new ArrayList<>();
		this.assignedTasks = assignedTasks != null ? assignedTasks : new ArrayList<>();
		this.team = team;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getRole() {
		return role;
	}

	public void setRole(String role) {
		this.role = role;
	}

	public double getCurrentLoad() {
		return currentLoad;
	}

	public void setCurrentLoad(double currentLoad) {
		this.currentLoad = currentLoad;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public ArrayList<MemberSkill> getMemberSkills() {
		return memberSkills;
	}

	public void setMemberSkills(ArrayList<MemberSkill> memberSkills) {
		this.memberSkills = memberSkills != null ? memberSkills : new ArrayList<>();
	}

	public ArrayList<Task> getAssignedTasks() {
		return assignedTasks;
	}

	public void setAssignedTasks(ArrayList<Task> assignedTasks) {
		this.assignedTasks = assignedTasks != null ? assignedTasks : new ArrayList<>();
	}

	public Team getTeam() {
		return team;
	}

	public void setTeam(Team team) {
		this.team = team;
	}
	
	public Integer getTeamId() {
		if (teamId != null) {
			return teamId;
		}
		return team != null ? team.getId() : null;
	}
	
	public void setTeamId(Integer teamId) {
		this.teamId = teamId;
		if (teamId != null && this.team == null) {
			this.team = new Team();
			this.team.setId(teamId);
		}
	}

	public double getWeeklyAvailability() {
		return weeklyAvailability;
	}

	public void setWeeklyAvailability(double weeklyAvailability) {
		this.weeklyAvailability = weeklyAvailability;
		this.remainingHours = weeklyAvailability;
	}

	public double getRemainingHours() {
		return remainingHours;
	}

	public void setRemainingHours(double remainingHours) {
		this.remainingHours = remainingHours;
	}

	/**
	 * Reduces remaining hours when a task is assigned
	 */
	public void allocateHours(double hours) {
		this.remainingHours -= hours;
		if (this.remainingHours < 0) {
			this.remainingHours = 0;
		}
		if (this.remainingHours <= 0) {
			this.available = false;
		}
	}

	/**
	 * Resets weekly availability (call this weekly)
	 */
	public void resetWeeklyAvailability() {
		this.remainingHours = this.weeklyAvailability;
		if (this.remainingHours > 0) {
			this.available = true;
		}
	}

	@Override
	public String toString() {
		return "Member [id=" + id + ", firstName=" + firstName + ", lastName=" + lastName + ", email=" + email
				+ ", role=" + role + ", currentLoad=" + currentLoad + ", available=" + available + "]";
	}

}
