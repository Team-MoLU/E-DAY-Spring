package team.molu.edayserver.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Jwt;

public interface JwtRepository extends ReactiveNeo4jRepository<Jwt, String> {
    @Query("MATCH (u:User {email = $email})-[r:HAS_JWT]->(j:Jwt) RETURN j")
    Mono<Jwt> findJwtByEmail(String email);
}
