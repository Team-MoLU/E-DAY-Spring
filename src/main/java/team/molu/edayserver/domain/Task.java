package team.molu.edayserver.domain;

import lombok.Builder;
import lombok.Getter;
import org.springframework.data.neo4j.core.schema.*;
import org.springframework.data.neo4j.core.support.UUIDStringGenerator;

import java.time.LocalDateTime;
import java.util.Set;

@Getter
@Node("Task")
public class Task {
    @Id
    @GeneratedValue(UUIDStringGenerator.class)
    private String id;

    @Property
    private String name;

    @Property
    private String memo;

    @Property
    private LocalDateTime startDate;

    @Property
    private LocalDateTime endDate;

    @Property
    private Integer priority;

    @Property
    private Boolean check;

    @Property
    private Boolean archive;

    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.INCOMING)
    private User createdBy;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    private Task parentTask;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.INCOMING)
    private Set<Task> childTasks;

    @Builder
    public Task(String id, String name, String memo, LocalDateTime startDate, LocalDateTime endDate, Integer priority, Boolean check, Boolean archive) {
        this.id = id;
        this.name = name;
        this.memo = memo;
        this.startDate = startDate;
        this.endDate = endDate;
        this.priority = priority;
        this.check = check;
        this.archive = archive;
    }

    // getter 메서드 추가
    public boolean isCheck() {
        return check != null && check;
    }

    public boolean isArchive() {
        return archive != null && archive;
    }
}
