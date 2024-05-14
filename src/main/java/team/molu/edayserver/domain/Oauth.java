package team.molu.edayserver.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.Node;
import org.springframework.data.neo4j.core.schema.Property;

@Getter
@Node("Oauth")
public class Oauth {
    @Property
    private final String oauthId;

    @Property
    private final OauthProviderEnum provider;

    @Builder
    public Oauth(String oauthId, OauthProviderEnum provider) {
        this.oauthId = oauthId;
        this.provider = provider;
    }
}
