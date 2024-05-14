package team.molu.edayserver.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;
import org.springframework.data.neo4j.core.schema.Relationship;

import java.util.List;

@Getter
@Node("Role")
public class Role {
    @Property
    private final RoleEnum type;

    @Relationship(type = "HAS_ROLE", direction = Relationship.Direction.INCOMING)
    private final List<User> users;

    @Builder
    public Role(RoleEnum type, List<User> users) {
        this.type = type;
        this.users = users;
    }
}
