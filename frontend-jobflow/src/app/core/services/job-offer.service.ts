import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Page } from '../models/company.model';
import { ContractType, JobOffer, JobOfferRequest, RemoteType } from '../models/job-offer.model';

@Injectable({ providedIn: 'root' })
export class JobOfferService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/jobs`;

  list(params: {
    search?: string;
    companyId?: string;
    remoteType?: RemoteType;
    contractType?: ContractType;
    favorite?: boolean;
    archived?: boolean;
    page?: number;
    size?: number;
  } = {}) {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<Page<JobOffer>>(this.baseUrl, { params: httpParams });
  }

  get(id: string) {
    return this.http.get<JobOffer>(`${this.baseUrl}/${id}`);
  }

  create(request: JobOfferRequest) {
    return this.http.post<JobOffer>(this.baseUrl, request);
  }

  update(id: string, request: JobOfferRequest) {
    return this.http.put<JobOffer>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }

  toggleFavorite(id: string) {
    return this.http.patch<JobOffer>(`${this.baseUrl}/${id}/favorite`, {});
  }

  toggleArchived(id: string) {
    return this.http.patch<JobOffer>(`${this.baseUrl}/${id}/archive`, {});
  }
}
