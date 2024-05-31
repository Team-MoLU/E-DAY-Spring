package team.molu.edayserver.dto;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

public class TasksDto {
    @Getter
    @Builder
    public static class TaskCreateRequest {
        private String parentId;
        private String name;
        private String memo;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer priority;
        private Boolean check;
        private Boolean archive;
    }

    @Getter
    @Builder
    public static class SearchTasksResponse {
        private List<TaskResponse> taskList;
    }

    @Getter
    @Builder
    public static class TaskCreateResponse {
        private TaskResponse task;
    }

    @Getter
    @Builder
    public static class TaskResponse {
        private String parentId;
        private String taskId;
        private String name;
        private String memo;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer priority;
        private Boolean check;
        private Boolean archive;
    }

    @Getter
    @Builder
    public static class TaskRouteResponse {
        private List<TaskRouteDto> routes;
    }

    @Getter
    @Builder
    public static class TaskRouteDto {
        private String id;
        private String name;
        private Integer order;
    }
}
