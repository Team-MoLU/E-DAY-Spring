package team.molu.edayserver.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.time.LocalDateTime;

@Getter
@Node("Jwt")
public class Jwt {
    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private final String refresh;

    @Property
    private final LocalDateTime ttl;

    @Relationship(type = "HAS_JWT", direction = Relationship.Direction.INCOMING)
    private User user;

    @Builder
    public Jwt(String refresh, LocalDateTime ttl, User user) {
        this.refresh = refresh;
        this.ttl = ttl;
        this.user = user;
    }
}
