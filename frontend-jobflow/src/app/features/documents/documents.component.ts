import { CommonModule } from '@angular/common';
import { Component, OnInit, inject, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { DocumentService } from '../../core/services/document.service';
import { ApplicationService } from '../../core/services/application.service';
import { AppDocument, DocumentType } from '../../core/models/document.model';
import { Application } from '../../core/models/application.model';

@Component({
  selector: 'app-documents',
  standalone: true,
  imports: [CommonModule, FormsModule],
  templateUrl: './documents.component.html',
})
export class DocumentsComponent implements OnInit {
  private readonly documentService = inject(DocumentService);
  private readonly applicationService = inject(ApplicationService);

  documents = signal<AppDocument[]>([]);
  applications = signal<Application[]>([]);
  loading = signal(false);
  error = signal<string | null>(null);
  uploading = signal(false);

  selectedType: DocumentType = 'CV';
  selectedApplicationId = '';

  ngOnInit(): void {
    this.applicationService.list({ size: 100 }).subscribe((page) => this.applications.set(page.content));
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.documentService.list({ size: 100 }).subscribe({
      next: (page) => {
        this.documents.set(page.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load documents.');
        this.loading.set(false);
      },
    });
  }

  onFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;
    const file = input.files?.[0];
    if (!file) return;

    this.uploading.set(true);
    this.documentService
      .upload(file, this.selectedType, this.selectedApplicationId || undefined)
      .subscribe({
        next: () => {
          this.uploading.set(false);
          input.value = '';
          this.reload();
        },
        error: () => {
          this.error.set('Upload failed.');
          this.uploading.set(false);
        },
      });
  }

  download(doc: AppDocument): void {
    this.documentService.download(doc.id, doc.fileName);
  }

  rename(doc: AppDocument): void {
    const newName = prompt('New file name', doc.fileName);
    if (!newName || newName === doc.fileName) return;
    this.documentService.rename(doc.id, newName).subscribe(() => this.reload());
  }

  setPrimary(doc: AppDocument): void {
    this.documentService.setPrimary(doc.id).subscribe(() => this.reload());
  }

  remove(doc: AppDocument): void {
    if (!confirm(`Delete "${doc.fileName}"?`)) return;
    this.documentService.delete(doc.id).subscribe(() => this.reload());
  }

  applicationLabel(id: string | undefined): string {
    if (!id) return '—';
    const app = this.applications().find((a) => a.id === id);
    return app ? (app.jobOfferTitle || app.companyName || 'Untitled') : '—';
  }
}
