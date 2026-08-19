import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Page } from '../models/company.model';
import { Interview, InterviewRequest } from '../models/interview.model';

@Injectable({ providedIn: 'root' })
export class InterviewService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/interviews`;

  list(params: { applicationId?: string; size?: number } = {}) {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<Page<Interview>>(this.baseUrl, { params: httpParams });
  }

  calendar(from: string, to: string) {
    const params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<Interview[]>(`${this.baseUrl}/calendar`, { params });
  }

  get(id: string) {
    return this.http.get<Interview>(`${this.baseUrl}/${id}`);
  }

  create(request: InterviewRequest) {
    return this.http.post<Interview>(this.baseUrl, request);
  }

  update(id: string, request: InterviewRequest) {
    return this.http.put<Interview>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
