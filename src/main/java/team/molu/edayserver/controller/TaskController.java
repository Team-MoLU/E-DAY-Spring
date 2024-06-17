package team.molu.edayserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import team.molu.edayserver.domain.Task;
import team.molu.edayserver.dto.TasksDto;
import team.molu.edayserver.service.TaskService;

@RestController()
@RequestMapping("api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) { this.taskService = taskService; }

    @GetMapping("/roots")
    public ResponseEntity<TasksDto.SearchTasksResponse> findRootTasks() {
        TasksDto.SearchTasksResponse tasks = taskService.findTaskByRoot();
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
    public ResponseEntity<TasksDto.TaskResponse> createTask(@RequestBody TasksDto.TaskCreateRequest taskDto) {
        TasksDto.TaskResponse task = taskService.createTask(taskDto);
        return ResponseEntity.ok(task);
    }

    @PatchMapping
    public ResponseEntity<TasksDto.TaskResponse> updateTask(@RequestBody TasksDto.TaskUpdateRequest taskDto) {
        TasksDto.TaskResponse task = taskService.updateTask(taskDto);
        return ResponseEntity.ok(task);
    }

    @PostMapping("/delete")
    public ResponseEntity<TasksDto.TaskDeleteResponse> deleteTask(@RequestBody TasksDto.TaskDeleteRequest taskDto) {
        TasksDto.TaskDeleteResponse taskDeleteResponse = taskService.deleteTask(taskDto);
        return ResponseEntity.ok(taskDeleteResponse);
    }

    @PostMapping("/restore")
    public ResponseEntity<TasksDto.TaskRestoreResponse> restoreTask(@RequestBody TasksDto.TaskRestoreRequest taskDto) {
        TasksDto.TaskRestoreResponse taskRestoreResponse = taskService.restoreTask(taskDto);
        return ResponseEntity.ok(taskRestoreResponse);
    }

    @DeleteMapping("/drop")
    public ResponseEntity<TasksDto.TaskDeleteResponse> dropTask(@RequestBody TasksDto.TaskDeleteRequest taskDto) {
        TasksDto.TaskDeleteResponse taskDropResponse = taskService.dropTask(taskDto);
        return ResponseEntity.ok(taskDropResponse);
    }

    @DeleteMapping("/drop/all")
    public ResponseEntity<TasksDto.EmptyTrashResponse> dropAllTasks() {
        TasksDto.EmptyTrashResponse emptyTrashResponse = taskService.dropAllTask();
        return  ResponseEntity.ok(emptyTrashResponse);
    }

    @PostMapping("/move")
    public ResponseEntity<TasksDto.TaskMoveResponse> moveTask(@RequestBody TasksDto.TaskMoveRequest taskDto) {
        TasksDto.TaskMoveResponse taskMoveResponse = taskService.moveTask(taskDto);
        return ResponseEntity.ok(taskMoveResponse);
    }

    @PostMapping("/archive")
    public ResponseEntity<TasksDto.TaskArchiveResponse> archiveTask(@RequestBody TasksDto.TaskArchiveRequest taskDto) {
        TasksDto.TaskArchiveResponse taskArchiveResponse = taskService.archiveTask(taskDto);
        return ResponseEntity.ok(taskArchiveResponse);
    }

    @PostMapping("/unarchive")
    public ResponseEntity<TasksDto.TaskUnarchiveResponse> unarchiveTask(@RequestBody TasksDto.TaskUnarchiveRequest taskDto) {
        TasksDto.TaskUnarchiveResponse taskUnarchiveResponse = taskService.unarchiveTask(taskDto);
        return ResponseEntity.ok(taskUnarchiveResponse);
    }

    @GetMapping(value = "", params = {"startDate","endDate"})
    public ResponseEntity<TasksDto.SearchTasksResponse> findTasksByDate(@RequestParam("startDate") String startDate, @RequestParam("endDate") String endDate) {

    	TasksDto.SearchTasksResponse task = taskService.findTasksByDate(startDate, endDate);
        return ResponseEntity.ok(task);
    }

    @GetMapping("/all")
    public ResponseEntity<TasksDto.SearchTasksResponse> findAllTasks() {

    	TasksDto.SearchTasksResponse task = taskService.findAllTasks();
        return ResponseEntity.ok(task);
    }
}
