import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DashboardService } from '../../core/services/dashboard.service';
import { Dashboard } from '../../core/models/dashboard.model';

interface DonutSegment {
  status: string;
  count: number;
  percent: number;
  color: string;
  dashArray: string;
  dashOffset: number;
}

const PALETTE = ['#1a1d23', '#2f6fed', '#22b07d', '#f5a623', '#e5484d', '#8b5cf6', '#0891b2', '#be123c', '#65a30d', '#7c3aed', '#0f766e'];
const CIRCUMFERENCE = 2 * Math.PI * 60; // r=60

@Component({
  selector: 'app-dashboard',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './dashboard.component.html',
})
export class DashboardComponent implements OnInit {
  private readonly dashboardService = inject(DashboardService);

  loading = signal(false);
  error = signal<string | null>(null);
  data = signal<Dashboard | null>(null);

  maxMonthlyCount = computed(() => Math.max(1, ...(this.data()?.applicationsByMonth.map((m) => m.count) ?? [1])));
  maxCompanyCount = computed(() => Math.max(1, ...(this.data()?.topCompanies.map((c) => c.count) ?? [1])));
  maxFunnelCount = computed(() => Math.max(1, ...(this.data()?.conversionFunnel.map((f) => f.count) ?? [1])));

  donutSegments = computed<DonutSegment[]>(() => {
    const dist = this.data()?.statusDistribution ?? [];
    const total = dist.reduce((sum, d) => sum + d.count, 0);
    if (total === 0) return [];

    let offsetSoFar = 0;
    return dist.map((d, i) => {
      const percent = (d.count / total) * 100;
      const dashLength = (percent / 100) * CIRCUMFERENCE;
      const segment: DonutSegment = {
        status: d.status,
        count: d.count,
        percent: Math.round(percent * 10) / 10,
        color: PALETTE[i % PALETTE.length],
        dashArray: `${dashLength} ${CIRCUMFERENCE - dashLength}`,
        dashOffset: -offsetSoFar,
      };
      offsetSoFar += dashLength;
      return segment;
    });
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.dashboardService.get().subscribe({
      next: (dashboard) => {
        this.data.set(dashboard);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load dashboard data.');
        this.loading.set(false);
      },
    });
  }
}
