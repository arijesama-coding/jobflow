import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { JobOfferService } from '../../core/services/job-offer.service';
import { CompanyService } from '../../core/services/company.service';
import { JobOffer } from '../../core/models/job-offer.model';
import { Company } from '../../core/models/company.model';

@Component({
  selector: 'app-jobs',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './jobs.component.html',
})
export class JobsComponent implements OnInit {
  private readonly jobOfferService = inject(JobOfferService);
  private readonly companyService = inject(CompanyService);
  private readonly fb = inject(FormBuilder);

  jobs = signal<JobOffer[]>([]);
  companies = signal<Company[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  showForm = signal(false);
  editingId = signal<string | null>(null);
  searchTerm = signal('');
  showArchived = signal(false);

  form = this.fb.nonNullable.group({
    title: ['', Validators.required],
    companyId: [''],
    location: [''],
    remoteType: [''],
    contractType: [''],
    jobUrl: [''],
    deadline: [''],
    skillsText: [''],
  });

  ngOnInit(): void {
    this.companyService.list({ size: 100 }).subscribe((page) => this.companies.set(page.content));
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.jobOfferService
      .list({ search: this.searchTerm() || undefined, archived: this.showArchived(), size: 50 })
      .subscribe({
        next: (page) => {
          this.jobs.set(page.content);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Unable to load job offers.');
          this.loading.set(false);
        },
      });
  }

  onSearch(term: string): void {
    this.searchTerm.set(term);
    this.reload();
  }

  toggleShowArchived(): void {
    this.showArchived.set(!this.showArchived());
    this.reload();
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.form.reset();
    this.showForm.set(true);
  }

  openEditForm(job: JobOffer): void {
    this.editingId.set(job.id);
    this.form.setValue({
      title: job.title,
      companyId: job.companyId ?? '',
      location: job.location ?? '',
      remoteType: job.remoteType ?? '',
      contractType: job.contractType ?? '',
      jobUrl: job.jobUrl ?? '',
      deadline: job.deadline ?? '',
      skillsText: (job.skills ?? []).join(', '),
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
      companyId: raw.companyId || undefined,
      location: raw.location || undefined,
      remoteType: (raw.remoteType || undefined) as any,
      contractType: (raw.contractType || undefined) as any,
      jobUrl: raw.jobUrl || undefined,
      deadline: raw.deadline || undefined,
      skills: raw.skillsText
        ? raw.skillsText.split(',').map((s) => s.trim()).filter(Boolean)
        : [],
    };

    const id = this.editingId();
    const request$ = id ? this.jobOfferService.update(id, payload) : this.jobOfferService.create(payload);
    request$.subscribe({
      next: () => {
        this.showForm.set(false);
        this.reload();
      },
      error: () => this.error.set('Could not save this job offer.'),
    });
  }

  toggleFavorite(job: JobOffer): void {
    this.jobOfferService.toggleFavorite(job.id).subscribe(() => this.reload());
  }

  toggleArchived(job: JobOffer): void {
    this.jobOfferService.toggleArchived(job.id).subscribe(() => this.reload());
  }

  remove(job: JobOffer): void {
    if (!confirm(`Delete "${job.title}"? This cannot be undone.`)) return;
    this.jobOfferService.delete(job.id).subscribe(() => this.reload());
  }
}
