package team.molu.edayserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.molu.edayserver.dto.TasksDto;
import team.molu.edayserver.service.TaskService;

@RestController()
@RequestMapping("api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) { this.taskService = taskService; }

    @GetMapping("/roots")
    public ResponseEntity<TasksDto.SearchTasksResponse> findRootTasks(@RequestParam String email) {
        TasksDto.SearchTasksResponse tasks = taskService.findTaskByRoot(email);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<TasksDto.TaskResponse> findTaskDetail(@PathVariable String taskId) {
        TasksDto.TaskResponse tasks = taskService.findTaskById(taskId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}/subtasks")
    public ResponseEntity<TasksDto.SearchTasksResponse> findChildTasks(@PathVariable String taskId) {
        TasksDto.SearchTasksResponse tasks = taskService.findSubtaskById(taskId);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}/routes")
    public ResponseEntity<TasksDto.TaskRouteResponse> getTaskRoutesById(@PathVariable String taskId) {
        TasksDto.TaskRouteResponse routes = taskService.getTaskRoutes(taskId);
        return ResponseEntity.ok(routes);
    }

    @PostMapping
    public ResponseEntity<TasksDto.TaskResponse> createTask(@RequestParam String email, @RequestBody TasksDto.TaskCreateRequest taskDto) {
        TasksDto.TaskResponse task = taskService.createTask(email, taskDto);
        return ResponseEntity.ok(task);
    }

    @PatchMapping
    public ResponseEntity<TasksDto.TaskResponse> updateTask(@RequestBody TasksDto.TaskUpdateRequest taskDto) {
        TasksDto.TaskResponse task = taskService.updateTask(taskDto);
        return ResponseEntity.ok(task);
    }
}
