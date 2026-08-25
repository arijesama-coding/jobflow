import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Page } from '../models/company.model';
import { FollowUp, FollowUpRequest, FollowUpStats } from '../models/follow-up.model';

@Injectable({ providedIn: 'root' })
export class FollowUpService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/follow-ups`;

  list(params: { applicationId?: string; size?: number } = {}) {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<Page<FollowUp>>(this.baseUrl, { params: httpParams });
  }

  calendar(from: string, to: string) {
    const params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<FollowUp[]>(`${this.baseUrl}/calendar`, { params });
  }

  stats() {
    return this.http.get<FollowUpStats>(`${this.baseUrl}/stats`);
  }

  create(request: FollowUpRequest) {
    return this.http.post<FollowUp>(this.baseUrl, request);
  }

  update(id: string, request: FollowUpRequest) {
    return this.http.put<FollowUp>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
