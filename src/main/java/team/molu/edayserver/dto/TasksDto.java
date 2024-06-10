package team.molu.edayserver.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Getter;
import lombok.extern.jackson.Jacksonized;

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
    }

    @Getter
    @Builder
    public static class TaskUpdateRequest {
        private String taskId;
        private String name;
        private String memo;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer priority;
        private Boolean check;
    }

    @Getter
    @Builder
    public static class TaskDeleteRequest {
        private String taskId;
        private Boolean cascade;
    }

    @Getter
    @Builder
    public static class TaskRestoreRequest {
        private String parentId;
        private String taskId;
    }

    @Getter
    @Builder
    public static class TaskMoveRequest {
        private String parentId;
        private String taskId;
    }

    @Getter
    @Builder
    @Jacksonized
    public static class TaskArchiveRequest {
        private String taskId;
    }

    @Getter
    @Builder
    public static class TaskDeleteResponse {
        private String taskId;
        private Integer deletedNodes;
    }

    @Getter
    @Builder
    public static class EmptyTrashResponse {
        private Integer deletedNodes;
    }

    @Getter
    @Builder
    public static class TaskRestoreResponse {
        private String taskId;
        private String parentId;
        private Integer restoredNodes;
    }

    @Getter
    @Builder
    public static class TaskMoveResponse {
        private String taskId;
        private String parentId;
        private Integer movedNodes;
    }

    @Getter
    @Builder
    public static class TaskArchiveResponse {
        private String taskId;
        private Integer archivedNodes;
    }

    @Getter
    @Builder
    public static class SearchTasksResponse {
        private List<TaskResponse> taskList;
    }

    @Getter
    @Builder
    public static class TaskResponse {
        private String taskId;
        private String name;
        private String memo;
        private LocalDateTime startDate;
        private LocalDateTime endDate;
        private Integer priority;
        private Boolean check;
    }

    @Getter
    @Builder
    public static class TaskRouteResponse {
        private List<TaskRouteDto> routes;
    }

    @Getter
    @Builder
    public static class TaskRouteDto {
        private String taskId;
        private String name;
        private Integer order;
    }
}
