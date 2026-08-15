import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ApplicationService } from '../../core/services/application.service';
import { CompanyService } from '../../core/services/company.service';
import { JobOfferService } from '../../core/services/job-offer.service';
import { APPLICATION_STATUSES, Application, ApplicationStatus } from '../../core/models/application.model';
import { Company } from '../../core/models/company.model';
import { JobOffer } from '../../core/models/job-offer.model';

@Component({
  selector: 'app-applications',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './applications.component.html',
})
export class ApplicationsComponent implements OnInit {
  private readonly applicationService = inject(ApplicationService);
  private readonly companyService = inject(CompanyService);
  private readonly jobOfferService = inject(JobOfferService);
  private readonly fb = inject(FormBuilder);

  readonly statuses = APPLICATION_STATUSES;

  applications = signal<Application[]>([]);
  companies = signal<Company[]>([]);
  jobOffers = signal<JobOffer[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  showForm = signal(false);
  editingId = signal<string | null>(null);
  statusFilter = signal<ApplicationStatus | ''>('');
  searchTerm = signal('');

  form = this.fb.nonNullable.group({
    companyId: [''],
    jobOfferId: [''],
    status: ['WISHLIST' as ApplicationStatus, Validators.required],
    priority: ['MEDIUM'],
    source: [''],
    applicationDate: [''],
    salaryExpectation: [''],
    nextFollowUpDate: [''],
    notes: [''],
  });

  ngOnInit(): void {
    this.companyService.list({ size: 100 }).subscribe((page) => this.companies.set(page.content));
    this.jobOfferService.list({ size: 100 }).subscribe((page) => this.jobOffers.set(page.content));
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.applicationService
      .list({ search: this.searchTerm() || undefined, status: this.statusFilter() || undefined, size: 50 })
      .subscribe({
        next: (page) => {
          this.applications.set(page.content);
          this.loading.set(false);
        },
        error: () => {
          this.error.set('Unable to load applications.');
          this.loading.set(false);
        },
      });
  }

  onSearch(term: string): void {
    this.searchTerm.set(term);
    this.reload();
  }

  onStatusFilter(status: string): void {
    this.statusFilter.set(status as ApplicationStatus | '');
    this.reload();
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.form.reset({ status: 'WISHLIST', priority: 'MEDIUM' });
    this.showForm.set(true);
  }

  openEditForm(application: Application): void {
    this.editingId.set(application.id);
    this.form.setValue({
      companyId: application.companyId ?? '',
      jobOfferId: application.jobOfferId ?? '',
      status: application.status,
      priority: application.priority,
      source: application.source ?? '',
      applicationDate: application.applicationDate ?? '',
      salaryExpectation: application.salaryExpectation != null ? String(application.salaryExpectation) : '',
      nextFollowUpDate: application.nextFollowUpDate ?? '',
      notes: application.notes ?? '',
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
      companyId: raw.companyId || undefined,
      jobOfferId: raw.jobOfferId || undefined,
      status: raw.status,
      priority: (raw.priority || undefined) as any,
      source: (raw.source || undefined) as any,
      applicationDate: raw.applicationDate || undefined,
      salaryExpectation: raw.salaryExpectation ? Number(raw.salaryExpectation) : undefined,
      nextFollowUpDate: raw.nextFollowUpDate || undefined,
      notes: raw.notes || undefined,
    };

    const id = this.editingId();
    const request$ = id ? this.applicationService.update(id, payload) : this.applicationService.create(payload);
    request$.subscribe({
      next: () => {
        this.showForm.set(false);
        this.reload();
      },
      error: () => this.error.set('Could not save this application.'),
    });
  }

  changeStatus(application: Application, status: string): void {
    this.applicationService.updateStatus(application.id, status as ApplicationStatus).subscribe(() => this.reload());
  }

  remove(application: Application): void {
    if (!confirm(`Delete this application for "${application.jobOfferTitle ?? application.companyName ?? 'this role'}"?`)) return;
    this.applicationService.delete(application.id).subscribe(() => this.reload());
  }
}
