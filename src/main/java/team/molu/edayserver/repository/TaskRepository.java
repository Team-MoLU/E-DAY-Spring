package team.molu.edayserver.repository;

import java.util.Map;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import org.springframework.data.repository.query.Param;

import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Task;
import team.molu.edayserver.dto.TasksDto;

public interface TaskRepository extends ReactiveNeo4jRepository<Task, String> {
    // email로 root 노드들 조회
    @Query("MATCH (u:User)-[CREATED_BY]->(r:Task {id: \"root\"}), (r)-[BELONGS_TO]->(t:Task) WHERE u.email = $email RETURN t")
    Flux<Task> findRootTasks(@Param("email") String email);

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

    // 루트에 노드 추가
    @Query("MATCH (u:User {email: $task.email}) " +
            "MATCH (u)-[:CREATED_BY]->(r:Task {id: \"root\"}) " +
            "CREATE (t:Task { " +
            "id: $task.id, " +
            "name: $task.name, " +
            "memo: $task.memo, " +
            "startDate: $task.startDate, " +
            "endDate: $task.endDate, " +
            "priority: $task.priority, " +
            "check: $task.check " +
            "}) " +
            "CREATE (r)-[:BELONGS_TO]->(t) " +
            "RETURN t")
    Mono<Task> createTaskWithRootParent(Map<String, Object> task);

    // 특정 노드에 하위 노드 추가
    @Query("MATCH (r:Task {id: $task.parentId}) " +
            "CREATE (t:Task { " +
            "id: $task.id, " +
            "name: $task.name, " +
            "memo: $task.memo, " +
            "startDate: $task.startDate, " +
            "endDate: $task.endDate, " +
            "priority: $task.priority, " +
            "check: $task.check " +
            "}) " +
            "CREATE (r)-[:BELONGS_TO]->(t) " +
            "RETURN t")
    Mono<Task> createTaskWithParent(Map<String, Object> task);

    // 특정 노드 수정
    @Query("MATCH (t:Task {id: $task.id})" +
            "SET t.name = $task.name, t.memo = $task.memo, t.startDate = $task.startDate, t.endDate = $task.endDate, " +
            "    t.priority = $task.priority, t.check = $task.check " +
            "RETURN t")
    Mono<Task> updateTask(Map<String, Object> task);

    // 노드 서브 트리 Cascade 삭제
    @Query("MATCH (p:Task)-[r:BELONGS_TO]->(t:Task {id: $taskId}) " +
            "WITH t, r " +
            "MATCH (u:User {email: $email})-[:CREATED_BY]->(d:Task {id: \"trash\"}) " +
            "CREATE (d)-[:BELONGS_TO]->(t) " +
            "SET t.deleteTime = datetime() " +
            "WITH t, r " +
            "MATCH (t)-[*0..]->(sub:Task) " +
            "DELETE r " +
            "RETURN COUNT(DISTINCT sub)")
    Mono<Integer> deleteTaskByIdWithCascade(String email, String taskId);

    // 단일 노드 삭제
    @Query("MATCH (p:Task)-[pr:BELONGS_TO]->(t:Task {id: $taskId}) " +
            "OPTIONAL MATCH (t)-[cr:BELONGS_TO]->(c:Task) " +
            "WITH t, p, pr, collect(c) as cs, collect(cr) as crs " +
            "FOREACH (c IN cs | CREATE (p)-[:BELONGS_TO]->(c)) " +
            "WITH t, p, pr, crs " +
            "FOREACH (cr IN crs | DELETE cr) " +
            "WITH t, p, pr " +
            "MATCH (u:User {email: $email})-[:CREATED_BY]->(d:Task {id: \"trash\"}) " +
            "CREATE (d)-[:BELONGS_TO]->(t) " +
            "SET t.deleteTime = datetime() " +
            "DELETE pr " +
            "RETURN COUNT(t)")
    Mono<Integer> deleteTaskById(String email, String taskId);
    
    // 특정 날짜에 포함되는 노드(할 일)들 조회
    @Query("MATCH (u:User {email: $email})-[CREATED_BY]->(r:Task {id: \"root\"}), (r)-[BELONGS_TO*]->(t:Task) " +
    		"WHERE ((date(t.startDate) >= date($startDate) AND date(t.startDate) <= date($endDate)) " +
    		"OR (date(t.endDate) >= date($startDate) AND DATE(t.endDate) <= DATE($endDate))) RETURN t")
    Flux<Task> findTasksByDate(@Param("email")String email, @Param("startDate")String startDate, @Param("endDate")String endDate);
    
    // 사용자가 갖고 있는 모든 노드(할 일)들을 조회
    @Query("MATCH (u:User {email: $email})-[CREATED_BY]->(r:Task {id: \"root\"}), (r)-[BELONGS_TO*]->(t:Task) RETURN t")
    Flux<Task> findAllTasks(@Param("email")String email);
}
