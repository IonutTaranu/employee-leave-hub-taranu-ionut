import { Component, inject, signal } from '@angular/core';
import { MAT_DIALOG_DATA, MatDialogModule } from '@angular/material/dialog';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatTabsModule } from '@angular/material/tabs';
import { ApiService } from '../../core/api.service';
import { saveBlob } from '../../core/http-error';
import { LeaveRequest, LeaveStatus } from '../../core/models';
import { formatDate, STATUS_LABELS } from '../../shared/status';

@Component({
  selector: 'app-request-detail-dialog',
  imports: [MatDialogModule, MatButtonModule, MatIconModule, MatTabsModule],
  template: `
    <div class="detail-head"><div><span>CERERE #{{data.id}}</span><h2 mat-dialog-title>{{data.leaveTypeName}}</h2><p>{{data.employeeName}} · {{data.departmentName}}</p></div><span [class]="statusClass(data.status)">{{labels[data.status]}}</span></div>
    <mat-dialog-content>
      <div class="summary-grid"><div><mat-icon>date_range</mat-icon><span>Perioada<strong>{{date(data.startDate)}} – {{date(data.endDate)}}</strong></span></div><div><mat-icon>work_history</mat-icon><span>Zile lucratoare<strong>{{data.workingDays}} zile</strong></span></div><div><mat-icon>category</mat-icon><span>Tip concediu<strong>{{data.leaveTypeCode}}</strong></span></div></div>
      <mat-tab-group animationDuration="150ms">
        <mat-tab label="Detalii"><div class="tab-content"><h4>Motiv / observatii</h4><p>{{data.reason || 'Nu au fost adaugate observatii.'}}</p><div class="created"><mat-icon>schedule</mat-icon>Creata la {{dateTime(data.createdAt)}}</div></div></mat-tab>
        <mat-tab label="Istoric ({{data.workflow.length}})"><div class="tab-content timeline">@for(entry of data.workflow;track entry.id){<div class="timeline-item"><span class="dot"></span><div><strong>{{labels[entry.currentStatus]}}</strong><small>{{entry.changedByName}} · {{dateTime(entry.changedAt)}}</small>@if(entry.comment){<p>{{entry.comment}}</p>}</div></div>}</div></mat-tab>
        <mat-tab label="Documente ({{data.attachments.length}})"><div class="tab-content documents">@for(file of data.attachments;track file.id){<button (click)="downloadAttachment(file.id,file.fileName)"><mat-icon>{{file.contentType==='application/pdf'?'picture_as_pdf':'image'}}</mat-icon><span><strong>{{file.fileName}}</strong><small>{{(file.fileSize/1024).toFixed(1)}} KB</small></span><mat-icon>download</mat-icon></button>}@empty{<div class="empty"><mat-icon>folder_off</mat-icon>Nu exista documente atasate.</div>}</div></mat-tab>
      </mat-tab-group>
    </mat-dialog-content>
    <mat-dialog-actions><button mat-button (click)="downloadPdf()"><mat-icon>picture_as_pdf</mat-icon>Descarca cererea PDF</button><span class="spacer"></span><button mat-flat-button mat-dialog-close>Inchide</button></mat-dialog-actions>
  `,
  styles: [`
    .detail-head{display:flex;justify-content:space-between;align-items:flex-start;padding:24px 24px 10px;gap:25px}.detail-head>div>span{color:#0c94a8;font-size:.65rem;font-weight:700;letter-spacing:.1em}.detail-head h2{padding:0;margin:4px 0 2px;font-size:1.35rem;color:#173540}.detail-head p{margin:0;color:#798a90;font-size:.76rem}.status{padding:6px 10px;border-radius:20px;font-size:.66rem;font-weight:700}.status-approved{background:#e7f6ed;color:#278153}.status-pending{background:#fff2df;color:#b47117}.status-rejected{background:#fdebea;color:#b74944}.status-draft{background:#eef2f3;color:#64757b}.status-cancelled{background:#efedf4;color:#746884}mat-dialog-content{width:min(680px,80vw)}.summary-grid{display:grid;grid-template-columns:1.7fr 1fr .8fr;gap:10px;margin:6px 0 20px}.summary-grid>div{display:flex;align-items:center;gap:9px;padding:12px;background:#f4f8f9;border-radius:10px}.summary-grid mat-icon{color:#1698ac}.summary-grid span{display:grid;font-size:.66rem;color:#7f8f94}.summary-grid strong{font-size:.74rem;color:#314d57;margin-top:2px}.tab-content{padding:20px 3px 8px;min-height:130px}.tab-content h4{margin:0 0 7px;color:#304c56}.tab-content>p{color:#65777e;line-height:1.6}.created{display:flex;gap:6px;align-items:center;color:#8a989d;font-size:.68rem;margin-top:24px}.created mat-icon{font-size:17px}.timeline{padding-left:10px}.timeline-item{display:flex;gap:13px;position:relative;padding-bottom:18px}.timeline-item:not(:last-child)::before{content:'';position:absolute;left:5px;top:13px;bottom:0;border-left:1px solid #cbd9dc}.dot{width:11px;height:11px;background:#16a1b6;border:3px solid #dff5f8;border-radius:50%;z-index:1}.timeline-item div{display:grid}.timeline-item strong{font-size:.76rem;color:#314d57}.timeline-item small{font-size:.65rem;color:#849297;margin-top:2px}.timeline-item p{font-size:.72rem;margin:7px 0 0;padding:8px;background:#f5f8f9;border-radius:7px;color:#61747b}.documents{display:grid;gap:8px}.documents button{display:flex;align-items:center;gap:10px;padding:11px;border:1px solid #e1e9eb;border-radius:10px;background:white;cursor:pointer;text-align:left}.documents button>span{display:grid;flex:1}.documents strong{font-size:.73rem;color:#314b55}.documents small{font-size:.65rem;color:#88959a}.documents button>mat-icon:first-child{color:#0e96ab}.documents button>mat-icon:last-child{color:#8d9b9f}.empty{display:grid;place-items:center;gap:6px;color:#87959a;padding:25px}.spacer{flex:1}@media(max-width:650px){.summary-grid{grid-template-columns:1fr}.detail-head{padding-inline:16px}mat-dialog-content{width:auto}}
  `],
})
export class RequestDetailDialogComponent {
  readonly data=inject<LeaveRequest>(MAT_DIALOG_DATA); private readonly api=inject(ApiService); readonly labels=STATUS_LABELS; readonly downloading=signal(false);
  date=formatDate; dateTime(value:string){return new Intl.DateTimeFormat('ro-RO',{dateStyle:'medium',timeStyle:'short'}).format(new Date(value))} statusClass(status:LeaveStatus){return`status status-${status.toLowerCase()}`}
  downloadPdf(){this.api.downloadPdf(`/leave-requests/${this.data.id}/pdf`).subscribe(response=>{if(response.body)saveBlob(response.body,`cerere-concediu-${this.data.id}.pdf`)})}
  downloadAttachment(id:number,name:string){this.api.downloadAttachment(id).subscribe(response=>{if(response.body)saveBlob(response.body,name)})}
}
