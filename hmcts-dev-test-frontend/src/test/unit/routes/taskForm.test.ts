import {
  buildTaskRequest,
  getTaskFormValues,
  getTaskFormValuesFromTask,
  parseTaskStatus,
  TaskFormError,
} from '../../../main/routes/taskForm';
import { Task } from '../../../main/services/TaskService';

describe('taskForm helpers', () => {
  const futureDate = new Date();
  futureDate.setDate(futureDate.getDate() + 1);

  const validFormValues = {
    title: 'Review case notes',
    description: 'Check the latest update',
    status: 'IN_PROGRESS' as const,
    dueDateDay: String(futureDate.getDate()),
    dueDateMonth: String(futureDate.getMonth() + 1),
    dueDateYear: String(futureDate.getFullYear()),
    dueTime: '14:30',
  };

  it('builds a task request from valid form values', () => {
    expect(buildTaskRequest(validFormValues)).toEqual({
      title: 'Review case notes',
      description: 'Check the latest update',
      dueDateTime: `${futureDate.getFullYear()}-${String(futureDate.getMonth() + 1).padStart(2, '0')}-${String(futureDate.getDate()).padStart(2, '0')}T14:30:00`,
    });
  });

  it('rejects an invalid calendar date', () => {
    expect(() =>
      buildTaskRequest({
        ...validFormValues,
        dueDateDay: '31',
        dueDateMonth: '2',
      })
    ).toThrow(new TaskFormError('Enter a real due date and time'));
  });

  it('rejects an invalid status', () => {
    expect(() => parseTaskStatus('BLOCKED')).toThrow(new TaskFormError('Choose a valid status'));
  });

  it('normalises request body input into form values', () => {
    expect(
      getTaskFormValues({
        title: '  Review case notes  ',
        description: '  Check the latest update  ',
        status: 'DONE',
        'dueDate-day': '4',
        'dueDate-month': '8',
        'dueDate-year': '2026',
        dueTime: '09:15',
      })
    ).toEqual({
      title: 'Review case notes',
      description: 'Check the latest update',
      status: 'DONE',
      dueDateDay: '4',
      dueDateMonth: '8',
      dueDateYear: '2026',
      dueTime: '09:15',
    });
  });

  it('maps a task into form values for editing', () => {
    const task: Task = {
      id: 'task-1',
      title: 'Review case notes',
      description: 'Check the latest update',
      status: 'TODO',
      dueDateTime: '2026-08-04T09:15:00',
      createdAt: '2026-08-01T09:00:00',
      updatedAt: '2026-08-01T09:00:00',
    };

    expect(getTaskFormValuesFromTask(task)).toEqual({
      title: 'Review case notes',
      description: 'Check the latest update',
      status: 'TODO',
      dueDateDay: '04',
      dueDateMonth: '08',
      dueDateYear: '2026',
      dueTime: '09:15',
    });
  });
});
