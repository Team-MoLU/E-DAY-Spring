package team.molu.edayserver.domain;


import lombok.Builder;
import lombok.Getter;

@Getter
public class CommonTestDomain {
    private final Long id;

    private final String email;

    private final String name;

    
    @Builder
    public CommonTestDomain(Long id, String email, String name) {
        this.id = id;
        this.email = email;
        this.name = name;
    }
    
    @Builder
    public CommonTestDomain() {
        this.id = null;
        this.email = "";
        this.name = "";
    }
}
