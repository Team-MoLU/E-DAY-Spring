package team.molu.edayserver.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.util.Set;

@Getter
@Node("Role")
public class Role {
    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private final RoleEnum type;

    @Relationship(type = "HAS_ROLE", direction = Relationship.Direction.INCOMING)
    private final Set<User> users;

    @Builder
    public Role(RoleEnum type, Set<User> users) {
        this.type = type;
        this.users = users;
    }
}
