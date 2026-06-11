package uk.gov.hmcts.reform.dev;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import uk.gov.hmcts.reform.dev.controllers.TaskController;
import uk.gov.hmcts.reform.dev.dtos.CreateTaskRequest;
import uk.gov.hmcts.reform.dev.dtos.UpdateTaskRequest;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.services.TaskService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc
@SuppressWarnings("removal")
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private TaskService taskService;

    private Task testTask;
    private UUID testId;

    @BeforeEach
    void setUp() {
        testId = UUID.randomUUID();
        testTask = new Task();
        testTask.setId(testId);
        testTask.setTitle("Test Task");
        testTask.setDescription("Test Description");
        testTask.setStatus(TaskStatus.TODO);
        testTask.setDueDateTime(LocalDateTime.now().plusDays(1));
    }

    @Test
    void testCreateTask() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest(
            "New Task",
            "New Description",
            LocalDateTime.now().plusDays(1)
        );

        when(taskService.createTask(any(Task.class))).thenReturn(testTask);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void testCreateTaskWithoutTitle() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("", "Description", LocalDateTime.now().plusDays(1));

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testCreateTaskWithoutDueDate() throws Exception {
        CreateTaskRequest request = new CreateTaskRequest("New Task", "Description", null);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("dueDateTime: Due date and time is required"));
    }

    @Test
    void testGetTaskById() throws Exception {
        when(taskService.getTaskById(testId)).thenReturn(testTask);

        mockMvc.perform(get("/api/tasks/{id}", testId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(testId.toString()))
                .andExpect(jsonPath("$.title").value("Test Task"));
    }

    @Test
    void testGetTaskByIdNotFound() throws Exception {
        when(taskService.getTaskById(testId)).thenThrow(new TaskNotFoundException(testId));

        mockMvc.perform(get("/api/tasks/{id}", testId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAllTasks() throws Exception {
        List<Task> tasks = List.of(testTask);
        when(taskService.getAllTasks()).thenReturn(tasks);

        mockMvc.perform(get("/api/tasks"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value("Test Task"));
    }

    @Test
    void testUpdateTaskStatus() throws Exception {
        testTask.setStatus(TaskStatus.IN_PROGRESS);
        when(taskService.updateTaskStatus(eq(testId), any(TaskStatus.class))).thenReturn(testTask);

        String statusRequest = "{\"status\": \"IN_PROGRESS\"}";

        mockMvc.perform(patch("/api/tasks/{id}/status", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusRequest))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("IN_PROGRESS"));
    }

    @Test
    void testUpdateTaskStatusWithInvalidValue() throws Exception {
        String statusRequest = "{\"status\": \"blocked\"}";

        mockMvc.perform(patch("/api/tasks/{id}/status", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Status must be one of TODO, IN_PROGRESS or DONE"));
    }

    @Test
    void testUpdateTaskStatusWithoutValue() throws Exception {
        String statusRequest = "{\"status\": \"   \"}";

        mockMvc.perform(patch("/api/tasks/{id}/status", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(statusRequest))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("status: Status is required"));
    }

    @Test
    void testUpdateTask() throws Exception {
        UpdateTaskRequest request = new UpdateTaskRequest(
                "Updated Task",
                "",
                LocalDateTime.now().plusDays(2)
        );
        testTask.setTitle("Updated Task");
        testTask.setDescription(null);
        testTask.setDueDateTime(request.getDueDateTime());
        when(taskService.updateTask(eq(testId), any(Task.class))).thenReturn(testTask);

        mockMvc.perform(put("/api/tasks/{id}", testId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Updated Task"));
    }

    @Test
    void testGetTaskByIdWithInvalidUuid() throws Exception {
        mockMvc.perform(get("/api/tasks/{id}", "not-a-uuid"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Task id must be a valid UUID"));
    }

    @Test
    void testDeleteTask() throws Exception {
        doNothing().when(taskService).deleteTask(testId);

        mockMvc.perform(delete("/api/tasks/{id}", testId))
                .andExpect(status().isNoContent());
    }

    @Test
    void testDeleteTaskNotFound() throws Exception {
        doThrow(new TaskNotFoundException(testId)).when(taskService).deleteTask(testId);

        mockMvc.perform(delete("/api/tasks/{id}", testId))
                .andExpect(status().isNotFound());
    }
}
