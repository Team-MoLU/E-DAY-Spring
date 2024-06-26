package team.molu.edayserver.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Task;
import team.molu.edayserver.dto.TasksDto;
import team.molu.edayserver.exception.TaskNotFoundException;
import team.molu.edayserver.repository.TaskRepository;
import team.molu.edayserver.util.SecurityUtils;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;
import static org.springframework.test.util.AssertionErrors.assertEquals;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
    private TaskService taskService;

    @Test
    void findTaskByRoot_shouldReturnTasksDto() {
        // Given
        String email = "test@example.com";
        List<Task> tasks = Arrays.asList(
                new Task("1", "Task 1", "Memo 1", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, false),
                new Task("2", "Task 2", "Memo 2", LocalDateTime.now(), LocalDateTime.now().plusDays(2), 2, true)
        );
        when(taskRepository.findRootTasks(email)).thenReturn(Flux.fromIterable(tasks));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.SearchTasksResponse result = taskService.findTaskByRoot();

            // Then
            assertEquals("Task List Size Test", 2, result.getTaskList().size());
            assertEquals("Task1 Properties Test", "Task 1", result.getTaskList().get(0).getName());
            verify(taskRepository, times(1)).findRootTasks(email);
        }
    }

    @Test
    void findTaskById_shouldReturnTaskDto_whenTaskExists() {
        // Given
        String taskId = "1";
        Task task = new Task(taskId, "Task 1", "Memo 1", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, false);
        when(taskRepository.findTaskById(taskId)).thenReturn(Mono.just(task));

        // When
        TasksDto.TaskResponse result = taskService.findTaskById(taskId);

        // Then
        assertEquals("TaskIdExist Test", taskId, result.getTaskId());
        verify(taskRepository, times(1)).findTaskById(taskId);
    }

    @Test
    void findTaskById_shouldThrowTaskNotFoundException_whenTaskDoesNotExist() {
        // Given
        String taskId = "1";
        when(taskRepository.findTaskById(taskId)).thenReturn(Mono.empty());

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> taskService.findTaskById(taskId));
        verify(taskRepository, times(1)).findTaskById(taskId);
    }

    @Test
    void findSubtaskById_shouldReturnTasksDto() {
        // Given
        String taskId = "1";
        List<Task> subtasks = Arrays.asList(
                new Task("2", "Subtask 1", "Memo 1", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, false),
                new Task("3", "Subtask 2", "Memo 2", LocalDateTime.now(), LocalDateTime.now().plusDays(2), 2, true)
        );
        when(taskRepository.findSubtaskById(taskId)).thenReturn(Flux.fromIterable(subtasks));

        // When
        TasksDto.SearchTasksResponse result = taskService.findSubtaskById(taskId);

        // Then
        assertEquals("SubTask List Size Test",2, result.getTaskList().size());
        assertEquals("SubTask Properties Test","2", result.getTaskList().get(0).getTaskId());
        assertEquals("SubTask Properties Test2","3", result.getTaskList().get(1).getTaskId());
        verify(taskRepository, times(1)).findSubtaskById(taskId);
    }

    @Test
    void getTaskRoutes_shouldReturnTaskRouteResponse() {
        // Given
        String taskId = "1";
        List<TasksDto.TaskRouteDto> routes = Arrays.asList(
                TasksDto.TaskRouteDto.builder()
                        .taskId("1")
                        .name("Task 1")
                        .order(1)
                        .build(),
                TasksDto.TaskRouteDto.builder()
                        .taskId("2")
                        .name("Task 2")
                        .order(2)
                        .build(),
                TasksDto.TaskRouteDto.builder()
                        .taskId("3")
                        .name("Task 3")
                        .order(0)
                        .build()
        );
        when(taskRepository.findRoutesById(taskId)).thenReturn(Flux.fromIterable(routes));

        // When
        TasksDto.TaskRouteResponse result = taskService.getTaskRoutes(taskId);

        // Then
        assertEquals("RouteSize Test",3, result.getRoutes().size());
        assertEquals("RouteID Test",0, result.getRoutes().get(2).getOrder());
        verify(taskRepository, times(1)).findRoutesById(taskId);
    }

    @Test
    void createTask_shouldReturnTaskResponse_whenParentIdIsZero() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskCreateRequest request = TasksDto.TaskCreateRequest.builder()
                .name("Task 1")
                .memo("Memo 1")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .priority(1)
                .parentId("0")
                .build();

        Task createdTask = new Task("1", "Task 1", "Memo 1", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, false);
        when(taskRepository.createTaskWithRootParent(any())).thenReturn(Mono.just(createdTask));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.TaskResponse result = taskService.createTask(request);

            // Then
            assertEquals("CreateTaskID Test","1", result.getTaskId());
            assertEquals("CreateTaskName Test","Task 1", result.getName());
            verify(taskRepository, times(1)).createTaskWithRootParent(any());
        }
    }

    @Test
    void createTask_shouldReturnTaskResponse_whenParentIdIsNotZero() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskCreateRequest request = TasksDto.TaskCreateRequest.builder()
                .name("Task 1")
                .memo("Memo 1")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .priority(1)
                .parentId("1")
                .build();
        Task createdTask = new Task("2", "Task 1", "Memo 1", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, false);
        when(taskRepository.createTaskWithParent(any())).thenReturn(Mono.just(createdTask));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.TaskResponse result = taskService.createTask(request);

            // Then
            assertEquals("CreateTaskID with ParentId Test","2", result.getTaskId());
            assertEquals("CreateTaskName with ParentId Test","Task 1", result.getName());
            verify(taskRepository, times(1)).createTaskWithParent(any());
        }
    }

    @Test
    void updateTask_shouldReturnUpdatedTaskResponse_whenTaskExists() {
        // Given
        TasksDto.TaskUpdateRequest request = TasksDto.TaskUpdateRequest.builder()
                .taskId("1")
                .name("Updated Task")
                .memo("Updated Memo")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .priority(2)
                .check(true)
                .build();
        Task existingTask = new Task("1", "Task 1", "Memo 1", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 1, false);
        Task updatedTask = new Task("1", "Updated Task", "Updated Memo", LocalDateTime.now(), LocalDateTime.now().plusDays(1), 2, true);
        when(taskRepository.findTaskById("1")).thenReturn(Mono.just(existingTask));
        when(taskRepository.updateTask(any())).thenReturn(Mono.just(updatedTask));

        // When
        TasksDto.TaskResponse result = taskService.updateTask(request);

        // Then
        assertEquals("UpdateTaskID Test","1", result.getTaskId());
        assertEquals("UpdateTaskName Test","Updated Task", result.getName());
        assertEquals("UpdateTaskMemo Test","Updated Memo", result.getMemo());
        assertEquals("UpdateTaskPriority Test", 2, result.getPriority());
        assertEquals("UpdateTaskCheck Test", true, result.getCheck());
        verify(taskRepository, times(1)).findTaskById("1");
        verify(taskRepository, times(1)).updateTask(any());
    }

    @Test
    void updateTask_shouldThrowTaskNotFoundException_whenTaskDoesNotExist() {
        // Given
        TasksDto.TaskUpdateRequest request = TasksDto.TaskUpdateRequest.builder()
                .taskId("1")
                .name("Updated Task")
                .memo("Updated Memo")
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(1))
                .priority(2)
                .check(true)
                .build();
        when(taskRepository.findTaskById("1")).thenReturn(Mono.empty());

        // When & Then
        assertThrows(TaskNotFoundException.class, () -> taskService.updateTask(request));
        verify(taskRepository, times(1)).findTaskById("1");
        verify(taskRepository, never()).updateTask(any());
    }

    @Test
    void deleteTask_shouldReturnTaskDeleteResponse_whenCascadeIsTrue() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskDeleteRequest request = TasksDto.TaskDeleteRequest.builder()
                .taskId("1")
                .cascade(true)
                .build();
        when(taskRepository.deleteTaskByIdWithCascade(email, "1")).thenReturn(Mono.just(3));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.TaskDeleteResponse result = taskService.deleteTask(request);

            // Then
            assertEquals("DeleteTaskId Test","1", result.getTaskId());
            assertEquals("DeleteTaskDeletedNodes Test",3, result.getDeletedNodes());
            verify(taskRepository, times(1)).deleteTaskByIdWithCascade(email, "1");
            verify(taskRepository, never()).deleteTaskById(anyString(), anyString());
        }
    }

    @Test
    void deleteTask_shouldReturnTaskDeleteResponse_whenCascadeIsFalse() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskDeleteRequest request = TasksDto.TaskDeleteRequest.builder()
                .taskId("1")
                .cascade(false)
                .build();
        when(taskRepository.deleteTaskById(email, "1")).thenReturn(Mono.just(1));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.TaskDeleteResponse result = taskService.deleteTask(request);

            // Then
            assertEquals("DeleteTaskId Test","1", result.getTaskId());
            assertEquals("DeleteTaskDeletedNodes Test",1, result.getDeletedNodes());
            verify(taskRepository, times(1)).deleteTaskById(email, "1");
            verify(taskRepository, never()).deleteTaskByIdWithCascade(anyString(), anyString());
        }
    }

    @Test
    void dropTask_shouldReturnTaskDeleteResponse_whenCascadeIsTrue() {
        // Given
        TasksDto.TaskDeleteRequest request = TasksDto.TaskDeleteRequest.builder()
                .taskId("1")
                .cascade(true)
                .build();
        when(taskRepository.dropTaskByIdWithCascade("1")).thenReturn(Mono.just(3));

        // When
        TasksDto.TaskDeleteResponse result = taskService.dropTask(request);

        // Then
        assertEquals("DropTaskId Test","1", result.getTaskId());
        assertEquals("DropTaskDeletedNodes Test",3, result.getDeletedNodes());
        verify(taskRepository, times(1)).dropTaskByIdWithCascade("1");
        verify(taskRepository, never()).dropTaskById(anyString());
    }

    @Test
    void dropTask_shouldReturnTaskDeleteResponse_whenCascadeIsFalse() {
        // Given
        TasksDto.TaskDeleteRequest request = TasksDto.TaskDeleteRequest.builder()
                .taskId("1")
                .cascade(false)
                .build();
        when(taskRepository.dropTaskById("1")).thenReturn(Mono.just(1));

        // When
        TasksDto.TaskDeleteResponse result = taskService.dropTask(request);

        // Then
        assertEquals("DropTaskId Test","1", result.getTaskId());
        assertEquals("DropTaskDeletedNodes Test",1, result.getDeletedNodes());
        verify(taskRepository, times(1)).dropTaskById("1");
        verify(taskRepository, never()).dropTaskByIdWithCascade(anyString());
    }

    @Test
    void dropAllTask_shouldReturnEmptyTrashResponse() {
        // Given
        String email = "test@example.com";
        when(taskRepository.emptyTrash(email)).thenReturn(Mono.just(5));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.EmptyTrashResponse result = taskService.dropAllTask();

            // Then
            assertEquals("DropAllTaskDeletedNodes Test",5, result.getDeletedNodes());
            verify(taskRepository, times(1)).emptyTrash(email);
        }
    }

    @Test
    void restoreTask_shouldReturnTaskRestoreResponse_whenParentIdIsZero() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskRestoreRequest request = TasksDto.TaskRestoreRequest.builder()
                .taskId("1")
                .parentId("0")
                .build();
        when(taskRepository.restoreTaskById(email, "root", "1")).thenReturn(Mono.just(2));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.TaskRestoreResponse result = taskService.restoreTask(request);

            // Then
            assertEquals("RestoreTaskId Test","1", result.getTaskId());
            assertEquals("RestoreTaskParentId Test","0", result.getParentId());
            assertEquals("RestoreTaskRestoredNodes Test",2, result.getRestoredNodes());
            verify(taskRepository, times(1)).restoreTaskById(email, "root", "1");
        }
    }

    @Test
    void restoreTask_shouldReturnTaskRestoreResponse_whenParentIdIsNotZero() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskRestoreRequest request = TasksDto.TaskRestoreRequest.builder()
                .taskId("1")
                .parentId("2")
                .build();
        when(taskRepository.restoreTaskById(email, "2", "1")).thenReturn(Mono.just(2));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.TaskRestoreResponse result = taskService.restoreTask(request);

            // Then
            assertEquals("RestoreTaskId Test","1", result.getTaskId());
            assertEquals("RestoreTaskParentId Test","2", result.getParentId());
            assertEquals("RestoreTaskRestoredNodes Test",2, result.getRestoredNodes());
            verify(taskRepository, times(1)).restoreTaskById(email, "2", "1");
            verify(taskRepository, never()).restoreTaskById(anyString(), eq("root"), anyString());
        }
    }

//    @Test
//    void moveTask_shouldReturnTaskMoveResponse_whenParentIdIsZero() {
//        // Given
//        String email = "test@example.com";
//        TasksDto.TaskMoveRequest request = TasksDto.TaskMoveRequest.builder()
//                .taskId("1")
//                .parentId("0")
//                .build();
//        when(taskRepository.moveTaskById(email, "root", "1")).thenReturn(Mono.just(2));
//
//        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
//            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);
//
//            // When
//            TasksDto.TaskMoveResponse result = taskService.moveTask(request);
//
//            // Then
//            assertEquals("MoveTaskId Test","1", result.getTaskId());
//            assertEquals("MoveTaskParentId Test","0", result.getParentId());
//            assertEquals("MoveTaskMovedNodes Test",2, result.getMovedNodes());
//            verify(taskRepository, times(1)).moveTaskById(email, "root", "1");
//        }
//    }
//
//    @Test
//    void moveTask_shouldReturnTaskMoveResponse_whenParentIdIsNotZero() {
//        // Given
//        String email = "test@example.com";
//        TasksDto.TaskMoveRequest request = TasksDto.TaskMoveRequest.builder()
//                .taskId("1")
//                .parentId("2")
//                .build();
//        when(taskRepository.moveTaskById(email, "2", "1")).thenReturn(Mono.just(2));
//
//        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
//            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);
//
//            // When
//            TasksDto.TaskMoveResponse result = taskService.moveTask(request);
//
//            // Then
//            assertEquals("MoveTaskId Test","1", result.getTaskId());
//            assertEquals("MoveTaskParentId Test","2", result.getParentId());
//            assertEquals("MoveTaskMovedNodes Test",2, result.getMovedNodes());
//            verify(taskRepository, times(1)).moveTaskById(email, "2", "1");
//            verify(taskRepository, never()).moveTaskById(anyString(), eq("root"), anyString());
//        }
//    }

    @Test
    void archiveTask_shouldReturnTaskArchiveResponse() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskArchiveRequest request = TasksDto.TaskArchiveRequest.builder()
                .taskId("1")
                .build();
        when(taskRepository.archiveTaskById(email, "1")).thenReturn(Mono.just(3));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.TaskArchiveResponse result = taskService.archiveTask(request);

            // Then
            assertEquals("ArchiveTaskId Test","1", result.getTaskId());
            assertEquals("ArchiveTaskArchivedNodes Test",3, result.getArchivedNodes());
            verify(taskRepository, times(1)).archiveTaskById(email, "1");
        }
    }

    @Test
    void unarchiveTask_shouldReturnTaskUnarchiveResponse_whenParentIdIsNull() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskUnarchiveRequest request = TasksDto.TaskUnarchiveRequest.builder()
                .taskId("1")
                .parentId(null)
                .build();
        TasksDto.TaskUnarchiveResponse response = TasksDto.TaskUnarchiveResponse.builder()
                .taskId("1")
                .parentId("2")
                .unarchivedNodes(2)
                .build();
        when(taskRepository.unarchiveTaskByIdWithOriginalParent(email, "1")).thenReturn(Mono.just(response));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.TaskUnarchiveResponse result = taskService.unarchiveTask(request);

            // Then
            assertEquals("UnarchiveTaskId Test","1", result.getTaskId());
            assertEquals("UnarchiveTaskParentId Test","2", result.getParentId());
            assertEquals("UnarchiveTaskUnarchivedNodes Test",2, result.getUnarchivedNodes());
            verify(taskRepository, times(1)).unarchiveTaskByIdWithOriginalParent(email, "1");
            verify(taskRepository, never()).unarchiveTaskByIdWithSpecificParent(anyString(), anyString(), anyString());
        }
    }

    @Test
    void unarchiveTask_shouldReturnTaskUnarchiveResponse_whenParentIdIsZero() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskUnarchiveRequest request = TasksDto.TaskUnarchiveRequest.builder()
                .taskId("1")
                .parentId("0")
                .build();
        TasksDto.TaskUnarchiveResponse response = TasksDto.TaskUnarchiveResponse.builder()
                .taskId("1")
                .parentId("0")
                .unarchivedNodes(2)
                .build();
        when(taskRepository.unarchiveTaskByIdWithSpecificParent(email, "root", "1")).thenReturn(Mono.just(response));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.TaskUnarchiveResponse result = taskService.unarchiveTask(request);

            // Then
            assertEquals("UnarchiveTaskId Test","1", result.getTaskId());
            assertEquals("UnarchiveTaskParentId Test","0", result.getParentId());
            assertEquals("UnarchiveTaskUnarchivedNodes Test",2, result.getUnarchivedNodes());
            verify(taskRepository, times(1)).unarchiveTaskByIdWithSpecificParent(email, "root", "1");
            verify(taskRepository, never()).unarchiveTaskByIdWithOriginalParent(anyString(), anyString());
        }
    }

    @Test
    void unarchiveTask_shouldReturnTaskUnarchiveResponse_whenParentIdIsNotZero() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskUnarchiveRequest request = TasksDto.TaskUnarchiveRequest.builder()
                .taskId("1")
                .parentId("2")
                .build();
        TasksDto.TaskUnarchiveResponse response = TasksDto.TaskUnarchiveResponse.builder()
                .taskId("1")
                .parentId("2")
                .unarchivedNodes(2)
                .build();
        when(taskRepository.unarchiveTaskByIdWithSpecificParent(email, "2", "1")).thenReturn(Mono.just(response));

        try (MockedStatic<SecurityUtils> utilities = mockStatic(SecurityUtils.class)) {
            utilities.when(SecurityUtils::getAuthenticatedUserEmail).thenReturn(email);

            // When
            TasksDto.TaskUnarchiveResponse result = taskService.unarchiveTask(request);

            // Then
            assertEquals("UnarchiveTaskId Test","1", result.getTaskId());
            assertEquals("UnarchiveTaskParentId Test","2", result.getParentId());
            assertEquals("UnarchiveTaskUnarchivedNodes Test",2, result.getUnarchivedNodes());
            verify(taskRepository, times(1)).unarchiveTaskByIdWithSpecificParent(email, "2", "1");
            verify(taskRepository, never()).unarchiveTaskByIdWithOriginalParent(anyString(), anyString());
        }
    }
}