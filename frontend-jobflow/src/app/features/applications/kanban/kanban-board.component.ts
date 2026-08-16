import { CommonModule } from '@angular/common';
import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { CdkDragDrop, DragDropModule, moveItemInArray, transferArrayItem } from '@angular/cdk/drag-drop';
import { ApplicationService } from '../../../core/services/application.service';
import { Application, ApplicationStatus } from '../../../core/models/application.model';

interface KanbanColumn {
  status: ApplicationStatus;
  label: string;
  items: Application[];
}

/**
 * Spec section 9 lists 8 Kanban columns. The full ApplicationStatus enum has
 * 11 values — WISHLIST, FINAL_INTERVIEW and WITHDRAWN are intentionally not
 * columns here (per the spec's literal column list). Applications sitting in
 * one of those three statuses simply won't appear on this board; they're
 * still visible and editable from the table view. Flagging this rather than
 * quietly hiding it.
 */
const COLUMNS: { status: ApplicationStatus; label: string }[] = [
  { status: 'TO_APPLY', label: 'À postuler' },
  { status: 'APPLIED', label: 'Postulé' },
  { status: 'SCREENING', label: 'Screening' },
  { status: 'INTERVIEW', label: 'Entretien' },
  { status: 'TECHNICAL_INTERVIEW', label: 'Entretien technique' },
  { status: 'OFFER', label: 'Offre' },
  { status: 'ACCEPTED', label: 'Accepté' },
  { status: 'REJECTED', label: 'Refusé' },
];

@Component({
  selector: 'app-kanban-board',
  standalone: true,
  imports: [CommonModule, DragDropModule],
  templateUrl: './kanban-board.component.html',
})
export class KanbanBoardComponent implements OnInit {
  private readonly applicationService = inject(ApplicationService);

  loading = signal(false);
  error = signal<string | null>(null);

  private readonly applications = signal<Application[]>([]);

  columns = computed<KanbanColumn[]>(() =>
    COLUMNS.map((col) => ({
      ...col,
      items: this.applications().filter((a) => a.status === col.status),
    })),
  );

  readonly connectedListIds = COLUMNS.map((c) => c.status);

  ngOnInit(): void {
    this.reload();
  }

  reload(): void {
    this.loading.set(true);
    this.error.set(null);
    this.applicationService.list({ size: 200 }).subscribe({
      next: (page) => {
        this.applications.set(page.content);
        this.loading.set(false);
      },
      error: () => {
        this.error.set('Unable to load applications.');
        this.loading.set(false);
      },
    });
  }

  drop(event: CdkDragDrop<Application[]>, targetStatus: ApplicationStatus): void {
    if (event.previousContainer === event.container) {
      // Reordering within the same column is cosmetic only — nothing to persist.
      moveItemInArray(event.container.data, event.previousIndex, event.currentIndex);
      return;
    }

    const application = event.previousContainer.data[event.previousIndex];
    const previousStatus = application.status;

    // Optimistic move: update local state immediately so the drag feels instant,
    // then confirm with the backend and roll back on failure.
    transferArrayItem(event.previousContainer.data, event.container.data, event.previousIndex, event.currentIndex);
    this.applications.update((apps) =>
      apps.map((a) => (a.id === application.id ? { ...a, status: targetStatus } : a)),
    );

    this.applicationService.updateStatus(application.id, targetStatus).subscribe({
      error: () => {
        this.error.set('Could not save the status change — reverting.');
        this.applications.update((apps) =>
          apps.map((a) => (a.id === application.id ? { ...a, status: previousStatus } : a)),
        );
      },
    });
  }
}
