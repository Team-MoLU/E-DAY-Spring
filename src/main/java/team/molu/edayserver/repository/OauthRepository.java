package team.molu.edayserver.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Oauth;
import team.molu.edayserver.domain.User;

public interface OauthRepository extends ReactiveNeo4jRepository<Oauth, Long> {
    @Query("MATCH (u:User {email = $email})-[r:HAS_OAUTH]->(o:Oauth) RETURN o")
    Mono<Oauth> findOauthByEmail(String email);
}
