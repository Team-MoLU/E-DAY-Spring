package team.molu.edayserver.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Task;
import team.molu.edayserver.dto.TasksDto;

import java.util.Map;

public interface TaskRepository extends ReactiveNeo4jRepository<Task, String> {
    // email로 root 노드들 조회
    @Query("MATCH (u:User)-[CREATED_BY]->(r:Task {id: \"root\"}), (r)-[BELONGS_TO]->(t:Task) WHERE u.email = $email RETURN t")
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
            "RETURN [i in indices | {name: parents[i].name, taskId: parents[i].id, order: i+1}] + [{name: t.name, taskId: t.id, order: 0}] AS routes " +
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
            "WITH t, r " +
            "MATCH (t)-[*0..]->(sub:Task) " +
            "SET t.deleteTime = datetime(), sub.deleteTime = datetime() " +
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

    // 노드 서브트리 Cascade 영구삭제
    @Query("MATCH (t:Task {id: $taskId}) " +
            "OPTIONAL MATCH (t)-[r:BELONGS_TO*]->(c:Task) " +
            "WITH t, collect(DISTINCT c) as cs " +
            "FOREACH (n IN cs | DETACH DELETE n) " +
            "DETACH DELETE t " +
            "RETURN SIZE(cs) + CASE WHEN t IS NOT NULL THEN 1 ELSE 0 END")
    Mono<Integer> dropTaskByIdWithCascade(String taskId);

    // 단일 노드 영구 삭제
    @Query("MATCH (t:Task {id: $taskId}) " +
            "OPTIONAL MATCH (p:Task)-[pr:BELONGS_TO]->(t) " +
            "OPTIONAL MATCH (t)-[cr:BELONGS_TO]->(c:Task) " +
            "WITH t, p, collect(c) as cs " +
            "FOREACH (c IN cs | CREATE (p)-[:BELONGS_TO]->(c)) " +
            "DETACH DELETE t " +
            "RETURN toInteger(CASE WHEN t IS NOT NULL THEN 1 ELSE 0 END)")
    Mono<Integer> dropTaskById(String taskId);

    // 휴지통 전체 영구 삭제
    @Query("MATCH (u:User {email: $email})-[:CREATED_BY]->(d:Task {id: \"trash\"}) " +
            "OPTIONAL MATCH (d)-[r:BELONGS_TO*]->(c:Task) " +
            "WITH d, collect(DISTINCT c) as cs " +
            "FOREACH (n IN cs | DETACH DELETE n) " +
            "RETURN SIZE(cs)")
    Mono<Integer> emptyTrash(String email);

    // 단순 할 일 노드 복구
    @Query("MATCH (u:User {email: $email})-[:CREATED_BY]->(d:Task {id: \"trash\"}) " +
            "MATCH (d)-[:BELONGS_TO*]->(t:Task {id: $taskId}) " +
            "WITH t " +
            "MATCH (:Task)-[r:BELONGS_TO]->(t) " +
            "MATCH (p:Task {id: $parentId}) " +
            "WITH t, p, r " +
            "OPTIONAL MATCH (t)-[:BELONGS_TO*]->(c:Task) " +
            "WITH t, p, r, collect(c) AS cs " +
            "FOREACH (c IN cs | REMOVE c.deleteTime) " +
            "REMOVE t.deleteTime " +
            "DELETE r " +
            "CREATE (p)-[:BELONGS_TO]->(t) " +
            "RETURN toInteger(CASE WHEN t IS NOT NULL THEN size(cs) + 1 ELSE 0 END) AS movedCount")
    Mono<Integer> restoreTaskById(String email, String parentId, String taskId);

    // 단순 할 일 노드 이동
    @Query("MATCH (u:User {email: $email})-[:CREATED_BY]->(m:Task {id: \"root\"}) " +
            "MATCH (m)-[:BELONGS_TO*]->(t:Task {id: $taskId}) " +
            "WITH t, m " +
            "MATCH (:Task)-[r:BELONGS_TO]->(t) " +
            "WITH t, m, r " +
            "MATCH (m)-[:BELONGS_TO*0..]->(p:Task {id: $parentId}) " +
            "WITH t, p, r " +
            "OPTIONAL MATCH (t)-[:BELONGS_TO*]->(c:Task) " +
            "WITH t, p, r, collect(c) AS cs " +
            "DELETE r " +
            "CREATE (p)-[:BELONGS_TO]->(t) " +
            "RETURN toInteger(CASE WHEN t IS NOT NULL THEN size(cs) + 1 ELSE 0 END) AS movedCount")
    Mono<Integer> moveTaskById(String email, String parentId, String taskId);

    // 단순 할 일 아카이빙
    @Query("MATCH (u:User {email: $email})-[:CREATED_BY]->(root:Task {id: \"root\"}) " +
            "MATCH (root)-[:BELONGS_TO*]->(t:Task {id: $taskId}) " +
            "WITH t " +
            "MATCH (p:Task)-[r:BELONGS_TO]->(t) " +
            "WITH p, t, r " +
            "MATCH (u:User {email: $email})-[:CREATED_BY]->(a:Task {id: \"archive\"}) " +
            "CREATE (a)-[:BELONGS_TO]->(t) " +
            "WITH p, t, r " +
            "MATCH (t)-[*0..]->(sub:Task) " +
            "SET t.originalParentId = p.id, sub.originalParentId = p.id " +
            "DELETE r " +
            "RETURN COUNT(DISTINCT sub)")
    Mono<Integer> archiveTaskById(String email, String taskId);

    // 단순 할 일 아카이빙 해제 - 기존 부모 ID 사용
    @Query("MATCH (u:User {email: $email})-[:CREATED_BY]->(a:Task {id: \"archive\"}) " +
            "MATCH (a)-[:BELONGS_TO*]->(t:Task {id: $taskId}) " +
            "WITH t " +
            "MATCH (:Task)-[r:BELONGS_TO]->(t) " +
            "WITH t, r " +
            "MATCH (u:User {email: $email})-[:CREATED_BY]->(root:Task {id: \"root\"}) " +
            "OPTIONAL MATCH (root)-[:BELONGS_TO*]->(p:Task {id: t.originalParentId}) " +
            "WITH t, r, COALESCE(p, root) AS p " +
            "WITH t, p, r " +
            "OPTIONAL MATCH (t)-[:BELONGS_TO*]->(c:Task) " +
            "WITH t, p, r, collect(c) AS cs " +
            "FOREACH (c IN cs | REMOVE c.originalParentId) " +
            "REMOVE t.originalParentId " +
            "DELETE r " +
            "CREATE (p)-[:BELONGS_TO]->(t) " +
            "RETURN toInteger(CASE WHEN t IS NOT NULL THEN size(cs) + 1 ELSE 0 END) AS archivedNodes, p.id AS parentId, t.id AS taskId")
    Mono<TasksDto.TaskUnarchiveResponse> unarchiveTaskByIdWithOriginalParent(String email, String taskId);

    // 단순 할 일 아카이빙 해제 - 입력한 부모 ID 사용
    @Query("MATCH (u:User {email: $email})-[:CREATED_BY]->(a:Task {id: \"archive\"}) " +
            "MATCH (a)-[:BELONGS_TO*]->(t:Task {id: $taskId}) " +
            "WITH t " +
            "MATCH (:Task)-[r:BELONGS_TO]->(t) " +
            "WITH t, r " +
            "MATCH (u:User {email: $email})-[:CREATED_BY]->(root:Task {id: \"root\"}) " +
            "OPTIONAL MATCH (root)-[:BELONGS_TO*]->(p:Task {id: $parentId}) " +
            "WITH t, r, COALESCE(p, root) AS p " +
            "WITH t, p, r " +
            "OPTIONAL MATCH (t)-[:BELONGS_TO*]->(c:Task) " +
            "WITH t, p, r, collect(c) AS cs " +
            "FOREACH (c IN cs | REMOVE c.originalParentId) " +
            "REMOVE t.originalParentId " +
            "DELETE r " +
            "CREATE (p)-[:BELONGS_TO]->(t) " +
            "RETURN toInteger(CASE WHEN t IS NOT NULL THEN size(cs) + 1 ELSE 0 END) AS archivedNodes, p.id AS parentId, t.id AS taskId")
    Mono<TasksDto.TaskUnarchiveResponse> unarchiveTaskByIdWithSpecificParent(String email, String parentId, String taskId);
}
