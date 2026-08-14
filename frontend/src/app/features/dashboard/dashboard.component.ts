import { Component, inject, OnInit, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressBarModule } from '@angular/material/progress-bar';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatTableModule } from '@angular/material/table';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { Dashboard, LeaveStatus } from '../../core/models';
import { formatDate, STATUS_LABELS } from '../../shared/status';

@Component({
  selector: 'app-dashboard',
  imports: [RouterLink, MatButtonModule, MatCardModule, MatIconModule, MatProgressBarModule, MatProgressSpinnerModule, MatTableModule],
  templateUrl: './dashboard.component.html',
  styleUrl: './dashboard.component.scss',
})
export class DashboardComponent implements OnInit {
  private readonly api = inject(ApiService);
  readonly auth = inject(AuthService);
  readonly data = signal<Dashboard | null>(null);
  readonly loading = signal(true);
  readonly columns = ['type', 'period', 'days', 'status'];
  readonly statusLabels = STATUS_LABELS;
  readonly formatDate = formatDate;

  ngOnInit(): void { this.load(); }
  load(): void { this.loading.set(true); this.api.dashboard().subscribe({ next: data => { this.data.set(data); this.loading.set(false); }, error: () => this.loading.set(false) }); }
  percentage(consumed: number, annual: number): number { return annual ? Math.round(consumed / annual * 100) : 0; }
  statusClass(status: LeaveStatus): string { return `status status-${status.toLowerCase()}`; }
  statusLabel(status: LeaveStatus): string { return STATUS_LABELS[status]; }
  greeting(): string { const hour = new Date().getHours(); return hour < 12 ? 'Buna dimineata' : hour < 18 ? 'Buna ziua' : 'Buna seara'; }
}
