package uk.gov.hmcts.reform.dev.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import uk.gov.hmcts.reform.dev.dtos.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.TaskDTO;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskStatusRequest;
import uk.gov.hmcts.reform.dev.exceptions.InvalidTaskException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.services.TaskService;

import java.util.List;
import java.util.Locale;
import java.util.UUID;

@RestController
@RequestMapping("/api/tasks")
@AllArgsConstructor
@Tag(name = "Tasks", description = "Task management API endpoints")
public class TaskController {

    private final TaskService taskService;

    @PostMapping
    @Operation(summary = "Create a new task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Task created successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<TaskDTO> createTask(@Valid @RequestBody CreateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDateTime(request.getDueDateTime());

        Task createdTask = taskService.createTask(task);
        return ResponseEntity.status(HttpStatus.CREATED).body(TaskDTO.from(createdTask));
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get a task by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task found"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<TaskDTO> getTaskById(@PathVariable UUID id) {
        Task task = taskService.getTaskById(id);
        return ResponseEntity.ok(TaskDTO.from(task));
    }

    @GetMapping
    @Operation(summary = "Get all tasks")
    @ApiResponse(responseCode = "200", description = "List of tasks")
    public ResponseEntity<List<TaskDTO>> getAllTasks() {
        List<Task> tasks = taskService.getAllTasks();
        List<TaskDTO> taskDTOs = tasks.stream().map(TaskDTO::from).toList();
        return ResponseEntity.ok(taskDTOs);
    }

    @PatchMapping("/{id}/status")
    @Operation(summary = "Update task status")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task status updated"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Invalid status")
    })
    public ResponseEntity<TaskDTO> updateTaskStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskStatusRequest request) {
        TaskStatus status = parseStatus(request.getStatus());
        Task updatedTask = taskService.updateTaskStatus(id, status);
        return ResponseEntity.ok(TaskDTO.from(updatedTask));
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update a task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Task updated successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found"),
        @ApiResponse(responseCode = "400", description = "Invalid request body")
    })
    public ResponseEntity<TaskDTO> updateTask(
            @PathVariable UUID id,
            @Valid @RequestBody UpdateTaskRequest request) {
        Task task = new Task();
        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setDueDateTime(request.getDueDateTime());

        Task updatedTask = taskService.updateTask(id, task);
        return ResponseEntity.ok(TaskDTO.from(updatedTask));
    }

    @DeleteMapping("/{id}")
    @Operation(summary = "Delete a task")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Task deleted successfully"),
        @ApiResponse(responseCode = "404", description = "Task not found")
    })
    public ResponseEntity<Void> deleteTask(@PathVariable UUID id) {
        taskService.deleteTask(id);
        return ResponseEntity.noContent().build();
    }

    private TaskStatus parseStatus(String rawStatus) {
        try {
            return TaskStatus.valueOf(rawStatus.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException exception) {
            throw new InvalidTaskException("Status must be one of TODO, IN_PROGRESS or DONE");
        }
    }
}
