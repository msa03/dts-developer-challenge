import axios, { AxiosError } from 'axios';

export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE';

export interface Task {
  id: string;
  title: string;
  description?: string;
  status: TaskStatus;
  dueDateTime: string;
  createdAt?: string;
  updatedAt?: string;
}

export interface CreateTaskRequest {
  title: string;
  description?: string | null;
  dueDateTime: string;
}

export interface UpdateTaskRequest extends CreateTaskRequest {}

export interface UpdateTaskStatusRequest {
  status: TaskStatus;
}

class TaskService {
  private readonly baseUrl = process.env.BACKEND_URL || 'http://localhost:4000';

  async createTask(request: CreateTaskRequest): Promise<Task> {
    try {
      const response = await axios.post<Task>(`${this.baseUrl}/api/tasks`, request);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async getTaskById(id: string): Promise<Task> {
    try {
      const response = await axios.get<Task>(`${this.baseUrl}/api/tasks/${id}`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async getAllTasks(): Promise<Task[]> {
    try {
      const response = await axios.get<Task[]>(`${this.baseUrl}/api/tasks`);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async updateTaskStatus(id: string, status: TaskStatus): Promise<Task> {
    try {
      const request: UpdateTaskStatusRequest = { status };
      const response = await axios.patch<Task>(`${this.baseUrl}/api/tasks/${id}/status`, request);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async updateTask(id: string, request: UpdateTaskRequest): Promise<Task> {
    try {
      const response = await axios.put<Task>(`${this.baseUrl}/api/tasks/${id}`, request);
      return response.data;
    } catch (error) {
      throw this.handleError(error);
    }
  }

  async deleteTask(id: string): Promise<void> {
    try {
      await axios.delete(`${this.baseUrl}/api/tasks/${id}`);
    } catch (error) {
      throw this.handleError(error);
    }
  }

  private handleError(error: unknown): Error {
    if (axios.isAxiosError(error)) {
      const axiosError = error as AxiosError<{ message?: string }>;
      const message = axiosError.response?.data?.message || axiosError.message || 'An error occurred';
      return new Error(message);
    }
    return new Error('An unexpected error occurred');
  }
}

export default new TaskService();
