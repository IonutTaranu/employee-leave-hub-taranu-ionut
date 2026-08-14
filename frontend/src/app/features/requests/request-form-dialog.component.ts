import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { concatMap, Observable, of } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { errorMessage } from '../../core/http-error';
import { LeaveRequest, LeaveType } from '../../core/models';

@Component({
  selector: 'app-request-form-dialog',
  imports: [ReactiveFormsModule, MatDialogModule, MatButtonModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule, MatSelectModule],
  template: `
    <div class="dialog-head"><span class="head-icon"><mat-icon>event_available</mat-icon></span><div><h2 mat-dialog-title>{{ data.request ? 'Modifica cererea' : 'Cerere noua de concediu' }}</h2><p>Completeaza perioada si tipul de concediu.</p></div></div>
    <mat-dialog-content>
      <form [formGroup]="form" class="request-form">
        <mat-form-field appearance="outline" class="full"><mat-label>Tip concediu</mat-label><mat-select formControlName="leaveTypeId">
          @for (type of types(); track type.id) { <mat-option [value]="type.id">{{ type.name }} ({{ type.code }})</mat-option> }
        </mat-select><mat-icon matPrefix>category</mat-icon></mat-form-field>
        <div class="date-row">
          <mat-form-field appearance="outline"><mat-label>Prima zi</mat-label><input matInput type="date" formControlName="startDate" /><mat-icon matPrefix>event</mat-icon></mat-form-field>
          <mat-form-field appearance="outline"><mat-label>Ultima zi</mat-label><input matInput type="date" formControlName="endDate" /><mat-icon matPrefix>event</mat-icon></mat-form-field>
        </div>
        <mat-form-field appearance="outline" class="full"><mat-label>Motiv / observatii</mat-label><textarea matInput rows="3" formControlName="reason" maxlength="800"></textarea><mat-hint align="end">{{ form.controls.reason.value.length }}/800</mat-hint></mat-form-field>
        <label class="upload-box" [class.has-file]="file()">
          <input type="file" accept=".pdf,.jpg,.jpeg,.png" (change)="chooseFile($event)" />
          <mat-icon>{{ file() ? 'task_alt' : 'cloud_upload' }}</mat-icon><span><strong>{{ file()?.name || 'Adauga document justificativ' }}</strong><small>PDF, JPG sau PNG · maximum 10 MB</small></span>
        </label>
        @if (selectedType()?.requiresAttachment) { <div class="info-note"><mat-icon>info</mat-icon>Acest tip de concediu necesita obligatoriu un document.</div> }
        @if (error()) { <div class="error-note"><mat-icon>error_outline</mat-icon>{{ error() }}</div> }
      </form>
    </mat-dialog-content>
    <mat-dialog-actions align="end">
      <button mat-button mat-dialog-close [disabled]="saving()">Renunta</button>
      <button mat-stroked-button (click)="save(false)" [disabled]="saving()">Salveaza draft</button>
      <button mat-flat-button class="submit" (click)="save(true)" [disabled]="saving()">@if(saving()){<mat-spinner diameter="20"/>}@else{<span>Trimite spre aprobare →</span>}</button>
    </mat-dialog-actions>
  `,
  styles: [`
    .dialog-head{display:flex;gap:14px;align-items:center;padding:24px 24px 4px}.head-icon{width:48px;height:48px;display:grid;place-items:center;border-radius:14px;background:#e7f7f9;color:#0a91a6}.dialog-head h2{padding:0;margin:0;color:#193540;font-size:1.3rem}.dialog-head p{margin:4px 0 0;color:#7d8b90;font-size:.78rem}mat-dialog-content{padding-top:18px!important;width:min(620px,80vw)}.request-form{display:grid}.full{width:100%}.date-row{display:grid;grid-template-columns:1fr 1fr;gap:12px}.upload-box{display:flex;align-items:center;gap:13px;padding:15px;border:1px dashed #a9c1c7;border-radius:12px;background:#f7fbfc;cursor:pointer;color:#58727b;margin-bottom:12px}.upload-box input{display:none}.upload-box>mat-icon{color:#159bb0}.upload-box span{display:grid}.upload-box strong{font-size:.78rem;color:#35505a}.upload-box small{font-size:.68rem;margin-top:2px}.upload-box.has-file{border-color:#5bb984;background:#f0faf4}.info-note,.error-note{display:flex;align-items:center;gap:8px;border-radius:9px;padding:10px;font-size:.73rem}.info-note{background:#edf7fa;color:#347381}.error-note{background:#fff0ef;color:#aa4540;margin-top:8px}.info-note mat-icon,.error-note mat-icon{font-size:18px}.submit{background:#098fa5;color:white;min-width:180px}@media(max-width:620px){.date-row{grid-template-columns:1fr}mat-dialog-content{width:auto}}
  `],
})
export class RequestFormDialogComponent {
  readonly data = inject<{ request?: LeaveRequest }>(MAT_DIALOG_DATA);
  private readonly ref = inject(MatDialogRef<RequestFormDialogComponent>);
  private readonly fb = inject(FormBuilder);
  private readonly api = inject(ApiService);
  readonly types = signal<LeaveType[]>([]);
  readonly file = signal<File | null>(null);
  readonly saving = signal(false);
  readonly error = signal('');
  readonly form = this.fb.nonNullable.group({
    leaveTypeId: [this.data.request?.leaveTypeId ?? 0, Validators.min(1)],
    startDate: [this.data.request?.startDate ?? '', Validators.required],
    endDate: [this.data.request?.endDate ?? '', Validators.required],
    reason: [this.data.request?.reason ?? ''],
  });

  constructor() { this.api.leaveTypes().subscribe(types => this.types.set(types)); }
  selectedType(): LeaveType | undefined { return this.types().find(type => type.id === this.form.controls.leaveTypeId.value); }
  chooseFile(event: Event): void { this.file.set((event.target as HTMLInputElement).files?.[0] ?? null); }

  save(submit: boolean): void {
    if (this.form.invalid || this.saving()) { this.form.markAllAsTouched(); return; }
    if (this.form.controls.endDate.value < this.form.controls.startDate.value) { this.error.set('Data de final trebuie sa fie dupa data de inceput.'); return; }
    this.saving.set(true); this.error.set('');
    const input = this.form.getRawValue();
    let action: Observable<LeaveRequest> = this.data.request ? this.api.updateRequest(this.data.request.id, input) : this.api.createRequest(input);
    action = action.pipe(concatMap(request => this.file() ? this.api.uploadAttachment(request.id, this.file()!).pipe(concatMap(() => of(request))) : of(request)));
    if (submit) action = action.pipe(concatMap(request => this.api.submitRequest(request.id)));
    action.subscribe({ next: request => this.ref.close(request), error: err => { this.saving.set(false); this.error.set(errorMessage(err)); } });
  }
}
