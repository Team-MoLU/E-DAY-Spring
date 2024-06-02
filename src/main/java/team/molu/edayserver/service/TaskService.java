package team.molu.edayserver.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Task;
import team.molu.edayserver.dto.TasksDto;
import team.molu.edayserver.exception.TaskNotFoundException;
import team.molu.edayserver.repository.TaskRepository;

import java.util.*;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    private static TasksDto.TaskResponse convertToTaskResponse(Task newTask) {
        return TasksDto.TaskResponse.builder()
                .taskId(newTask.getId())
                .name(newTask.getName())
                .memo(newTask.getMemo() != null ? newTask.getMemo() : "")
                .startDate(newTask.getStartDate())
                .endDate(newTask.getEndDate())
                .priority(newTask.getPriority())
                .check(newTask.isCheck())
                .build();
    }

    /**
     * 사용자의 단순 할 일 루트 노드들을 조회합니다.
     *
     * @param email 조회할 사용자 email
     * @return 단순 할 일(Task) 리스트 DTO
     */
    public TasksDto.SearchTasksResponse findTaskByRoot(String email) {
        return taskRepository.findRootTasks(email)
                .collectList()
                .map(taskList -> {
                    List<TasksDto.TaskResponse> taskResponseList = new ArrayList<>();
                    for (Task task : taskList) {
                        TasksDto.TaskResponse taskResponse = TasksDto.TaskResponse.builder()
                                .taskId(task.getId())
                                .name(task.getName())
                                .memo(task.getMemo() != null ? task.getMemo() : "")
                                .startDate(task.getStartDate())
                                .endDate(task.getEndDate())
                                .priority(task.getPriority())
                                .check(task.isCheck())
                                .build();
                        taskResponseList.add(taskResponse);
                    }
                    return TasksDto.SearchTasksResponse.builder()
                            .taskList(taskResponseList)
                            .build();
                }).block();
    }

    /**
     * 사용자의 단순 할 일을 조회합니다.
     *
     * @param taskId 조회 할 단순 할 일 노드의 ID
     * @return 단순 할 일(Task) DTO
     */
    public TasksDto.TaskResponse findTaskById(String taskId) {
        return taskRepository.findTaskById(taskId).switchIfEmpty(Mono.error(new TaskNotFoundException("Task not found with id: " + taskId)))
                .map(TaskService::convertToTaskResponse)
                .block();
    }

    /**
     * 특정 단순 할 일의 하위 할 일 노드들을 조회합니다.
     *
     * @param taskId 조회 할 단순 할 일 노드의 ID
     * @return 단순 할 일(Task) 리스트 DTO
     */
    public TasksDto.SearchTasksResponse findSubtaskById(String taskId) {
        return taskRepository.findSubtaskById(taskId)
                .collectList()
                .map(taskList -> {
                    List<TasksDto.TaskResponse> taskResponseList = new ArrayList<>();
                    for (Task task : taskList) {
                        TasksDto.TaskResponse taskResponse = TasksDto.TaskResponse.builder()
                                .taskId(task.getId())
                                .name(task.getName())
                                .memo(task.getMemo() != null ? task.getMemo() : "")
                                .startDate(task.getStartDate())
                                .endDate(task.getEndDate())
                                .priority(task.getPriority() != null ? task.getPriority() : 0)
                                .check(task.isCheck())
                                .build();
                        taskResponseList.add(taskResponse);
                    }
                    return TasksDto.SearchTasksResponse.builder()
                            .taskList(taskResponseList)
                            .build();
                }).block();
    }

    /**
     * 특정 단순 할 일의 경로를 조회합니다.
     *
     * @param taskId 조회 할 단순 할 일 노드의 ID
     * @return TaskRouteDTO(id, name, order) 리스트 (= TaskRouteResponse)
     */
    public TasksDto.TaskRouteResponse getTaskRoutes(String taskId) {
        return taskRepository.findRoutesById(taskId)
                .collectList()
                .map(routes -> TasksDto.TaskRouteResponse.builder()
                        .routes(routes)
                        .build())
                .block();
    }

    /**
     * 단순 할 일 정보를 저장합니다.
     *
     * @param tasksDto 저장할 단순 할일 정보 및 부모 단순 할일 정보 DTO
     * @return 단순 할 일(Task) DTO
     */
    public TasksDto.TaskResponse createTask(String email, TasksDto.TaskCreateRequest tasksDto) {
        String taskId = UUID.randomUUID().toString();
        Map<String, Object> task = new HashMap<>();

        task.put("id", taskId);
        task.put("name", tasksDto.getName());
        task.put("memo", tasksDto.getMemo());
        task.put("startDate", tasksDto.getStartDate());
        task.put("endDate", tasksDto.getEndDate());
        task.put("priority", tasksDto.getPriority());
        task.put("check", false);

        if("0".equals(tasksDto.getParentId())) {
            task.put("email", email);

            return taskRepository.createTaskWithRootParent(task)
                    .map(TaskService::convertToTaskResponse)
                    .block();
        } else {
            task.put("parentId", tasksDto.getParentId());

            return taskRepository.createTaskWithParent(task)
                    .map(TaskService::convertToTaskResponse
                    )
                    .block();
        }
    }

    /**
     * 단순 할 일 정보를 수정합니다.
     *
     * @param tasksDto 수정할 단순 할일 정보 및 부모 단순 할일 정보 DTO
     * @return 단순 할 일(Task) DTO
     */
    public TasksDto.TaskResponse updateTask(TasksDto.TaskUpdateRequest tasksDto) {
        return taskRepository.findTaskById(tasksDto.getId())
                .switchIfEmpty(Mono.error(new TaskNotFoundException("Task not found with id: " + tasksDto.getId())))
                .flatMap(task -> {
                    Map<String, Object> updatedTask = new HashMap<>();

                    updatedTask.put("id", task.getId());
                    updatedTask.put("name", tasksDto.getName());
                    updatedTask.put("memo", tasksDto.getMemo() != null ? tasksDto.getMemo() : "");
                    updatedTask.put("startDate", tasksDto.getStartDate());
                    updatedTask.put("endDate", tasksDto.getEndDate());
                    updatedTask.put("priority", tasksDto.getPriority() != null ? tasksDto.getPriority() : 0);
                    updatedTask.put("check", tasksDto.getCheck());

                    return taskRepository.updateTask(updatedTask);
                })
                .map(TaskService::convertToTaskResponse).block();
    }

    /**
     * 단순 할 일 정보를 삭제합니다. (휴지통으로 이동)
     *
     * @param tasksDto 삭제할 단순 할일 id 및 cascade 여부 DTO
     */
    public TasksDto.TaskDeleteResponse deleteTask(TasksDto.TaskDeleteRequest tasksDto) {
        Integer deletedNodes;
        if(tasksDto.getCascade()) {
            deletedNodes = taskRepository.deleteTaskByIdWithCascade(tasksDto.getId()).block();
        } else {
            deletedNodes = taskRepository.deleteTaskById(tasksDto.getId()).block();
        }
        return TasksDto.TaskDeleteResponse.builder()
                .id(tasksDto.getId())
                .deletedNodes(deletedNodes)
                .build();
    }
}
