package uk.gov.hmcts.reform.dev.dtos;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import uk.gov.hmcts.reform.dev.models.Task;

import java.time.LocalDateTime;
import java.util.UUID;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class TaskDTO {

    private UUID id;
    private String title;
    private String description;
    private String status;
    private LocalDateTime dueDateTime;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static TaskDTO from(Task task) {
        return new TaskDTO(
                task.getId(),
                task.getTitle(),
                task.getDescription(),
                task.getStatus().name(),
                task.getDueDateTime(),
                task.getCreatedAt(),
                task.getUpdatedAt()
        );
    }
}
