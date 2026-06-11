package uk.gov.hmcts.reform.dev;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.hmcts.reform.dev.exceptions.TaskNotFoundException;
import uk.gov.hmcts.reform.dev.models.Task;
import uk.gov.hmcts.reform.dev.models.Task.TaskStatus;
import uk.gov.hmcts.reform.dev.repositories.TaskRepository;
import uk.gov.hmcts.reform.dev.services.TaskService;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository taskRepository;

    @InjectMocks
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
    void testCreateTask() {
        Task newTask = new Task();
        newTask.setTitle("New Task");
        newTask.setDescription("   ");

        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.createTask(newTask);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getTitle()).isEqualTo("Test Task");
        verify(taskRepository).save(any(Task.class));
        assertThat(newTask.getStatus()).isEqualTo(TaskStatus.TODO);
        assertThat(newTask.getDescription()).isNull();
    }

    @Test
    void testGetTaskById() {
        when(taskRepository.findById(testId)).thenReturn(Optional.of(testTask));

        Task result = taskService.getTaskById(testId);

        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(testId);
        assertThat(result.getTitle()).isEqualTo("Test Task");
    }

    @Test
    void testGetTaskByIdNotFound() {
        when(taskRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.getTaskById(testId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void testGetAllTasks() {
        List<Task> tasks = List.of(testTask);
        when(taskRepository.findAllOrderedByDueDate()).thenReturn(tasks);

        List<Task> result = taskService.getAllTasks();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(testId);
    }

    @Test
    void testUpdateTaskStatus() {
        when(taskRepository.findById(testId)).thenReturn(Optional.of(testTask));
        testTask.setStatus(TaskStatus.IN_PROGRESS);
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.updateTaskStatus(testId, TaskStatus.IN_PROGRESS);

        assertThat(result.getStatus()).isEqualTo(TaskStatus.IN_PROGRESS);
        verify(taskRepository).save(any(Task.class));
    }

    @Test
    void testDeleteTask() {
        when(taskRepository.findById(testId)).thenReturn(Optional.of(testTask));

        taskService.deleteTask(testId);

        verify(taskRepository).delete(testTask);
    }

    @Test
    void testDeleteTaskNotFound() {
        when(taskRepository.findById(testId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> taskService.deleteTask(testId))
                .isInstanceOf(TaskNotFoundException.class);
    }

    @Test
    void testUpdateTask() {
        when(taskRepository.findById(testId)).thenReturn(Optional.of(testTask));

        Task updatedTask = new Task();
        updatedTask.setTitle("Updated Task");
        updatedTask.setDescription("   ");
        updatedTask.setDueDateTime(LocalDateTime.now().plusDays(2));

        testTask.setTitle("Updated Task");
        testTask.setDescription(null);
        testTask.setDueDateTime(updatedTask.getDueDateTime());
        when(taskRepository.save(any(Task.class))).thenReturn(testTask);

        Task result = taskService.updateTask(testId, updatedTask);

        assertThat(result.getTitle()).isEqualTo("Updated Task");
        assertThat(result.getDescription()).isNull();
        assertThat(result.getDueDateTime()).isEqualTo(updatedTask.getDueDateTime());
    }
}
