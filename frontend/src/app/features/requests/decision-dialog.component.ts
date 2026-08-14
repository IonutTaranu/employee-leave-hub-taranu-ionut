import { Component, inject, signal } from '@angular/core';
import { FormControl, ReactiveFormsModule, Validators } from '@angular/forms';
import { MAT_DIALOG_DATA, MatDialogModule, MatDialogRef } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { ApiService } from '../../core/api.service';
import { errorMessage } from '../../core/http-error';

@Component({ selector:'app-decision-dialog', imports:[ReactiveFormsModule,MatDialogModule,MatButtonModule,MatFormFieldModule,MatIconModule,MatInputModule,MatProgressSpinnerModule], template:`
  <h2 mat-dialog-title>{{ data.decision === 'APPROVED' ? 'Aproba cererea' : 'Respinge cererea' }}</h2>
  <mat-dialog-content><div class="decision-summary"><mat-icon>{{data.decision==='APPROVED'?'check_circle':'cancel'}}</mat-icon><span><strong>{{data.employee}}</strong><small>{{data.period}}</small></span></div>
  <mat-form-field appearance="outline"><mat-label>{{data.decision==='REJECTED'?'Motivul respingerii':'Comentariu (optional)'}}</mat-label><textarea matInput rows="4" [formControl]="comment"></textarea></mat-form-field>
  @if(error()){<p class="error">{{error()}}</p>}</mat-dialog-content>
  <mat-dialog-actions align="end"><button mat-button mat-dialog-close>Renunta</button><button mat-flat-button [class.approve]="data.decision==='APPROVED'" [class.reject]="data.decision==='REJECTED'" (click)="submit()" [disabled]="loading()">@if(loading()){<mat-spinner diameter="20"/>}@else{Confirma decizia}</button></mat-dialog-actions>`, styles:[`mat-dialog-content{width:min(440px,75vw)}.decision-summary{display:flex;gap:10px;align-items:center;padding:12px;background:#f4f8f9;border-radius:10px;margin-bottom:16px}.decision-summary>span{display:grid}.decision-summary small{color:#7c8a90;margin-top:3px}mat-form-field{width:100%}.approve{background:#278b59;color:white}.reject{background:#b94e49;color:white}.error{color:#b4433e;font-size:.76rem}`] })
export class DecisionDialogComponent {
  readonly data=inject<{id:number;decision:'APPROVED'|'REJECTED';employee:string;period:string}>(MAT_DIALOG_DATA); private readonly api=inject(ApiService); private readonly ref=inject(MatDialogRef<DecisionDialogComponent>);
  readonly comment=new FormControl('',this.data.decision==='REJECTED'?Validators.required:[]); readonly loading=signal(false); readonly error=signal('');
  submit(){if(this.comment.invalid){this.comment.markAsTouched();return}this.loading.set(true);this.api.decideRequest(this.data.id,this.data.decision,this.comment.value??'').subscribe({next:r=>this.ref.close(r),error:e=>{this.loading.set(false);this.error.set(errorMessage(e))}})}
}
