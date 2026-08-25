import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { TaskService } from '../../core/services/task.service';
import { ApplicationService } from '../../core/services/application.service';
import { Task } from '../../core/models/task.model';
import { Application } from '../../core/models/application.model';

@Component({
  selector: 'app-tasks',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './tasks.component.html',
})
export class TasksComponent implements OnInit {
  private readonly taskService = inject(TaskService);
  private readonly applicationService = inject(ApplicationService);
  private readonly fb = inject(FormBuilder);

  tasks = signal<Task[]>([]);
  applications = signal<Application[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  showForm = signal(false);
  editingId = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    description: [''],
    applicationId: [''],
    dueDate: [''],
    priority: ['MEDIUM'],
    status: ['TODO'],
  });

  ngOnInit(): void {
    this.applicationService.list({ size: 100 }).subscribe((page) => this.applications.set(page.content));
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.taskService.list({ size: 100 }).subscribe({
      next: (page) => {
        this.tasks.set(page.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load tasks.');
        this.loading.set(false);
      },
    });
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.form.reset({ priority: 'MEDIUM', status: 'TODO' });
    this.showForm.set(true);
  }

  openEditForm(task: Task): void {
    this.editingId.set(task.id);
    this.form.setValue({
      title: task.title,
      description: task.description ?? '',
      applicationId: task.applicationId ?? '',
      dueDate: task.dueDate ?? '',
      priority: task.priority,
      status: task.status,
    });
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  submit(): void {
    if (this.form.invalid) return;
    const raw = this.form.getRawValue();
    const payload = {
      title: raw.title,
      description: raw.description || undefined,
      applicationId: raw.applicationId || undefined,
      dueDate: raw.dueDate || undefined,
      priority: raw.priority,
      status: raw.status as any,
    };

    const id = this.editingId();
    const request$ = id ? this.taskService.update(id, payload) : this.taskService.create(payload);
    request$.subscribe({
      next: () => {
        this.showForm.set(false);
        this.reload();
      },
      error: () => this.error.set('Could not save this task.'),
    });
  }

  toggleDone(task: Task): void {
    const newStatus = task.status === 'DONE' ? 'TODO' : 'DONE';
    this.taskService.update(task.id, {
      title: task.title,
      description: task.description,
      applicationId: task.applicationId,
      dueDate: task.dueDate,
      priority: task.priority,
      status: newStatus,
    }).subscribe(() => this.reload());
  }

  remove(task: Task): void {
    if (!confirm(`Delete "${task.title}"?`)) return;
    this.taskService.delete(task.id).subscribe(() => this.reload());
  }
}
