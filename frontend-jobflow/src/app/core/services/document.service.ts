import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Page } from '../models/company.model';
import { AppDocument, DocumentType } from '../models/document.model';

@Injectable({ providedIn: 'root' })
export class DocumentService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/documents`;

  list(params: { applicationId?: string; type?: DocumentType; size?: number } = {}) {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<Page<AppDocument>>(this.baseUrl, { params: httpParams });
  }

  upload(file: File, type: DocumentType, applicationId?: string, replaceDocumentId?: string) {
    const formData = new FormData();
    formData.append('file', file);
    let params = new HttpParams().set('type', type);
    if (applicationId) params = params.set('applicationId', applicationId);
    if (replaceDocumentId) params = params.set('replaceDocumentId', replaceDocumentId);
    return this.http.post<AppDocument>(this.baseUrl, formData, { params });
  }

  download(id: string, fileName: string): void {
    this.http.get(`${this.baseUrl}/${id}/download`, { responseType: 'blob' }).subscribe((blob) => {
      const url = window.URL.createObjectURL(blob);
      const a = document.createElement('a');
      a.href = url;
      a.download = fileName;
      a.click();
      window.URL.revokeObjectURL(url);
    });
  }

  rename(id: string, fileName: string) {
    return this.http.patch<AppDocument>(`${this.baseUrl}/${id}`, { fileName });
  }

  setPrimary(id: string) {
    return this.http.patch<AppDocument>(`${this.baseUrl}/${id}/primary`, {});
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
