import { CreateTaskRequest, Task, TaskStatus } from '../services/TaskService';

export interface TaskFormValues {
  title: string;
  description: string;
  status: TaskStatus;
  dueDateDay: string;
  dueDateMonth: string;
  dueDateYear: string;
  dueTime: string;
}

export class TaskFormError extends Error {}

const validStatuses: readonly TaskStatus[] = ['TODO', 'IN_PROGRESS', 'DONE'];
const timePattern = /^([01]\d|2[0-3]):([0-5]\d)$/;

export function getTaskFormValues(body: Record<string, unknown>): TaskFormValues {
  const status = (typeof body.status === 'string' && isTaskStatus(body.status.trim())
    ? body.status.trim()
    : 'TODO') as TaskStatus;

  return {
    title: readField(body.title),
    description: readField(body.description),
    status,
    dueDateDay: readField(body['dueDate-day']),
    dueDateMonth: readField(body['dueDate-month']),
    dueDateYear: readField(body['dueDate-year']),
    dueTime: readField(body.dueTime),
  };
}

export function getTaskFormValuesFromTask(task: Task): TaskFormValues {
  const [datePart, timePart = '00:00:00'] = task.dueDateTime.split('T');
  const [year, month, day] = datePart.split('-');

  return {
    title: task.title,
    description: task.description ?? '',
    status: task.status,
    dueDateDay: day ?? '',
    dueDateMonth: month ?? '',
    dueDateYear: year ?? '',
    dueTime: timePart.slice(0, 5),
  };
}

export function buildTaskRequest(formValues: TaskFormValues): CreateTaskRequest {
  if (!formValues.title) {
    throw new TaskFormError('Enter a task title');
  }

  if (!isTaskStatus(formValues.status)) {
    throw new TaskFormError('Choose a valid status');
  }

  return {
    title: formValues.title,
    description: formValues.description || null,
    dueDateTime: buildDueDateTime(formValues),
  };
}

export function parseTaskStatus(status: string): TaskStatus {
  if (!isTaskStatus(status)) {
    throw new TaskFormError('Choose a valid status');
  }

  return status;
}

function buildDueDateTime(formValues: TaskFormValues): string {
  const { dueDateDay, dueDateMonth, dueDateYear, dueTime } = formValues;

  if (!dueDateDay || !dueDateMonth || !dueDateYear) {
    throw new TaskFormError('Enter a due date');
  }

  if (!dueTime) {
    throw new TaskFormError('Enter a due time');
  }

  if (![dueDateDay, dueDateMonth, dueDateYear].every(value => /^\d+$/.test(value))) {
    throw new TaskFormError('Due date must contain numbers only');
  }

  if (!timePattern.test(dueTime)) {
    throw new TaskFormError('Enter a due time in HH:MM format');
  }

  const day = dueDateDay.padStart(2, '0');
  const month = dueDateMonth.padStart(2, '0');
  const isoDateTime = `${dueDateYear}-${month}-${day}T${dueTime}:00`;
  const dueDateTime = new Date(isoDateTime);

  if (Number.isNaN(dueDateTime.getTime())) {
    throw new TaskFormError('Enter a real due date and time');
  }

  const isMatchingDate =
    String(dueDateTime.getFullYear()) === dueDateYear
    && String(dueDateTime.getMonth() + 1).padStart(2, '0') === month
    && String(dueDateTime.getDate()).padStart(2, '0') === day;

  if (!isMatchingDate) {
    throw new TaskFormError('Enter a real due date and time');
  }

  const now = new Date();
  now.setSeconds(0, 0);
  if (dueDateTime < now) {
    throw new TaskFormError('Due date and time must be in the present or future');
  }

  return isoDateTime;
}

function isTaskStatus(value: string): value is TaskStatus {
  return validStatuses.includes(value as TaskStatus);
}

function readField(value: unknown): string {
  return typeof value === 'string' ? value.trim() : '';
}
