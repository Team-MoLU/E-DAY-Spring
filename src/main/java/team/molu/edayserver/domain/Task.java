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
    private final String id;

    @Property
    private final String name;

    @Property
    private final String memo;

    @Property
    private final LocalDateTime start_date;

    @Property
    private final LocalDateTime end_date;

    @Property
    private final int priority;

    @Property
    private final boolean check;

    @Property
    private final boolean archive;

    @Relationship(type = "CREATED_BY", direction = Relationship.Direction.INCOMING)
    private User createdBy;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.OUTGOING)
    private Task parentTask;

    @Relationship(type = "BELONGS_TO", direction = Relationship.Direction.INCOMING)
    private Set<Task> childTasks;

    @Builder
    public Task(String id, String name, String memo, LocalDateTime start_date, LocalDateTime end_date, int priority, boolean check, boolean archive) {
        this.id = id;
        this.name = name;
        this.memo = memo;
        this.start_date = start_date;
        this.end_date = end_date;
        this.priority = priority;
        this.check = check;
        this.archive = archive;
    }
}
