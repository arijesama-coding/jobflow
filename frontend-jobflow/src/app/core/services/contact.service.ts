import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Page } from '../models/company.model';
import { Contact, ContactRequest } from '../models/contact.model';

@Injectable({ providedIn: 'root' })
export class ContactService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/contacts`;

  list(params: { search?: string; companyId?: string; size?: number } = {}) {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<Page<Contact>>(this.baseUrl, { params: httpParams });
  }

  create(request: ContactRequest) {
    return this.http.post<Contact>(this.baseUrl, request);
  }

  update(id: string, request: ContactRequest) {
    return this.http.put<Contact>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
