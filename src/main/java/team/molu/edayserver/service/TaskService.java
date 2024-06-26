package team.molu.edayserver.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Task;
import team.molu.edayserver.dto.TasksDto;
import team.molu.edayserver.exception.TaskNotFoundException;
import team.molu.edayserver.repository.TaskRepository;
import team.molu.edayserver.util.SecurityUtils;

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
     * @return 단순 할 일(Task) 리스트 DTO
     */
    public TasksDto.SearchTasksResponse findTaskByRoot() {
        String email = SecurityUtils.getAuthenticatedUserEmail();

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
    public TasksDto.TaskResponse createTask(TasksDto.TaskCreateRequest tasksDto) {
        String email = SecurityUtils.getAuthenticatedUserEmail();

        String taskId = UUID.randomUUID().toString();
        Map<String, Object> task = new HashMap<>();

        task.put("id", taskId);
        task.put("name", tasksDto.getName());
        task.put("memo", tasksDto.getMemo());
        task.put("startDate", tasksDto.getStartDate());
        task.put("endDate", tasksDto.getEndDate());
        task.put("priority", tasksDto.getPriority());
        task.put("check", false);

        if ("0".equals(tasksDto.getParentId())) {
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
        return taskRepository.findTaskById(tasksDto.getTaskId())
                .switchIfEmpty(Mono.error(new TaskNotFoundException("Task not found with id: " + tasksDto.getTaskId())))
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
     * @return 삭제된 서브트리의 root ID, 삭제된 총 노드 개수 DTO
     */
    public TasksDto.TaskDeleteResponse deleteTask(TasksDto.TaskDeleteRequest tasksDto) {
        String email = SecurityUtils.getAuthenticatedUserEmail();

        Integer deletedNodes;
        if (tasksDto.getCascade()) {
            deletedNodes = taskRepository.deleteTaskByIdWithCascade(email, tasksDto.getTaskId()).block();
        } else {
            deletedNodes = taskRepository.deleteTaskById(email, tasksDto.getTaskId()).block();
        }
        return TasksDto.TaskDeleteResponse.builder()
                .taskId(tasksDto.getTaskId())
                .deletedNodes(deletedNodes)
                .build();
    }

    /**
     * 특정 날짜에 포함되는 노드들을 조회합니다.
     *
     * @param startDate 조회활 할일 시작일자,
     * @param endDate 조회할 할일 종료일자
     * @return 단순 할 일(Task) 리스트 DTO
     */
    public TasksDto.SearchTasksResponse findTasksByDate(String startDate, String endDate) {
        String email = SecurityUtils.getAuthenticatedUserEmail();

        return taskRepository.findTasksByDate(email, startDate, endDate)
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
     * 사용자가 갖고 있는 모든 노드들을 조회합니다.
     *
     * @return 단순 할 일(Task) 리스트 DTO
     */
    public TasksDto.SearchTasksResponse findAllTasks() {
        String email = SecurityUtils.getAuthenticatedUserEmail();

        return taskRepository.findAllTasks(email)
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
     * 단순 할 일 정보를 영구적으로 삭제합니다.
     *
     * @param tasksDto 삭제할 단순 할일 id 및 cascade 여부 DTO
     * @return 삭제된 서브트리의 root ID, 삭제된 총 노드 개수 DTO
     */
    public TasksDto.TaskDeleteResponse dropTask(TasksDto.TaskDeleteRequest tasksDto) {
        Integer deletedNodes;
        if (tasksDto.getCascade()) {
            deletedNodes = taskRepository.dropTaskByIdWithCascade(tasksDto.getTaskId()).block();
        } else {
            deletedNodes = taskRepository.dropTaskById(tasksDto.getTaskId()).block();
        }

        return TasksDto.TaskDeleteResponse.builder()
                .taskId(tasksDto.getTaskId())
                .deletedNodes(deletedNodes)
                .build();
    }

    /**
     * 휴지통에 있는 모든 단순 할 일 정보를 영구적으로 삭제합니다.
     *
     * @return 삭제된 총 노드 개수 DTO
     */
    public TasksDto.EmptyTrashResponse dropAllTask() {
        String email = SecurityUtils.getAuthenticatedUserEmail();

        Integer deletedNodes = taskRepository.emptyTrash(email).block();

        return TasksDto.EmptyTrashResponse.builder()
                .deletedNodes(deletedNodes)
                .build();
    }

    /**
     * 휴지통에서 단순 할 일을 복구합니다.
     *
     * @param tasksDto 복구 요청 Task ID, 복구할 위치 Task ID DTO
     * @return 복구 요청에 대한 응답(복구 요청 Task ID, 복구할 위치 Task ID, 복구한 노드 개수) DTO
     */
    public TasksDto.TaskRestoreResponse restoreTask(TasksDto.TaskRestoreRequest tasksDto) {
        String email = SecurityUtils.getAuthenticatedUserEmail();

        Integer restoredNodes;
        if ("0".equals(tasksDto.getParentId())) {
            restoredNodes = taskRepository.restoreTaskById(email, "root", tasksDto.getTaskId()).block();

        } else {
            restoredNodes = taskRepository.restoreTaskById(email, tasksDto.getParentId(), tasksDto.getTaskId()).block();

        }
        return TasksDto.TaskRestoreResponse.builder()
                .taskId(tasksDto.getTaskId())
                .parentId(tasksDto.getParentId())
                .restoredNodes(restoredNodes)
                .build();
    }

    /**
     * 단순 할 일의 위치를 이동합니다.
     *
     * @param tasksDto 이동 요청 Task ID, 이동할 위치 Task ID DTO
     * @return 이동 요청에 대한 응답(이동 요청 Task ID, 이동할 위치 Task ID, 이동한 노드 개수) DTO
     */
    public TasksDto.TaskMoveResponse moveTask(TasksDto.TaskMoveRequest tasksDto) {
        String email = SecurityUtils.getAuthenticatedUserEmail();

        Integer restoredNodes;
        boolean isDescendant;

        isDescendant = taskRepository.isDescendant(tasksDto.getTaskId(), tasksDto.getParentId())
                .defaultIfEmpty(false)
                .onErrorReturn(false)
                .block();

        if(Boolean.TRUE.equals(isDescendant)) {
            restoredNodes = 0;
        } else {
            if ("0".equals(tasksDto.getParentId())) {
                restoredNodes = taskRepository.moveTaskById(email, "root", tasksDto.getTaskId()).block();
            } else {
                restoredNodes = taskRepository.moveTaskById(email, tasksDto.getParentId(), tasksDto.getTaskId()).block();
            }
        }
        return TasksDto.TaskMoveResponse.builder()
                .taskId(tasksDto.getTaskId())
                .parentId(tasksDto.getParentId())
                .movedNodes(restoredNodes)
                .build();
    }

    /**
     * 단순 할 일을 아카이빙합니다.
     *
     * @param tasksDto 아카이빙 요청 Task ID DTO
     * @return 아카이빙 요청에 대한 응답(아카이빙 요청 Task ID, 아카이빙한 노드 개수) DTO
     */
    public TasksDto.TaskArchiveResponse archiveTask(TasksDto.TaskArchiveRequest tasksDto) {
        String email = SecurityUtils.getAuthenticatedUserEmail();

        Integer archivedNodes = taskRepository.archiveTaskById(email, tasksDto.getTaskId()).block();

        return TasksDto.TaskArchiveResponse.builder()
                .taskId(tasksDto.getTaskId())
                .archivedNodes(archivedNodes)
                .build();
    }

    /**
     * 아카이빙된 단순 할 일을 아카이빙 해제합니다.
     *
     * @param tasksDto 아카이빙 해제 요청 DTO(Task ID, Parent ID: null일 경우 기존 위치로 아카이빙 해제)
     * @return 아카이빙 해제 요청에 대한 응답(아카이빙 해제 요청 Task ID, 아카이빙 해제된 위치 Task ID, 아키이빙 해제된 노드 개수) DTO
     */
    public TasksDto.TaskUnarchiveResponse unarchiveTask(TasksDto.TaskUnarchiveRequest tasksDto) {
        String email = SecurityUtils.getAuthenticatedUserEmail();

        if(tasksDto.getParentId() == null) {
            return taskRepository.unarchiveTaskByIdWithOriginalParent(email, tasksDto.getTaskId()).block();
        } else if("0".equals(tasksDto.getParentId())) {
            return taskRepository.unarchiveTaskByIdWithSpecificParent(email, "root", tasksDto.getTaskId()).block();
        } else {
            return taskRepository.unarchiveTaskByIdWithSpecificParent(email, tasksDto.getParentId(), tasksDto.getTaskId()).block();
        }
    }

    /**
     * 특정 문자열을 포함하는 Task를 검색합니다.
     *
     * @param tasksDto 문자열 검색 요청 DTO(text, type)
     * @return 해당 문자열로 검색 된 Task 노드들 반환
     */
    public TasksDto.SearchTasksResponse searchTasksByName(TasksDto.TaskSearchByNameRequest tasksDto) {
        String email = SecurityUtils.getAuthenticatedUserEmail();

        String type = "root";
        if (tasksDto.getType() != null && (tasksDto.getType().equals("root") || tasksDto.getType().equals("trash") || tasksDto.getType().equals("archive"))) {
            type = tasksDto.getType();
        }

        return taskRepository.searchTaskByName(email, type, tasksDto.getText())
                .collectList()
                .map(taskList -> {
                    List<TasksDto.TaskResponse> taskResponseList = new ArrayList<>();
                    for(Task task : taskList) {
                        TasksDto.TaskResponse taskResponse = TasksDto.TaskResponse.builder()
                                .taskId(task.getId())
                                .name(task.getName())
                                .memo(task.getMemo())
                                .startDate(task.getStartDate())
                                .endDate(task.getEndDate())
                                .priority(task.getPriority())
                                .check(task.getCheck())
                                .build();
                        taskResponseList.add(taskResponse);
                    }
                    return TasksDto.SearchTasksResponse.builder()
                            .taskList(taskResponseList)
                            .build();
                }).block();
    }
}
