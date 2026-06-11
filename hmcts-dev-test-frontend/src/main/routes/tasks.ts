import { Application } from 'express';
import TaskService from '../services/TaskService';
import {
  buildTaskRequest,
  getTaskFormValues,
  getTaskFormValuesFromTask,
  parseTaskStatus,
} from './taskForm';

export default function (app: Application): void {
  app.get('/tasks', async (req, res) => {
    try {
      const tasks = await TaskService.getAllTasks();
      res.render('tasks/list', { tasks });
    } catch (error) {
      console.error('Error fetching tasks:', error);
      res.render('tasks/list', { tasks: [], error: 'Failed to load tasks' });
    }
  });

  app.get('/tasks/new', (req, res) => {
    res.render('tasks/form', { task: null, formValues: null, isNew: true });
  });

  app.get('/tasks/:id', async (req, res) => {
    try {
      const task = await TaskService.getTaskById(req.params.id);
      res.render('tasks/detail', { task });
    } catch (error) {
      console.error('Error fetching task:', error);
      res.render('error', { status: 404, message: 'Task not found' });
    }
  });

  app.get('/tasks/:id/edit', async (req, res) => {
    try {
      const task = await TaskService.getTaskById(req.params.id);
      res.render('tasks/form', { task, formValues: getTaskFormValuesFromTask(task), isNew: false });
    } catch (error) {
      console.error('Error fetching task:', error);
      res.render('error', { status: 404, message: 'Task not found' });
    }
  });

  app.post('/tasks', async (req, res) => {
    const formValues = getTaskFormValues(req.body as Record<string, unknown>);

    try {
      const newTask = await TaskService.createTask(buildTaskRequest(formValues));
      if (formValues.status !== 'TODO') {
        await TaskService.updateTaskStatus(newTask.id, formValues.status);
      }
      res.redirect(`/tasks/${newTask.id}`);
    } catch (error) {
      console.error('Error creating task:', error);
      const errorMessage = error instanceof Error ? error.message : 'Failed to create task';
      res.render('tasks/form', {
        task: null,
        formValues,
        isNew: true,
        error: errorMessage,
      });
    }
  });

  app.post('/tasks/:id', async (req, res) => {
    const formValues = getTaskFormValues(req.body as Record<string, unknown>);

    try {
      await TaskService.updateTask(req.params.id, buildTaskRequest(formValues));
      await TaskService.updateTaskStatus(req.params.id, formValues.status);
      res.redirect(`/tasks/${req.params.id}`);
    } catch (error) {
      console.error('Error updating task:', error);
      const errorMessage = error instanceof Error ? error.message : 'Failed to update task';
      const task = await TaskService.getTaskById(req.params.id).catch(() => null);
      if (!task) {
        res.render('error', { status: 404, message: 'Task not found' });
        return;
      }

      res.render('tasks/form', { task, formValues, isNew: false, error: errorMessage });
    }
  });

  app.post('/tasks/:id/status', async (req, res) => {
    try {
      const status = parseTaskStatus(String(req.body.status ?? '').trim());
      await TaskService.updateTaskStatus(req.params.id, status);
      res.redirect(`/tasks/${req.params.id}`);
    } catch (error) {
      console.error('Error updating task status:', error);
      const task = await TaskService.getTaskById(req.params.id).catch(() => null);
      if (!task) {
        res.render('error', { status: 404, message: 'Task not found' });
        return;
      }

      const errorMessage = error instanceof Error ? error.message : 'Failed to update status';
      res.render('tasks/detail', { task, error: errorMessage });
    }
  });

  app.post('/tasks/:id/delete', async (req, res) => {
    try {
      await TaskService.deleteTask(req.params.id);
      res.redirect('/tasks');
    } catch (error) {
      console.error('Error deleting task:', error);
      const task = await TaskService.getTaskById(req.params.id).catch(() => null);
      if (!task) {
        res.render('error', { status: 404, message: 'Task not found' });
        return;
      }

      res.render('tasks/detail', { task, error: 'Failed to delete task' });
    }
  });
}
