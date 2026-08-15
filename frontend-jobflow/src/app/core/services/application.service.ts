import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Page } from '../models/company.model';
import {
  Application,
  ApplicationRequest,
  ApplicationStatus,
  ApplicationStatusHistoryEntry,
  Priority,
} from '../models/application.model';

@Injectable({ providedIn: 'root' })
export class ApplicationService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/applications`;

  list(params: {
    search?: string;
    status?: ApplicationStatus;
    priority?: Priority;
    companyId?: string;
    dateFrom?: string;
    dateTo?: string;
    page?: number;
    size?: number;
  } = {}) {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<Page<Application>>(this.baseUrl, { params: httpParams });
  }

  get(id: string) {
    return this.http.get<Application>(`${this.baseUrl}/${id}`);
  }

  getHistory(id: string) {
    return this.http.get<ApplicationStatusHistoryEntry[]>(`${this.baseUrl}/${id}/history`);
  }

  create(request: ApplicationRequest) {
    return this.http.post<Application>(this.baseUrl, request);
  }

  update(id: string, request: ApplicationRequest) {
    return this.http.put<Application>(`${this.baseUrl}/${id}`, request);
  }

  updateStatus(id: string, status: ApplicationStatus) {
    return this.http.patch<Application>(`${this.baseUrl}/${id}/status`, { status });
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
