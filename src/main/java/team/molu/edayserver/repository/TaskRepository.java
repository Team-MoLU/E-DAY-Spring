package team.molu.edayserver.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Task;
import team.molu.edayserver.dto.TasksDto;

public interface TaskRepository extends ReactiveNeo4jRepository<Task, String> {
    // email로 root 노드들 조회
    @Query("MATCH (u:User)-[CREATED_BY]->(r:Task), (r)-[BELONGS_TO]->(t:Task) WHERE u.email = $email RETURN t")
    Flux<Task> findRootTasks(String email);

    // id로 task 조회
    @Query("MATCH (t:Task {id: $taskId}) RETURN t")
    Mono<Task> findTaskById(String taskId);

    // id로 자식 노드들 조회
    @Query("MATCH (t:Task {id: $taskId})-[:BELONGS_TO]->(r:Task) RETURN r")
    Flux<Task> findSubtaskById(String taskId);

    // id로 경로 조회
    @Query("MATCH path = (t:Task {id: $taskId})<-[:BELONGS_TO*0..]-(p:Task) " +
            "WHERE NOT (p)<-[:CREATED_BY]-(:User) " +
            "WITH t, nodes(path)[1..] AS parents " +
            "WITH t, parents, range(0, size(parents)-1) AS indices " +
            "RETURN [i in indices | {name: parents[i].name, id: parents[i].id, order: i+1}] + [{name: t.name, id: t.id, order: 0}] AS routes " +
            "ORDER BY size(routes) DESC " +
            "LIMIT 1")
    Flux<TasksDto.TaskRouteDto> findRoutesById(String taskId);
}
