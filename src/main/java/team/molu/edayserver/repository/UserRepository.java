package team.molu.edayserver.repository;

import org.springframework.data.neo4j.repository.ReactiveNeo4jRepository;
import org.springframework.data.neo4j.repository.query.Query;
import reactor.core.publisher.Mono;
import team.molu.edayserver.domain.Jwt;
import team.molu.edayserver.domain.OauthProviderEnum;
import team.molu.edayserver.domain.RoleEnum;
import team.molu.edayserver.domain.User;

import java.util.Date;

public interface UserRepository extends ReactiveNeo4jRepository<User, String> {
    // email로 사용자 조회
    @Query("MATCH (u:User) WHERE u.email = $email RETURN u")
    Mono<User> findUserByEmail(String email);

    // 사용자와 관련된 Role, Oauth, Jwt 함께 조회
    @Query("MATCH (u:User)-[:HAS_ROLE]->(r:Role), (u)-[:HAS_OAUTH]->(o:Oauth), (u)-[:HAS_JWT]->(j:Jwt) WHERE u.email = $email RETURN u, r, o, j")
    Mono<User> findUserWithRelationshipsByEmail(String email);

    @Query("MATCH (u:User {email: $email})-[*0..]-(connected) DETACH DELETE connected, u")
    Mono<Void> deleteUser(String email);

    @Query("CREATE (u:User {email: $email, profileImage: $profileImage})"
            + " WITH u"
            + " CREATE"
            + " (o:Oauth {oauthId: $oauthId, provider: $provider}),"
            + " (r:Role {type: $roleType}),"
            + " (j:Jwt {refresh: $refreshToken, ttl: datetime($ttl)}),"
            + " (u)-[:HAS_OAUTH]->(o),"
            + " (u)-[:HAS_ROLE]->(r),"
            + " (u)-[:HAS_JWT]->(j)"
            + " RETURN u")
    Mono<User> createUserAndAll(String email, String profileImage,
                                String oauthId, OauthProviderEnum provider,
                                RoleEnum roleType,
                                String refreshToken, Date ttl);

    @Query("MATCH (u:User {email: $email})-[:HAS_JWT]->(j:Jwt)"
            + " SET j.refresh = $newRefreshToken, j.ttl = datetime($newTtl)"
            + " RETURN j")
    Mono<Jwt> findUserAndUpdateJwt(String email, String newRefreshToken, Date newTtl);
}
