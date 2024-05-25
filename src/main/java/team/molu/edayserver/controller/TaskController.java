package team.molu.edayserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.molu.edayserver.dto.TasksDto;
import team.molu.edayserver.service.TaskService;

@RestController
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) { this.taskService = taskService; }

    @GetMapping("tasks/roots")
    public ResponseEntity<TasksDto.SearchTasksResponse> findRootTasks(@RequestParam String email) {
        TasksDto.SearchTasksResponse tasks = taskService.findTaskByRoot(email);
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("a-task-detail/{taskId}")
    public ResponseEntity<TasksDto.SearchTasksResponse> findChildTasks(@PathVariable String taskId) {
        TasksDto.SearchTasksResponse tasks = taskService.findTaskById(taskId);
        return ResponseEntity.ok(tasks);
    }
}
