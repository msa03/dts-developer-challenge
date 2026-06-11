import axios from 'axios';
import TaskService, { Task, CreateTaskRequest, UpdateTaskRequest } from '../../../main/services/TaskService';

jest.mock('axios');

const mockedAxios = axios as jest.Mocked<typeof axios>;

describe('TaskService', () => {
  const mockTask: Task = {
    id: '1',
    title: 'Test Task',
    description: 'Test Description',
    status: 'TODO',
    dueDateTime: '2024-12-31T10:00:00',
    createdAt: '2024-01-01T10:00:00',
    updatedAt: '2024-01-01T10:00:00',
  };

  const baseUrl = 'http://localhost:4000';

  afterEach(() => {
    jest.clearAllMocks();
  });

  describe('getAllTasks', () => {
    it('should fetch all tasks successfully', async () => {
      const mockTasks = [mockTask];
      mockedAxios.get.mockResolvedValue({ data: mockTasks });

      const result = await TaskService.getAllTasks();

      expect(result).toEqual(mockTasks);
      expect(mockedAxios.get).toHaveBeenCalledWith(`${baseUrl}/api/tasks`);
    });

    it('should handle error when fetching tasks fails', async () => {
      mockedAxios.get.mockRejectedValue(new Error('Network error'));

      await expect(TaskService.getAllTasks()).rejects.toThrow();
    });
  });

  describe('getTaskById', () => {
    it('should fetch a task by ID successfully', async () => {
      const taskId = '1';
      mockedAxios.get.mockResolvedValue({ data: mockTask });

      const result = await TaskService.getTaskById(taskId);

      expect(result).toEqual(mockTask);
      expect(mockedAxios.get).toHaveBeenCalledWith(`${baseUrl}/api/tasks/${taskId}`);
    });

    it('should handle error when task not found', async () => {
      mockedAxios.get.mockRejectedValue({ response: { data: { message: 'Not found' } } });

      await expect(TaskService.getTaskById('invalid')).rejects.toThrow();
    });
  });

  describe('createTask', () => {
    it('should create a task successfully', async () => {
      const createRequest: CreateTaskRequest = {
        title: 'New Task',
        description: 'New Description',
      };
      mockedAxios.post.mockResolvedValue({ data: mockTask });

      const result = await TaskService.createTask(createRequest);

      expect(result).toEqual(mockTask);
      expect(mockedAxios.post).toHaveBeenCalledWith(`${baseUrl}/api/tasks`, createRequest);
    });

    it('should handle validation error on task creation', async () => {
      mockedAxios.post.mockRejectedValue({ response: { data: { message: 'Title is required' } } });

      await expect(TaskService.createTask({ title: '' })).rejects.toThrow();
    });
  });

  describe('updateTaskStatus', () => {
    it('should update task status successfully', async () => {
      const taskId = '1';
      mockedAxios.patch.mockResolvedValue({ data: { ...mockTask, status: 'IN_PROGRESS' } });

      const result = await TaskService.updateTaskStatus(taskId, 'IN_PROGRESS');

      expect(result.status).toBe('IN_PROGRESS');
      expect(mockedAxios.patch).toHaveBeenCalledWith(`${baseUrl}/api/tasks/${taskId}/status`, {
        status: 'IN_PROGRESS',
      });
    });
  });

  describe('updateTask', () => {
    it('should update a task successfully', async () => {
      const taskId = '1';
      const updateRequest: UpdateTaskRequest = {
        title: 'Updated task',
        description: '',
        dueDateTime: '2026-12-31T14:30:00',
      };
      mockedAxios.put.mockResolvedValue({ data: { ...mockTask, ...updateRequest } });

      const result = await TaskService.updateTask(taskId, updateRequest);

      expect(result.title).toBe('Updated task');
      expect(mockedAxios.put).toHaveBeenCalledWith(`${baseUrl}/api/tasks/${taskId}`, updateRequest);
    });
  });

  describe('deleteTask', () => {
    it('should delete a task successfully', async () => {
      const taskId = '1';
      mockedAxios.delete.mockResolvedValue({});

      await TaskService.deleteTask(taskId);

      expect(mockedAxios.delete).toHaveBeenCalledWith(`${baseUrl}/api/tasks/${taskId}`);
    });

    it('should handle error when deleting non-existent task', async () => {
      mockedAxios.delete.mockRejectedValue({ response: { data: { message: 'Not found' } } });

      await expect(TaskService.deleteTask('invalid')).rejects.toThrow();
    });
  });
});
