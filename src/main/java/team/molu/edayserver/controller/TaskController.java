package team.molu.edayserver.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.ErrorResponse;
import org.springframework.web.bind.annotation.*;
import team.molu.edayserver.domain.Task;
import team.molu.edayserver.dto.TasksDto;
import team.molu.edayserver.service.TaskService;

@RestController()
@RequestMapping("api/v1/tasks")
public class TaskController {
    private final TaskService taskService;

    public TaskController(TaskService taskService) { this.taskService = taskService; }

    /**
     * taskId가 유효한 지 체크 하는 private 함수
     *
     * @param taskId 체크할 ID, String
     * @return 유효한 지 여부
     */
    private Boolean isValidTaskId(String taskId) {
        String trimmedTaskId = taskId.trim().toLowerCase();
        return !trimmedTaskId.equals("root") && !trimmedTaskId.equals("trash") && !trimmedTaskId.equals("archive");
    }

    @GetMapping("/roots")
    public ResponseEntity<TasksDto.SearchTasksResponse> findRootTasks() {
        TasksDto.SearchTasksResponse tasks = taskService.findTaskByRoot();
        return ResponseEntity.ok(tasks);
    }

    @GetMapping("/{taskId}")
    public ResponseEntity<?> findTaskDetail(@PathVariable String taskId) {
        if(!isValidTaskId(taskId)) {
            return ResponseEntity.badRequest().body(taskId + " is not allowed.");
        } else {
            TasksDto.TaskResponse tasks = taskService.findTaskById(taskId);
            return ResponseEntity.ok(tasks);
        }
    }

    @GetMapping("/{taskId}/subtasks")
    public ResponseEntity<?> findChildTasks(@PathVariable String taskId) {
        if(!isValidTaskId(taskId)) {
            return ResponseEntity.badRequest().body(taskId + " is not allowed.");
        } else {
            TasksDto.SearchTasksResponse tasks = taskService.findSubtaskById(taskId);
            return ResponseEntity.ok(tasks);
        }
    }

    @GetMapping("/{taskId}/routes")
    public ResponseEntity<?> getTaskRoutesById(@PathVariable String taskId) {
        if(!isValidTaskId(taskId)) {
            return ResponseEntity.badRequest().body(taskId + " is not allowed.");
        } else {
            TasksDto.TaskRouteResponse routes = taskService.getTaskRoutes(taskId);
            return ResponseEntity.ok(routes);
        }
    }

    @PostMapping
    public ResponseEntity<?> createTask(@RequestBody TasksDto.TaskCreateRequest taskDto) {
        if(!isValidTaskId(taskDto.getParentId())) {
            return ResponseEntity.badRequest().body(taskDto.getParentId() + " is not allowed.");
        } else {
            TasksDto.TaskResponse task = taskService.createTask(taskDto);
            return ResponseEntity.ok(task);
        }
    }

    @PatchMapping
    public ResponseEntity<?> updateTask(@RequestBody TasksDto.TaskUpdateRequest taskDto) {
        if(!isValidTaskId(taskDto.getTaskId())) {
            return ResponseEntity.badRequest().body(taskDto.getTaskId() + " is not allowed.");
        } else {
            TasksDto.TaskResponse task = taskService.updateTask(taskDto);
            return ResponseEntity.ok(task);
        }
    }

    @PostMapping("/delete")
    public ResponseEntity<?> deleteTask(@RequestBody TasksDto.TaskDeleteRequest taskDto) {
        if(!isValidTaskId(taskDto.getTaskId())) {
            return ResponseEntity.badRequest().body(taskDto.getTaskId() + " is not allowed.");
        } else {
            TasksDto.TaskDeleteResponse taskDeleteResponse = taskService.deleteTask(taskDto);
            return ResponseEntity.ok(taskDeleteResponse);
        }
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

    @PostMapping("/restore")
    public ResponseEntity<?> restoreTask(@RequestBody TasksDto.TaskRestoreRequest taskDto) {
        if(!isValidTaskId(taskDto.getTaskId())) {
            return ResponseEntity.badRequest().body(taskDto.getTaskId() + " is not allowed.");
        } else if (!isValidTaskId(taskDto.getParentId())) {
            return ResponseEntity.badRequest().body(taskDto.getParentId() + " is not allowed.");
        } else {
            TasksDto.TaskRestoreResponse taskRestoreResponse = taskService.restoreTask(taskDto);
            return ResponseEntity.ok(taskRestoreResponse);
        }
    }

    @DeleteMapping("/drop")
    public ResponseEntity<?> dropTask(@RequestBody TasksDto.TaskDeleteRequest taskDto) {
        if(!isValidTaskId(taskDto.getTaskId())) {
            return ResponseEntity.badRequest().body(taskDto.getTaskId() + " is not allowed.");
        } else {
            TasksDto.TaskDeleteResponse taskDropResponse = taskService.dropTask(taskDto);
            return ResponseEntity.ok(taskDropResponse);
        }
    }

    @DeleteMapping("/drop/all")
    public ResponseEntity<TasksDto.EmptyTrashResponse> dropAllTasks() {
        TasksDto.EmptyTrashResponse emptyTrashResponse = taskService.dropAllTask();
        return  ResponseEntity.ok(emptyTrashResponse);
    }

    @PostMapping("/move")
    public ResponseEntity<?> moveTask(@RequestBody TasksDto.TaskMoveRequest taskDto) {
        if(!isValidTaskId(taskDto.getTaskId())) {
            return ResponseEntity.badRequest().body(taskDto.getTaskId() + " is not allowed.");
        } else if (!isValidTaskId(taskDto.getParentId())) {
            return ResponseEntity.badRequest().body(taskDto.getParentId() + " is not allowed.");
        } else {
            TasksDto.TaskMoveResponse taskMoveResponse = taskService.moveTask(taskDto);
            return ResponseEntity.ok(taskMoveResponse);
        }
    }

    @PostMapping("/archive")
    public ResponseEntity<?> archiveTask(@RequestBody TasksDto.TaskArchiveRequest taskDto) {
        if(!isValidTaskId(taskDto.getTaskId())) {
            return ResponseEntity.badRequest().body(taskDto.getTaskId() + " is not allowed.");
        } else {
            TasksDto.TaskArchiveResponse taskArchiveResponse = taskService.archiveTask(taskDto);
            return ResponseEntity.ok(taskArchiveResponse);
        }
    }

    @PostMapping("/unarchive")
    public ResponseEntity<?> unarchiveTask(@RequestBody TasksDto.TaskUnarchiveRequest taskDto) {
        if(!isValidTaskId(taskDto.getTaskId())) {
            return ResponseEntity.badRequest().body(taskDto.getTaskId() + " is not allowed.");
        } else if (!isValidTaskId(taskDto.getParentId())) {
            return ResponseEntity.badRequest().body(taskDto.getParentId() + " is not allowed.");
        } else {
            TasksDto.TaskUnarchiveResponse taskUnarchiveResponse = taskService.unarchiveTask(taskDto);
            return ResponseEntity.ok(taskUnarchiveResponse);
        }
    }

    @PostMapping("/search")
    public ResponseEntity<TasksDto.SearchTasksResponse> searchTaskByName(@RequestBody TasksDto.TaskSearchByNameRequest taskDto) {
        TasksDto.SearchTasksResponse searchTasksResponse = taskService.searchTasksByName(taskDto);
        return ResponseEntity.ok(searchTasksResponse);
    }
}
