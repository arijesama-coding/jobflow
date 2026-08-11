import { HttpClient } from '@angular/common/http';
import { Injectable, computed, inject, signal } from '@angular/core';
import { Router } from '@angular/router';
import { tap } from 'rxjs';
import { environment } from '../../../environments/environment';
import { AuthResponse, LoginRequest, RegisterRequest, UserResponse } from '../models/auth.model';

const ACCESS_TOKEN_KEY = 'jobflow_access_token';
const REFRESH_TOKEN_KEY = 'jobflow_refresh_token';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly http = inject(HttpClient);
  private readonly router = inject(Router);

  private readonly currentUserSignal = signal<UserResponse | null>(null);
  readonly currentUser = this.currentUserSignal.asReadonly();
  readonly isAuthenticated = computed(() => this.currentUserSignal() !== null);
  readonly isAdmin = computed(() => this.currentUserSignal()?.role === 'ADMIN');

  login(request: LoginRequest) {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/login`, request).pipe(
      tap((res) => this.handleAuthSuccess(res)),
    );
  }

  register(request: RegisterRequest) {
    return this.http.post<AuthResponse>(`${environment.apiBaseUrl}/auth/register`, request).pipe(
      tap((res) => this.handleAuthSuccess(res)),
    );
  }

  logout(): void {
    localStorage.removeItem(ACCESS_TOKEN_KEY);
    localStorage.removeItem(REFRESH_TOKEN_KEY);
    this.currentUserSignal.set(null);
    this.router.navigate(['/login']);
  }

  getAccessToken(): string | null {
    return localStorage.getItem(ACCESS_TOKEN_KEY);
  }

  getRefreshToken(): string | null {
    return localStorage.getItem(REFRESH_TOKEN_KEY);
  }

  private handleAuthSuccess(res: AuthResponse): void {
    localStorage.setItem(ACCESS_TOKEN_KEY, res.accessToken);
    if (res.refreshToken) {
      localStorage.setItem(REFRESH_TOKEN_KEY, res.refreshToken);
    }
    this.currentUserSignal.set(res.user);
  }
}
