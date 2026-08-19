import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { InterviewService } from '../../core/services/interview.service';
import { Interview } from '../../core/models/interview.model';

interface CalendarDay {
  date: Date;
  inCurrentMonth: boolean;
  isToday: boolean;
  interviews: Interview[];
}

/**
 * Spec section 22 describes a calendar showing interviews, follow-ups,
 * deadlines and tasks together. Follow-ups and tasks don't exist yet
 * (Phase 8) and job-offer deadlines aren't wired in here yet either — this
 * is interviews-only for now. The month grid and event-rendering plumbing
 * are built so adding the other event types later is additive, not a rewrite.
 */
@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calendar.component.html',
})
export class CalendarComponent implements OnInit {
  private readonly interviewService = inject(InterviewService);

  loading = signal(false);
  error = signal<string | null>(null);
  cursor = signal(this.startOfMonth(new Date()));
  interviews = signal<Interview[]>([]);
  selectedDay = signal<CalendarDay | null>(null);

  monthLabel = computed(() =>
    this.cursor().toLocaleDateString('en-US', { month: 'long', year: 'numeric' }),
  );

  days = computed<CalendarDay[]>(() => {
    const monthStart = this.cursor();
    const gridStart = new Date(monthStart);
    gridStart.setDate(gridStart.getDate() - ((gridStart.getDay() + 6) % 7)); // Monday-start grid

    const today = new Date();
    const byDay = new Map<string, Interview[]>();
    for (const interview of this.interviews()) {
      const key = new Date(interview.scheduledAt).toDateString();
      byDay.set(key, [...(byDay.get(key) ?? []), interview]);
    }

    const result: CalendarDay[] = [];
    for (let i = 0; i < 42; i++) {
      const date = new Date(gridStart);
      date.setDate(gridStart.getDate() + i);
      result.push({
        date,
        inCurrentMonth: date.getMonth() === monthStart.getMonth(),
        isToday: date.toDateString() === today.toDateString(),
        interviews: byDay.get(date.toDateString()) ?? [],
      });
    }
    return result;
  });

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    const monthStart = this.cursor();
    const rangeStart = new Date(monthStart.getFullYear(), monthStart.getMonth(), 1);
    const rangeEnd = new Date(monthStart.getFullYear(), monthStart.getMonth() + 1, 1);

    this.interviewService.calendar(rangeStart.toISOString(), rangeEnd.toISOString()).subscribe({
      next: (interviews) => {
        this.interviews.set(interviews);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load interviews for this month.');
        this.loading.set(false);
      },
    });
  }

  previousMonth(): void {
    const c = this.cursor();
    this.cursor.set(new Date(c.getFullYear(), c.getMonth() - 1, 1));
    this.selectedDay.set(null);
    this.reload();
  }

  nextMonth(): void {
    const c = this.cursor();
    this.cursor.set(new Date(c.getFullYear(), c.getMonth() + 1, 1));
    this.selectedDay.set(null);
    this.reload();
  }

  selectDay(day: CalendarDay): void {
    this.selectedDay.set(day);
  }

  private startOfMonth(date: Date): Date {
    return new Date(date.getFullYear(), date.getMonth(), 1);
  }
}
