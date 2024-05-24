package team.molu.edayserver.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

@Getter
@Node("Oauth")
public class Oauth {
    @Id @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private final String oauthId;

    @Property
    private final OauthProviderEnum provider;

    @Relationship(type = "HAS_OAUTH", direction = Relationship.Direction.INCOMING)
    private User user;

    @Builder
    public Oauth(String oauthId, OauthProviderEnum provider, User user) {
        this.oauthId = oauthId;
        this.provider = provider;
        this.user = user;
    }
}
