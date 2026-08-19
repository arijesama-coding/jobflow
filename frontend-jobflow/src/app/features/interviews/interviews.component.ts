import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { InterviewService } from '../../core/services/interview.service';
import { ApplicationService } from '../../core/services/application.service';
import { Interview } from '../../core/models/interview.model';
import { Application } from '../../core/models/application.model';

@Component({
  selector: 'app-interviews',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './interviews.component.html',
})
export class InterviewsComponent implements OnInit {
  private readonly interviewService = inject(InterviewService);
  private readonly applicationService = inject(ApplicationService);
  private readonly fb = inject(FormBuilder);

  interviews = signal<Interview[]>([]);
  applications = signal<Application[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  showForm = signal(false);
  editingId = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    applicationId: ['', Validators.required],
    type: ['VIDEO', Validators.required],
    scheduledAt: ['', Validators.required],
    durationMinutes: [''],
    location: [''],
    meetingUrl: [''],
    interviewer: [''],
    result: ['PENDING'],
    notes: [''],
    feedback: [''],
  });

  ngOnInit(): void {
    this.applicationService.list({ size: 100 }).subscribe((page) => this.applications.set(page.content));
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.interviewService.list({ size: 100 }).subscribe({
      next: (page) => {
        this.interviews.set(page.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load interviews.');
        this.loading.set(false);
      },
    });
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.form.reset({ type: 'VIDEO', result: 'PENDING' });
    this.showForm.set(true);
  }

  openEditForm(interview: Interview): void {
    this.editingId.set(interview.id);
    this.form.setValue({
      applicationId: interview.applicationId,
      type: interview.type,
      scheduledAt: interview.scheduledAt?.slice(0, 16) ?? '',
      durationMinutes: interview.durationMinutes != null ? String(interview.durationMinutes) : '',
      location: interview.location ?? '',
      meetingUrl: interview.meetingUrl ?? '',
      interviewer: interview.interviewer ?? '',
      result: interview.result,
      notes: interview.notes ?? '',
      feedback: interview.feedback ?? '',
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
      applicationId: raw.applicationId,
      type: raw.type as any,
      scheduledAt: raw.scheduledAt,
      durationMinutes: raw.durationMinutes ? Number(raw.durationMinutes) : undefined,
      location: raw.location || undefined,
      meetingUrl: raw.meetingUrl || undefined,
      interviewer: raw.interviewer || undefined,
      result: raw.result as any,
      notes: raw.notes || undefined,
      feedback: raw.feedback || undefined,
    };

    const id = this.editingId();
    const request$ = id ? this.interviewService.update(id, payload) : this.interviewService.create(payload);
    request$.subscribe({
      next: () => {
        this.showForm.set(false);
        this.reload();
      },
      error: () => this.error.set('Could not save this interview.'),
    });
  }

  remove(interview: Interview): void {
    if (!confirm('Delete this interview?')) return;
    this.interviewService.delete(interview.id).subscribe(() => this.reload());
  }

  applicationLabel(app: Application): string {
    return [app.jobOfferTitle, app.companyName].filter(Boolean).join(' @ ') || 'Untitled application';
  }
}
