package com.company.jmixpm.app;

import com.company.jmixpm.entity.Project;
import com.company.jmixpm.entity.ProjectStats;
import com.company.jmixpm.entity.Task;
import io.jmix.core.DataManager;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class ProjectStatsService {

    private final DataManager dataManager;

    public ProjectStatsService(DataManager dataManager) {
        this.dataManager = dataManager;
    }

    public List<ProjectStats> fetchProjectStatistics() {
        List<Project> projects = dataManager.load(Project.class).all().fetchPlan("project-with-tasks").list();
        List<ProjectStats> projectStats = projects.stream()
                .map(project -> {
                    ProjectStats stat = dataManager.create(ProjectStats.class);
                    stat.setId(project.getId());
                    stat.setProjectName(project.getName());
                    stat.setTasksCount(project.getTasks().size());
                    Integer estimatedEfforts =
                            project.getTasks().stream()
                                    .map(Task::getEstimatedEfforts).reduce(0, Integer::sum);
                    stat.setPlannedEfforts(estimatedEfforts);
                    stat.setActualEfforts(getActualEfforts(project.getId()));
                    return stat;
                })
                .toList();

        return projectStats;
    }

    private Integer getActualEfforts(UUID projectId) {
        return dataManager.loadValue("select sum(te.timeSpent) from TimeEntry te " +
                "where te.task.project.id = :projectId", Integer.class)
                .parameter("projectId", projectId)
                .one();
    }
}