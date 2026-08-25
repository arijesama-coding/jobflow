import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { environment } from '../../../environments/environment';
import { Page } from '../models/company.model';
import { Task, TaskRequest } from '../models/task.model';

@Injectable({ providedIn: 'root' })
export class TaskService {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = `${environment.apiBaseUrl}/tasks`;

  list(params: { status?: string; size?: number } = {}) {
    let httpParams = new HttpParams();
    Object.entries(params).forEach(([key, value]) => {
      if (value !== undefined && value !== null && value !== '') {
        httpParams = httpParams.set(key, String(value));
      }
    });
    return this.http.get<Page<Task>>(this.baseUrl, { params: httpParams });
  }

  calendar(from: string, to: string) {
    const params = new HttpParams().set('from', from).set('to', to);
    return this.http.get<Task[]>(`${this.baseUrl}/calendar`, { params });
  }

  create(request: TaskRequest) {
    return this.http.post<Task>(this.baseUrl, request);
  }

  update(id: string, request: TaskRequest) {
    return this.http.put<Task>(`${this.baseUrl}/${id}`, request);
  }

  delete(id: string) {
    return this.http.delete<void>(`${this.baseUrl}/${id}`);
  }
}
