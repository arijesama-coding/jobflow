import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { FollowUpService } from '../../core/services/follow-up.service';
import { ApplicationService } from '../../core/services/application.service';
import { FollowUp, FollowUpStats } from '../../core/models/follow-up.model';
import { Application } from '../../core/models/application.model';

@Component({
  selector: 'app-follow-ups',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './follow-ups.component.html',
})
export class FollowUpsComponent implements OnInit {
  private readonly followUpService = inject(FollowUpService);
  private readonly applicationService = inject(ApplicationService);
  private readonly fb = inject(FormBuilder);

  followUps = signal<FollowUp[]>([]);
  applications = signal<Application[]>([]);
  stats = signal<FollowUpStats | null>(null);
  loading = signal(false);
  error = signal<string | null>(null);
  showForm = signal(false);
  editingId = signal<string | null>(null);

  form = this.fb.nonNullable.group({
    applicationId: ['', Validators.required],
    followUpDate: ['', Validators.required],
    type: ['EMAIL', Validators.required],
    status: ['PLANNED'],
    notes: [''],
  });

  ngOnInit(): void {
    this.applicationService.list({ size: 100 }).subscribe((page) => this.applications.set(page.content));
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.followUpService.stats().subscribe((stats) => this.stats.set(stats));
    this.followUpService.list({ size: 100 }).subscribe({
      next: (page) => {
        this.followUps.set(page.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load follow-ups.');
        this.loading.set(false);
      },
    });
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.form.reset({ type: 'EMAIL', status: 'PLANNED' });
    this.showForm.set(true);
  }

  openEditForm(followUp: FollowUp): void {
    this.editingId.set(followUp.id);
    this.form.setValue({
      applicationId: followUp.applicationId,
      followUpDate: followUp.followUpDate,
      type: followUp.type,
      status: followUp.status,
      notes: followUp.notes ?? '',
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
      followUpDate: raw.followUpDate,
      type: raw.type as any,
      status: raw.status as any,
      notes: raw.notes || undefined,
    };

    const id = this.editingId();
    const request$ = id ? this.followUpService.update(id, payload) : this.followUpService.create(payload);
    request$.subscribe({
      next: () => {
        this.showForm.set(false);
        this.reload();
      },
      error: () => this.error.set('Could not save this follow-up.'),
    });
  }

  remove(followUp: FollowUp): void {
    if (!confirm('Delete this follow-up?')) return;
    this.followUpService.delete(followUp.id).subscribe(() => this.reload());
  }

  applicationLabel(app: Application): string {
    return [app.jobOfferTitle, app.companyName].filter(Boolean).join(' @ ') || 'Untitled application';
  }
}
