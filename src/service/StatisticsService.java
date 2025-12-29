package service;

import classes.*;
import dao.*;

import java.sql.Connection;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Service for generating project statistics and reports.
 * Supports Scenario 7: Statistics and comprehensive reporting
 */
public class StatisticsService {

    private Connection connection;

    public StatisticsService(Connection connection) {
        this.connection = connection;
    }

    /**
     * Generate comprehensive project statistics
     */
    public Map<String, Object> getProjectStatistics() {
        Map<String, Object> stats = new LinkedHashMap<>();
        
        ProjectDAO projectDAO = new ProjectDAO(connection);
        TaskDAO taskDAO = new TaskDAO(connection);
        MemberDAO memberDAO = new MemberDAO(connection);
        
        List<Project> projects = projectDAO.getAllProjects();
        List<Task> tasks = taskDAO.getAllTasks();
        List<Member> members = memberDAO.getAllMembers();
        
        // Project overview
        stats.put("totalProjects", projects.size());
        stats.put("activeProjects", projects.stream()
            .filter(p -> "En cours".equals(p.getStatus()) || "In Progress".equals(p.getStatus()))
            .count());
        stats.put("completedProjects", projects.stream()
            .filter(p -> "Terminé".equals(p.getStatus()) || "Completed".equals(p.getStatus()))
            .count());
        
        // Task overview
        stats.put("totalTasks", tasks.size());
        stats.put("assignedTasks", tasks.stream().filter(t -> t.getAssigneeId() > 0).count());
        stats.put("unassignedTasks", tasks.stream().filter(t -> t.getAssigneeId() == 0).count());
        stats.put("completedTasks", tasks.stream()
            .filter(t -> "Completed".equals(t.getStatus()) || "Terminé".equals(t.getStatus()))
            .count());
        stats.put("inProgressTasks", tasks.stream()
            .filter(t -> "In Progress".equals(t.getStatus()) || "En cours".equals(t.getStatus()))
            .count());
        
        // Member overview
        stats.put("totalMembers", members.size());
        stats.put("availableMembers", members.stream().filter(Member::isAvailable).count());
        
        // Calculate task completion rate
        double completionRate = tasks.isEmpty() ? 0 : 
            (double) tasks.stream().filter(t -> "Completed".equals(t.getStatus()) || "Terminé".equals(t.getStatus())).count() 
            / tasks.size() * 100;
        stats.put("taskCompletionRate", Math.round(completionRate * 100.0) / 100.0);
        
        return stats;
    }

    /**
     * Generate workload distribution statistics
     */
    public Map<String, Object> getWorkloadDistribution() {
        Map<String, Object> workload = new LinkedHashMap<>();
        
        MemberDAO memberDAO = new MemberDAO(connection);
        List<Member> members = memberDAO.getAllMembers();
        
        if (members.isEmpty()) {
            workload.put("members", Collections.emptyList());
            workload.put("averageLoad", 0.0);
            workload.put("maxLoad", 0.0);
            workload.put("minLoad", 0.0);
            workload.put("loadStandardDeviation", 0.0);
            return workload;
        }
        
        // Per-member workload details
        List<Map<String, Object>> memberWorkloads = new ArrayList<>();
        for (Member m : members) {
            Map<String, Object> mw = new LinkedHashMap<>();
            mw.put("id", m.getId());
            mw.put("name", m.getFirstName() + " " + m.getLastName());
            mw.put("currentLoad", m.getCurrentLoad());
            mw.put("weeklyAvailability", m.getWeeklyAvailability());
            mw.put("remainingHours", m.getRemainingHours());
            mw.put("utilizationRate", m.getWeeklyAvailability() > 0 ? 
                Math.round(((m.getWeeklyAvailability() - m.getRemainingHours()) / m.getWeeklyAvailability() * 100) * 100.0) / 100.0 : 0);
            mw.put("available", m.isAvailable());
            mw.put("overloaded", m.getRemainingHours() < 0 || m.getCurrentLoad() > 160);
            memberWorkloads.add(mw);
        }
        workload.put("members", memberWorkloads);
        
        // Aggregate statistics
        double totalLoad = members.stream().mapToDouble(Member::getCurrentLoad).sum();
        double avgLoad = totalLoad / members.size();
        double maxLoad = members.stream().mapToDouble(Member::getCurrentLoad).max().orElse(0);
        double minLoad = members.stream().mapToDouble(Member::getCurrentLoad).min().orElse(0);
        
        // Standard deviation
        double variance = members.stream()
            .mapToDouble(m -> Math.pow(m.getCurrentLoad() - avgLoad, 2))
            .average().orElse(0);
        double stdDev = Math.sqrt(variance);
        
        workload.put("averageLoad", Math.round(avgLoad * 100.0) / 100.0);
        workload.put("maxLoad", maxLoad);
        workload.put("minLoad", minLoad);
        workload.put("loadStandardDeviation", Math.round(stdDev * 100.0) / 100.0);
        workload.put("loadBalanceScore", calculateLoadBalanceScore(members));
        
        // Overloaded members count
        long overloadedCount = members.stream()
            .filter(m -> m.getRemainingHours() < 0 || m.getCurrentLoad() > 160)
            .count();
        workload.put("overloadedMembersCount", overloadedCount);
        
        return workload;
    }

    /**
     * Calculate load balance score (0-100, higher = better balance)
     */
    private double calculateLoadBalanceScore(List<Member> members) {
        if (members.isEmpty() || members.size() == 1) return 100.0;
        
        double avgLoad = members.stream().mapToDouble(Member::getCurrentLoad).average().orElse(0);
        if (avgLoad == 0) return 100.0;
        
        double maxDeviation = members.stream()
            .mapToDouble(m -> Math.abs(m.getCurrentLoad() - avgLoad))
            .max().orElse(0);
        
        // Score decreases with deviation from average
        double score = Math.max(0, 100 - (maxDeviation / avgLoad * 50));
        return Math.round(score * 100.0) / 100.0;
    }

    /**
     * Generate skill coverage report
     */
    public Map<String, Object> getSkillCoverage() {
        Map<String, Object> coverage = new LinkedHashMap<>();
        
        SkillDAO skillDAO = new SkillDAO(connection);
        MemberSkillDAO memberSkillDAO = new MemberSkillDAO(connection);
        TaskDAO taskDAO = new TaskDAO(connection);
        
        List<Skill> allSkills = skillDAO.getAllSkills();
        List<Task> allTasks = taskDAO.getAllTasks();
        
        // Skills summary
        List<Map<String, Object>> skillDetails = new ArrayList<>();
        int coveredSkillsCount = 0;
        
        for (Skill skill : allSkills) {
            Map<String, Object> sd = new LinkedHashMap<>();
            sd.put("id", skill.getId());
            sd.put("name", skill.getName());
            
            // Count members with this skill
            List<MemberSkill> membersWithSkill = memberSkillDAO.getMembersBySkill(skill.getId());
            sd.put("memberCount", membersWithSkill.size());
            sd.put("covered", membersWithSkill.size() > 0);
            
            if (membersWithSkill.size() > 0) {
                coveredSkillsCount++;
            }
            
            // Count tasks requiring this skill
            long tasksRequiringSkill = allTasks.stream()
                .filter(t -> t.getRequiredSkillIds() != null && t.getRequiredSkillIds().contains(skill.getId()))
                .count();
            sd.put("requiredByTasks", tasksRequiringSkill);
            
            // Calculate coverage gap (tasks require it but no members have it)
            sd.put("hasGap", tasksRequiringSkill > 0 && membersWithSkill.isEmpty());
            
            skillDetails.add(sd);
        }
        
        coverage.put("skills", skillDetails);
        coverage.put("totalSkills", allSkills.size());
        coverage.put("coveredSkills", coveredSkillsCount);
        coverage.put("coverageRate", allSkills.isEmpty() ? 100.0 : 
            Math.round((double) coveredSkillsCount / allSkills.size() * 10000.0) / 100.0);
        
        // Identify skill gaps
        List<String> gaps = skillDetails.stream()
            .filter(s -> Boolean.TRUE.equals(s.get("hasGap")))
            .map(s -> (String) s.get("name"))
            .collect(Collectors.toList());
        coverage.put("skillGaps", gaps);
        
        return coverage;
    }

    /**
     * Get task assignment details with member and skill information
     */
    public Map<String, Object> getAssignmentDetails() {
        Map<String, Object> details = new LinkedHashMap<>();
        
        TaskDAO taskDAO = new TaskDAO(connection);
        MemberDAO memberDAO = new MemberDAO(connection);
        MemberSkillDAO memberSkillDAO = new MemberSkillDAO(connection);
        SkillDAO skillDAO = new SkillDAO(connection);
        
        List<Task> tasks = taskDAO.getAllTasks();
        List<Member> members = memberDAO.getAllMembers();
        
        // Load skills for all members
        for (Member member : members) {
            loadMemberSkills(member, memberSkillDAO, skillDAO);
        }
        
        // Task details with assignment info
        List<Map<String, Object>> taskDetails = new ArrayList<>();
        for (Task task : tasks) {
            Map<String, Object> td = new LinkedHashMap<>();
            td.put("id", task.getId());
            td.put("name", task.getName());
            td.put("priority", task.getPriority());
            td.put("status", task.getStatus());
            td.put("estimatedDuration", task.getEstimatedDuration());
            td.put("assigneeId", task.getAssigneeId());
            td.put("assigned", task.getAssigneeId() > 0);
            
            // Required skills names
            List<Skill> requiredSkills = taskDAO.getTaskRequiredSkillsWithNames(task.getId());
            td.put("requiredSkills", requiredSkills.stream().map(Skill::getName).collect(Collectors.toList()));
            td.put("requiredSkillIds", task.getRequiredSkillIds());
            
            // Assignee info
            if (task.getAssigneeId() > 0) {
                Member assignee = members.stream()
                    .filter(m -> m.getId() == task.getAssigneeId())
                    .findFirst().orElse(null);
                if (assignee != null) {
                    td.put("assigneeName", assignee.getFirstName() + " " + assignee.getLastName());
                    td.put("assigneeSkills", assignee.getMemberSkills().stream()
                        .map(ms -> ms.getSkill() != null ? ms.getSkill().getName() : "Unknown")
                        .collect(Collectors.toList()));
                }
            }
            
            taskDetails.add(td);
        }
        
        details.put("tasks", taskDetails);
        details.put("totalTasks", tasks.size());
        details.put("assignedCount", tasks.stream().filter(t -> t.getAssigneeId() > 0).count());
        details.put("unassignedCount", tasks.stream().filter(t -> t.getAssigneeId() == 0).count());
        
        // Member assignment summary
        List<Map<String, Object>> memberAssignments = new ArrayList<>();
        for (Member member : members) {
            Map<String, Object> ma = new LinkedHashMap<>();
            ma.put("id", member.getId());
            ma.put("name", member.getFirstName() + " " + member.getLastName());
            ma.put("skills", member.getMemberSkills().stream()
                .map(ms -> ms.getSkill() != null ? ms.getSkill().getName() : "Unknown")
                .collect(Collectors.toList()));
            
            // Count assigned tasks
            long assignedCount = tasks.stream()
                .filter(t -> t.getAssigneeId() == member.getId())
                .count();
            ma.put("assignedTaskCount", assignedCount);
            ma.put("currentLoad", member.getCurrentLoad());
            ma.put("remainingHours", member.getRemainingHours());
            
            memberAssignments.add(ma);
        }
        details.put("memberAssignments", memberAssignments);
        
        return details;
    }

    /**
     * Helper to load member skills with Skill objects populated
     */
    private void loadMemberSkills(Member member, MemberSkillDAO memberSkillDAO, SkillDAO skillDAO) {
        List<MemberSkill> memberSkills = memberSkillDAO.getSkillsByMember(member.getId());
        for (MemberSkill ms : memberSkills) {
            Skill skill = skillDAO.getSkillById(ms.getSkillId());
            ms.setSkill(skill);
        }
        member.setMemberSkills(new ArrayList<>(memberSkills));
    }

    /**
     * Get timeline data for visualization
     */
    public Map<String, Object> getTimelineData() {
        Map<String, Object> timeline = new LinkedHashMap<>();
        
        ProjectDAO projectDAO = new ProjectDAO(connection);
        TaskDAO taskDAO = new TaskDAO(connection);
        MemberDAO memberDAO = new MemberDAO(connection);
        
        List<Project> projects = projectDAO.getAllProjects();
        List<Task> tasks = taskDAO.getAllTasks();
        List<Member> members = memberDAO.getAllMembers();
        
        // Projects timeline
        List<Map<String, Object>> projectTimeline = new ArrayList<>();
        for (Project p : projects) {
            Map<String, Object> pt = new LinkedHashMap<>();
            pt.put("id", p.getId());
            pt.put("name", p.getName());
            pt.put("startDate", p.getStartDate() != null ? p.getStartDate().toString() : null);
            pt.put("endDate", p.getEndDate() != null ? p.getEndDate().toString() : null);
            pt.put("status", p.getStatus());
            
            // Tasks for this project
            List<Task> projectTasks = tasks.stream()
                .filter(t -> t.getProjectId() != null && t.getProjectId() == p.getId())
                .collect(Collectors.toList());
            pt.put("taskCount", projectTasks.size());
            pt.put("completedTaskCount", projectTasks.stream()
                .filter(t -> "Completed".equals(t.getStatus()) || "Terminé".equals(t.getStatus()))
                .count());
            
            projectTimeline.add(pt);
        }
        timeline.put("projects", projectTimeline);
        
        // Tasks timeline
        List<Map<String, Object>> taskTimeline = new ArrayList<>();
        for (Task t : tasks) {
            Map<String, Object> tt = new LinkedHashMap<>();
            tt.put("id", t.getId());
            tt.put("name", t.getName());
            tt.put("startDate", t.getPlannedStartDate() != null ? t.getPlannedStartDate().toString() : null);
            tt.put("endDate", t.getPlannedEndDate() != null ? t.getPlannedEndDate().toString() : null);
            tt.put("status", t.getStatus());
            tt.put("priority", t.getPriority());
            tt.put("assigneeId", t.getAssigneeId());
            tt.put("estimatedDuration", t.getEstimatedDuration());
            
            // Assignee name
            if (t.getAssigneeId() > 0) {
                members.stream()
                    .filter(m -> m.getId() == t.getAssigneeId())
                    .findFirst()
                    .ifPresent(m -> tt.put("assigneeName", m.getFirstName() + " " + m.getLastName()));
            }
            
            taskTimeline.add(tt);
        }
        timeline.put("tasks", taskTimeline);
        
        return timeline;
    }

    /**
     * Generate complete report combining all statistics
     */
    public Map<String, Object> getCompleteReport() {
        Map<String, Object> report = new LinkedHashMap<>();
        
        report.put("generatedAt", java.time.LocalDateTime.now().toString());
        report.put("projectStatistics", getProjectStatistics());
        report.put("workloadDistribution", getWorkloadDistribution());
        report.put("skillCoverage", getSkillCoverage());
        report.put("assignmentDetails", getAssignmentDetails());
        report.put("timeline", getTimelineData());
        
        return report;
    }
}
