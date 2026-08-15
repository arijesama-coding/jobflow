import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { CompanyService } from '../../core/services/company.service';
import { Company } from '../../core/models/company.model';

@Component({
  selector: 'app-companies',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './companies.component.html',
})
export class CompaniesComponent implements OnInit {
  private readonly companyService = inject(CompanyService);
  private readonly fb = inject(FormBuilder);

  companies = signal<Company[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  showForm = signal(false);
  editingId = signal<string | null>(null);
  searchTerm = signal('');

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    industry: [''],
    location: [''],
    website: [''],
    size: [''],
    notes: [''],
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.companyService.list({ search: this.searchTerm() || undefined, size: 50 }).subscribe({
      next: (page) => {
        this.companies.set(page.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load companies.');
        this.loading.set(false);
      },
    });
  }

  onSearch(term: string): void {
    this.searchTerm.set(term);
    this.reload();
  }

  openCreateForm(): void {
    this.editingId.set(null);
    this.form.reset();
    this.showForm.set(true);
  }

  openEditForm(company: Company): void {
    this.editingId.set(company.id);
    this.form.setValue({
      name: company.name,
      industry: company.industry ?? '',
      location: company.location ?? '',
      website: company.website ?? '',
      size: company.size ?? '',
      notes: company.notes ?? '',
    });
    this.showForm.set(true);
  }

  cancelForm(): void {
    this.showForm.set(false);
    this.editingId.set(null);
  }

  submit(): void {
    if (this.form.invalid) return;
    const payload = this.form.getRawValue();
    const id = this.editingId();

    const request$ = id ? this.companyService.update(id, payload) : this.companyService.create(payload);
    request$.subscribe({
      next: () => {
        this.showForm.set(false);
        this.reload();
      },
      error: () => this.error.set('Could not save this company.'),
    });
  }

  toggleFavorite(company: Company): void {
    this.companyService.toggleFavorite(company.id).subscribe(() => this.reload());
  }

  remove(company: Company): void {
    if (!confirm(`Delete "${company.name}"? This cannot be undone.`)) return;
    this.companyService.delete(company.id).subscribe(() => this.reload());
  }
}
