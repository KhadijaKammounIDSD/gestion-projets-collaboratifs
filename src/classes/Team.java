package classes;

import java.util.ArrayList;
import java.util.Objects;

import com.google.gson.annotations.SerializedName;

public class Team {
    private int id;
    private String name;
    private ArrayList<Member> members;

    public Team() {
        this.id = 0;
        this.name = "";
        this.members = new ArrayList<>();
    }

    public Team(int id, String name) {
        this.id = id;
        this.name = name;
        this.members = new ArrayList<>();
    }

    public Team(int id, String name, ArrayList<Member> members) {
        this.id = id;
        this.name = name;
        this.members = members != null ? members : new ArrayList<>();
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
        this.name = name;
    }

    public ArrayList<Member> getMembers() {
        return members;
    }

    public void setMembers(ArrayList<Member> members) {
        this.members = members != null ? members : new ArrayList<>();
    }

    public void addMember(Member member) {
        if (member != null && !this.members.contains(member)) {
            this.members.add(member);
            member.setTeam(this);
        }
    }

    public void removeMember(Member member) {
        if (this.members.remove(member)) {
            member.setTeam(null);
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        Team team = (Team) o;
        return id == team.id && Objects.equals(name, team.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name);
    }

    @Override
    public String toString() {
        return "Team [id=" + id + ", name='" + name + "', members=" + members.size() + "]";
    }
}
