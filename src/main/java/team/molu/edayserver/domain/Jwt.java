package team.molu.edayserver.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

import java.time.LocalDateTime;

@Getter
@Node("Jwt")
public class Jwt {
    @Property
    private final String refresh;

    @Property
    private final LocalDateTime ttl;

    @Builder
    public Jwt(String refresh, LocalDateTime ttl) {
        this.refresh = refresh;
        this.ttl = ttl;
    }
}
