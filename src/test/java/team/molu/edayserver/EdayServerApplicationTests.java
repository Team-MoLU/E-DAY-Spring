package team.molu.edayserver;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import team.molu.edayserver.repository.UserRepository;

@SpringBootTest
@RequiredArgsConstructor
class EdayServerApplicationTests {

    private final UserRepository userRepository;

    @Test
    void contextLoads() {
    }

}
