import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Company, CompanyRequest, Page } from '../models/company.model';

@Injectable({ providedIn: 'root' })
export class CompanyService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/companies`;

  list(params: { search?: string; industry?: string; favorite?: boolean; page?: number; size?: number } = {}) {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<Page<Company>>(this.baseUrl, { params: httpParams });
  }

  get(id: string) {
    return this.http.get<Company>(`${this.baseUrl}/${id}`);
  }

  create(request: CompanyRequest) {
    return this.http.post<Company>(this.baseUrl, request);
  }

  update(id: string, request: CompanyRequest) {
    return this.http.put<Company>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  toggleFavorite(id: string) {
    return this.http.patch<Company>(`${this.baseUrl}/${id}/favorite`, {});
  }
}
