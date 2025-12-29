package classes;

import java.util.Objects;

/**
 * Association class between Member and Skill.
 * Each member has their own proficiency level for each skill.
 */
import com.google.gson.annotations.SerializedName;

public class MemberSkill {
    private int id;
    private int memberId;
    private int skillId;
    private int level; // proficiency level for this member on this skill
    
    // Transient skill object for enriched responses
    @SerializedName("skill")
    private Skill skill;

    public MemberSkill() {
    }

    public MemberSkill(int id, int memberId, int skillId, int level) {
        this.id = id;
        this.memberId = memberId;
        this.skillId = skillId;
        this.level = level;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getMemberId() {
        return memberId;
    }

    public void setMemberId(int memberId) {
        this.memberId = memberId;
    }

    public int getSkillId() {
        return skillId;
    }

    public void setSkillId(int skillId) {
        this.skillId = skillId;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        MemberSkill that = (MemberSkill) o;
        return id == that.id && memberId == that.memberId && skillId == that.skillId && level == that.level;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, memberId, skillId, level);
    }

    @Override
    public String toString() {
        return "MemberSkill [id=" + id + ", memberId=" + memberId + ", skillId=" + skillId + ", level=" + level + "]";
    }
}
