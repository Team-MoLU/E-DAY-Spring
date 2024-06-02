package team.molu.edayserver.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Task;
import team.molu.edayserver.dto.TasksDto;
import team.molu.edayserver.exception.TaskNotFoundException;
import team.molu.edayserver.repository.TaskRepository;

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

        // When
        TasksDto.SearchTasksResponse result = taskService.findTaskByRoot(email);

        // Then
        assertEquals("Task List Size Test",2, result.getTaskList().size());
        assertEquals("Task1 Properties Test", "Task 1", result.getTaskList().get(0).getName());
        verify(taskRepository, times(1)).findRootTasks(email);
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
                        .id("1")
                        .name("Task 1")
                        .order(1)
                        .build(),
                TasksDto.TaskRouteDto.builder()
                        .id("2")
                        .name("Task 2")
                        .order(2)
                        .build(),
                TasksDto.TaskRouteDto.builder()
                        .id("3")
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

        // When
        TasksDto.TaskResponse result = taskService.createTask(email, request);

        // Then
        assertEquals("CreateTaskID Test","1", result.getTaskId());
        assertEquals("CreateTaskName Test","Task 1", result.getName());
        verify(taskRepository, times(1)).createTaskWithRootParent(any());
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

        // When
        TasksDto.TaskResponse result = taskService.createTask(email, request);

        // Then
        assertEquals("CreateTaskID with ParentId Test","2", result.getTaskId());
        assertEquals("CreateTaskName with ParentId Test","Task 1", result.getName());
        verify(taskRepository, times(1)).createTaskWithParent(any());
    }

    @Test
    void updateTask_shouldReturnUpdatedTaskResponse_whenTaskExists() {
        // Given
        TasksDto.TaskUpdateRequest request = TasksDto.TaskUpdateRequest.builder()
                .id("1")
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
                .id("1")
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
                .id("1")
                .cascade(true)
                .build();
        when(taskRepository.deleteTaskByIdWithCascade(email, "1")).thenReturn(Mono.just(3));

        // When
        TasksDto.TaskDeleteResponse result = taskService.deleteTask(email, request);

        // Then
        assertEquals("Test","1", result.getId());
        assertEquals("Test",3, result.getDeletedNodes());
        verify(taskRepository, times(1)).deleteTaskByIdWithCascade(email, "1");
        verify(taskRepository, never()).deleteTaskById(anyString(), anyString());
    }

    @Test
    void deleteTask_shouldReturnTaskDeleteResponse_whenCascadeIsFalse() {
        // Given
        String email = "test@example.com";
        TasksDto.TaskDeleteRequest request = TasksDto.TaskDeleteRequest.builder()
                .id("1")
                .cascade(false)
                .build();
        when(taskRepository.deleteTaskById(email, "1")).thenReturn(Mono.just(1));

        // When
        TasksDto.TaskDeleteResponse result = taskService.deleteTask(email, request);

        // Then
        assertEquals("Test","1", result.getId());
        assertEquals("Test",1, result.getDeletedNodes());
        verify(taskRepository, times(1)).deleteTaskById(email, "1");
        verify(taskRepository, never()).deleteTaskByIdWithCascade(anyString(), anyString());
    }
}