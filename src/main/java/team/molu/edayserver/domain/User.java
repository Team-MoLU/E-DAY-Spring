package team.molu.edayserver.domain;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.neo4j.core.schema.*;

@Getter
@Node("User")
public class User {
    @Id @GeneratedValue
    private final Long id;

    @Property
    private final String email;

    @Property
    private final String profileImage;

    @Relationship(type = "HAS_ROLE", direction = Relationship.Direction.OUTGOING)
    private final Role userRole;

    @Relationship(type = "HAS_OAUTH", direction = Relationship.Direction.OUTGOING)
    private final Oauth userOauth;

    @Relationship(type = "HAS_JWT", direction = Relationship.Direction.OUTGOING)
    private final Jwt userJwt;

    @Builder
    public User(Long id, String email, String profileImage, Role userRole, Oauth userOauth, Jwt userJwt) {
        this.id = id;
        this.email = email;
        this.profileImage = profileImage;
        this.userRole = userRole;
        this.userOauth = userOauth;
        this.userJwt = userJwt;
    }
}
