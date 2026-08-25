export type TaskStatus = 'TODO' | 'IN_PROGRESS' | 'DONE' | 'CANCELLED';

export interface Task {
  id: string;
  title: string;
  description?: string;
  applicationId?: string;
  applicationLabel?: string;
  dueDate?: string;
  priority: string;
  status: TaskStatus;
  createdAt: string;
  updatedAt: string;
}

export interface TaskRequest {
  title: string;
  description?: string;
  applicationId?: string;
  dueDate?: string;
  priority?: string;
  status?: TaskStatus;
}
