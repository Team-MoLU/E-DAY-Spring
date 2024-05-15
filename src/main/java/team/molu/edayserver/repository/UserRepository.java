package team.molu.edayserver.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.User;

public interface UserRepository extends ReactiveNeo4jRepository<User, Long> {
    // email로 사용자 조회
    @Query("MATCH (u:User) WHERE u.email = $email RETURN u")
    Mono<User> findUserByEmail(String email);

    // 사용자와 관련된 Role, Oauth, Jwt 함께 조회
    @Query("MATCH (u:User)-[:HAS_ROLE]->(r:Role), (u)-[:HAS_OAUTH]->(o:Oauth), (u)-[:HAS_JWT]->(j:Jwt) WHERE u.email = $email RETURN u, r, o, j")
    Mono<User> findUserWithRelationshipsByEmail(String email);
}
