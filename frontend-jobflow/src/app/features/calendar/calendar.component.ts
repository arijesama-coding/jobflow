import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { forkJoin } from 'rxjs';
import { InterviewService } from '../../core/services/interview.service';
import { FollowUpService } from '../../core/services/follow-up.service';
import { TaskService } from '../../core/services/task.service';

type CalendarEventType = 'interview' | 'followup' | 'task';

interface CalendarEvent {
  type: CalendarEventType;
  label: string;
  time?: string;
  date: Date;
}

interface CalendarDay {
  date: Date;
  inCurrentMonth: boolean;
  isToday: boolean;
  events: CalendarEvent[];
}

/**
 * Spec section 22 wants interviews, follow-ups, deadlines and tasks on one
 * calendar. Interviews, follow-ups and tasks are wired in now that all three
 * modules exist (Phases 7-8). Job-offer deadlines aren't included yet — that
 * needs a dedicated date-range endpoint on the jobs API, which doesn't exist.
 * Flagging rather than pretending this calendar is spec-complete.
 */
@Component({
  selector: 'app-calendar',
  standalone: true,
  imports: [CommonModule],
  templateUrl: './calendar.component.html',
})
export class CalendarComponent implements OnInit {
  private readonly interviewService = inject(InterviewService);
  private readonly followUpService = inject(FollowUpService);
  private readonly taskService = inject(TaskService);

  loading = signal(false);
  error = signal<string | null>(null);
  cursor = signal(this.startOfMonth(new Date()));
  events = signal<CalendarEvent[]>([]);
  selectedDay = signal<CalendarDay | null>(null);

  monthLabel = computed(() =>
    this.cursor().toLocaleDateString('en-US', { month: 'long', year: 'numeric' }),
  );

  days = computed<CalendarDay[]>(() => {
    const monthStart = this.cursor();
    const gridStart = new Date(monthStart);
    gridStart.setDate(gridStart.getDate() - ((gridStart.getDay() + 6) % 7)); // Monday-start grid

    const today = new Date();
    const byDay = new Map<string, CalendarEvent[]>();
    for (const event of this.events()) {
      const key = event.date.toDateString();
      byDay.set(key, [...(byDay.get(key) ?? []), event]);
    }

    const result: CalendarDay[] = [];
    for (let i = 0; i < 42; i++) {
      const date = new Date(gridStart);
      date.setDate(gridStart.getDate() + i);
      result.push({
        date,
        inCurrentMonth: date.getMonth() === monthStart.getMonth(),
        isToday: date.toDateString() === today.toDateString(),
        events: byDay.get(date.toDateString()) ?? [],
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
    const isoStart = rangeStart.toISOString();
    const isoEnd = rangeEnd.toISOString();
    const dateStart = isoStart.slice(0, 10);
    const dateEnd = isoEnd.slice(0, 10);

    forkJoin({
      interviews: this.interviewService.calendar(isoStart, isoEnd),
      followUps: this.followUpService.calendar(dateStart, dateEnd),
      tasks: this.taskService.calendar(dateStart, dateEnd),
    }).subscribe({
      next: ({ interviews, followUps, tasks }) => {
        const events: CalendarEvent[] = [
          ...interviews.map((i) => ({
            type: 'interview' as const,
            label: `${i.jobOfferTitle || i.companyName || 'Interview'} (${i.type})`,
            time: new Date(i.scheduledAt).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
            date: new Date(i.scheduledAt),
          })),
          ...followUps.map((f) => ({
            type: 'followup' as const,
            label: `Follow-up: ${f.jobOfferTitle || f.companyName || 'application'} (${f.type})`,
            date: new Date(f.followUpDate),
          })),
          ...tasks
            .filter((t) => !!t.dueDate)
            .map((t) => ({
              type: 'task' as const,
              label: `Task: ${t.title}`,
              date: new Date(t.dueDate as string),
            })),
        ];
        this.events.set(events);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load calendar data for this month.');
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
