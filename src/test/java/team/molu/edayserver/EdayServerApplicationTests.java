package team.molu.edayserver;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import team.molu.edayserver.repository.TaskRepository;
import team.molu.edayserver.repository.UserRepository;

@SpringBootTest
class EdayServerApplicationTests {

    @Autowired
    private TaskRepository taskRepository;

    @Test
    void contextLoads() {
    }

}
