package team.molu.edayserver.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Flux;
import team.molu.edayserver.domain.Role;
import team.molu.edayserver.domain.RoleEnum;

public interface RoleRepository extends ReactiveNeo4jRepository<Role, Long> {
    // Role Type과 일치하는 모든 유저를 조회
    @Query("MATCH (u:User)-[:HAS_ROLE]->(r:Role {type: $roleType}) RETURN u")
    Flux<Role> findAllUserByRole(RoleEnum roleType);
}
