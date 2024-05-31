package team.molu.edayserver.service;

import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Task;
import team.molu.edayserver.dto.TasksDto;
import team.molu.edayserver.exception.TaskNotFoundException;
import team.molu.edayserver.repository.TaskRepository;

import java.util.ArrayList;
import java.util.List;

@Service
public class TaskService {
    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
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
                                .parentId(task.getParentTask() != null ? task.getParentTask().getId() : null)
                                .taskId(task.getId())
                                .name(task.getName())
                                .memo(task.getMemo() != null ? task.getMemo() : "")
                                .startDate(task.getStartDate())
                                .endDate(task.getEndDate())
                                .priority(task.getPriority())
                                .check(task.isCheck())
                                .archive(task.isArchive())
                                .build();
                        taskResponseList.add(taskResponse);
                    }
                    return TasksDto.SearchTasksResponse.builder()
                            .taskList(taskResponseList)
                            .build();
                }).block();
    }

    public TasksDto.TaskResponse findTaskById(String taskId) {
        return taskRepository.findTaskById(taskId).switchIfEmpty(Mono.error(new TaskNotFoundException("Task not found with id: " + taskId)))
                .map(task ->
                        TasksDto.TaskResponse.builder()
                                .parentId(task.getParentTask() != null ? task.getParentTask().getId() : null)
                                .taskId(task.getId())
                                .name(task.getName())
                                .memo(task.getMemo() != null ? task.getMemo() : "")
                                .startDate(task.getStartDate())
                                .endDate(task.getEndDate())
                                .priority(task.getPriority())
                                .check(task.isCheck())
                                .archive(task.isArchive())
                                .build()
                )
                .block();
    }

    public TasksDto.SearchTasksResponse findSubtaskById(String taskId) {
        return taskRepository.findSubtaskById(taskId)
                .collectList()
                .map(taskList -> {
                    List<TasksDto.TaskResponse> taskResponseList = new ArrayList<>();
                    for (Task task : taskList) {
                        TasksDto.TaskResponse taskResponse = TasksDto.TaskResponse.builder()
                                .parentId(task.getParentTask() != null ? task.getParentTask().getId() : null)
                                .taskId(task.getId())
                                .name(task.getName())
                                .memo(task.getMemo() != null ? task.getMemo() : "")
                                .startDate(task.getStartDate())
                                .endDate(task.getEndDate())
                                .priority(task.getPriority() != null ? task.getPriority() : 0)
                                .check(task.isCheck())
                                .archive(task.isArchive())
                                .build();
                        taskResponseList.add(taskResponse);
                    }
                    return TasksDto.SearchTasksResponse.builder()
                            .taskList(taskResponseList)
                            .build();
                }).block();
    }

    /**
     * 단순 할일 정보를 저장합니다.
     *
     * @param tasksDto 저장할 단순 할일 정보 및 부모 단순 할일 정보 Dto
     */
//    public void createTask(String email, TasksDto.TaskCreateRequest tasksDto) {
//        if(Objects.equals(tasksDto.getParentId(), "0")) {
//
//        } else {
//
//        }
//    }
}
