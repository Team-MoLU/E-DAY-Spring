package team.molu.edayserver.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Task;

public interface TaskRepository extends ReactiveNeo4jRepository<Task, String> {
    // email로 root 노드들 조회
    @Query("MATCH (u:User)-[CREATED_BY]->(r:Task), (r)-[BELONGS_TO]->(t:Task) WHERE u.email = $email RETURN t")
    Flux<Task> findRootTasks(String email);

    // id로 task 조회
    @Query("MATCH (t:Task {id: $taskId}) RETURN t")
    Mono<Task> findTaskById(String taskId);

    // id로 자식 노드들 조회
    @Query("MATCH (parentTask:Task {id: $taskId})-[:BELONGS_TO]->(childTask:Task) RETURN childTask")
    Flux<Task> findSubtaskById(String taskId);
}
