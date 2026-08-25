import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { ContactService } from '../../core/services/contact.service';
import { CompanyService } from '../../core/services/company.service';
import { Contact } from '../../core/models/contact.model';
import { Company } from '../../core/models/company.model';

@Component({
  selector: 'app-contacts',
  standalone: true,
  imports: [CommonModule, ReactiveFormsModule],
  templateUrl: './contacts.component.html',
})
export class ContactsComponent implements OnInit {
  private readonly contactService = inject(ContactService);
  private readonly companyService = inject(CompanyService);
  private readonly fb = inject(FormBuilder);

  contacts = signal<Contact[]>([]);
  companies = signal<Company[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  showForm = signal(false);
  editingId = signal<string | null>(null);
  searchTerm = signal('');

  form = this.fb.nonNullable.group({
    name: ['', Validators.required],
    email: [''],
    phone: [''],
    position: [''],
    companyId: [''],
    linkedinUrl: [''],
    notes: [''],
  });

  ngOnInit(): void {
    this.companyService.list({ size: 100 }).subscribe((page) => this.companies.set(page.content));
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.contactService.list({ search: this.searchTerm() || undefined, size: 100 }).subscribe({
      next: (page) => {
        this.contacts.set(page.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load contacts.');
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

  openEditForm(contact: Contact): void {
    this.editingId.set(contact.id);
    this.form.setValue({
      name: contact.name,
      email: contact.email ?? '',
      phone: contact.phone ?? '',
      position: contact.position ?? '',
      companyId: contact.companyId ?? '',
      linkedinUrl: contact.linkedinUrl ?? '',
      notes: contact.notes ?? '',
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
      name: raw.name,
      email: raw.email || undefined,
      phone: raw.phone || undefined,
      position: raw.position || undefined,
      companyId: raw.companyId || undefined,
      linkedinUrl: raw.linkedinUrl || undefined,
      notes: raw.notes || undefined,
    };

    const id = this.editingId();
    const request$ = id ? this.contactService.update(id, payload) : this.contactService.create(payload);
    request$.subscribe({
      next: () => {
        this.showForm.set(false);
        this.reload();
      },
      error: () => this.error.set('Could not save this contact.'),
    });
  }

  remove(contact: Contact): void {
    if (!confirm(`Delete "${contact.name}"?`)) return;
    this.contactService.delete(contact.id).subscribe(() => this.reload());
  }
}
