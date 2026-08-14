import { Component, inject, OnInit, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ActivatedRoute } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog, MatDialogModule } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatMenuModule } from '@angular/material/menu';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/http-error';
import { Department, LeaveRequest, LeaveStatus, LeaveType } from '../../core/models';
import { formatDate, STATUS_LABELS } from '../../shared/status';
import { DecisionDialogComponent } from './decision-dialog.component';
import { RequestDetailDialogComponent } from './request-detail-dialog.component';
import { RequestFormDialogComponent } from './request-form-dialog.component';

@Component({ selector:'app-requests', imports:[ReactiveFormsModule,MatButtonModule,MatCardModule,MatDialogModule,MatFormFieldModule,MatIconModule,MatInputModule,MatMenuModule,MatProgressSpinnerModule,MatSelectModule,MatSnackBarModule,MatTableModule], templateUrl:'./requests.component.html', styleUrl:'./requests.component.scss' })
export class RequestsComponent implements OnInit {
  private readonly api=inject(ApiService); readonly auth=inject(AuthService); private readonly dialog=inject(MatDialog); private readonly snack=inject(MatSnackBar); private readonly fb=inject(FormBuilder); private readonly route=inject(ActivatedRoute);
  readonly approvals=this.route.snapshot.data['approvals']===true; readonly requests=signal<LeaveRequest[]>([]); readonly departments=signal<Department[]>([]); readonly types=signal<LeaveType[]>([]); readonly loading=signal(true); readonly labels=STATUS_LABELS; readonly date=formatDate; readonly columns=['employee','type','period','days','status','actions'];
  readonly statuses: LeaveStatus[]=['DRAFT','PENDING','APPROVED','REJECTED','CANCELLED'];
  readonly filter=this.fb.group({status:[this.approvals?'PENDING':'' as LeaveStatus|''],departmentId:[null as number|null],leaveTypeId:[null as number|null],employee:[''],from:[''],to:['']});
  ngOnInit(){forkJoin({departments:this.api.departments(),types:this.api.leaveTypes()}).subscribe(v=>{this.departments.set(v.departments);this.types.set(v.types)});this.load()}
  load(){this.loading.set(true);const value=this.filter.getRawValue();this.api.requests({status:value.status??'',departmentId:value.departmentId,leaveTypeId:value.leaveTypeId,employee:value.employee??'',from:value.from||undefined,to:value.to||undefined}).subscribe({next:r=>{this.requests.set(r);this.loading.set(false)},error:e=>{this.loading.set(false);this.notify(errorMessage(e))}})}
  reset(){this.filter.reset({status:this.approvals?'PENDING':'',departmentId:null,leaveTypeId:null,employee:'',from:'',to:''});this.load()}
  create(){this.dialog.open(RequestFormDialogComponent,{data:{},width:'660px',maxWidth:'95vw'}).afterClosed().subscribe(r=>{if(r){this.notify('Cererea a fost salvata.');this.load()}})}
  edit(request:LeaveRequest){this.dialog.open(RequestFormDialogComponent,{data:{request},width:'660px',maxWidth:'95vw'}).afterClosed().subscribe(r=>{if(r)this.load()})}
  view(request:LeaveRequest){this.dialog.open(RequestDetailDialogComponent,{data:request,width:'740px',maxWidth:'96vw'})}
  submit(request:LeaveRequest){this.api.submitRequest(request.id).subscribe({next:()=>{this.notify('Cererea a fost trimisa spre aprobare.');this.load()},error:e=>this.notify(errorMessage(e))})}
  cancel(request:LeaveRequest){if(!confirm('Sigur vrei sa anulezi aceasta cerere?'))return;this.api.cancelRequest(request.id).subscribe({next:()=>{this.notify('Cererea a fost anulata.');this.load()},error:e=>this.notify(errorMessage(e))})}
  remove(request:LeaveRequest){if(!confirm('Stergi definitiv aceasta ciorna?'))return;this.api.deleteRequest(request.id).subscribe({next:()=>this.load(),error:e=>this.notify(errorMessage(e))})}
  decide(request:LeaveRequest,decision:'APPROVED'|'REJECTED'){this.dialog.open(DecisionDialogComponent,{data:{id:request.id,decision,employee:request.employeeName,period:`${this.date(request.startDate)} – ${this.date(request.endDate)}`}}).afterClosed().subscribe(r=>{if(r){this.notify(decision==='APPROVED'?'Cererea a fost aprobata.':'Cererea a fost respinsa.');this.load()}})}
  own(request:LeaveRequest){return request.employeeId===this.auth.user()?.id} statusClass(s:LeaveStatus){return`status status-${s.toLowerCase()}`} statusLabel(s:LeaveStatus){return STATUS_LABELS[s]} private notify(message:string){this.snack.open(message,'Inchide',{duration:4000})}
}
