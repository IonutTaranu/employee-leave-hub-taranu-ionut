import { Component, computed, inject, OnInit, signal } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSelectModule } from '@angular/material/select';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { CalendarEvent, Department } from '../../core/models';
import { RequestDetailDialogComponent } from '../requests/request-detail-dialog.component';

interface CalendarDay { date: Date; iso: string; current: boolean; today: boolean; }

@Component({ selector:'app-calendar', imports:[FormsModule,MatButtonModule,MatCardModule,MatFormFieldModule,MatIconModule,MatProgressSpinnerModule,MatSelectModule], templateUrl:'./calendar.component.html', styleUrl:'./calendar.component.scss' })
export class CalendarComponent implements OnInit {
  private readonly api=inject(ApiService); readonly auth=inject(AuthService); private readonly dialog=inject(MatDialog);
  readonly month=signal(new Date(new Date().getFullYear(),new Date().getMonth(),1)); readonly events=signal<CalendarEvent[]>([]); readonly departments=signal<Department[]>([]); readonly selectedDepartment=signal<number|null>(null); readonly loading=signal(true);
  readonly monthLabel=computed(()=>new Intl.DateTimeFormat('ro-RO',{month:'long',year:'numeric'}).format(this.month()));
  readonly days=computed<CalendarDay[]>(()=>{const value=this.month();const first=new Date(value.getFullYear(),value.getMonth(),1);const mondayOffset=(first.getDay()+6)%7;const start=new Date(value.getFullYear(),value.getMonth(),1-mondayOffset);return Array.from({length:42},(_,index)=>{const date=new Date(start);date.setDate(start.getDate()+index);const iso=this.iso(date);return{date,iso,current:date.getMonth()===value.getMonth(),today:iso===this.iso(new Date())}})});
  readonly warningCount=computed(()=>new Set(this.events().filter(event=>event.overlapWarning).map(event=>event.requestId)).size);
  ngOnInit(){this.selectedDepartment.set(this.auth.hasRole('ADMIN')?null:this.auth.user()?.departmentId??null);forkJoin({departments:this.api.departments()}).subscribe(v=>{this.departments.set(v.departments);this.load()})}
  previous(){const d=this.month();this.month.set(new Date(d.getFullYear(),d.getMonth()-1,1));this.load()} next(){const d=this.month();this.month.set(new Date(d.getFullYear(),d.getMonth()+1,1));this.load()} today(){const d=new Date();this.month.set(new Date(d.getFullYear(),d.getMonth(),1));this.load()}
  load(){this.loading.set(true);const d=this.month();const from=this.iso(new Date(d.getFullYear(),d.getMonth(),1));const to=this.iso(new Date(d.getFullYear(),d.getMonth()+1,0));this.api.calendar(this.selectedDepartment(),from,to).subscribe({next:e=>{this.events.set(e);this.loading.set(false)},error:()=>this.loading.set(false)})}
  eventsFor(day:CalendarDay){return this.events().filter(event=>event.startDate<=day.iso&&event.endDate>=day.iso)}
  view(event:CalendarEvent){this.api.request(event.requestId).subscribe(request=>this.dialog.open(RequestDetailDialogComponent,{data:request,width:'740px',maxWidth:'96vw'}))}
  private iso(date:Date){return `${date.getFullYear()}-${String(date.getMonth()+1).padStart(2,'0')}-${String(date.getDate()).padStart(2,'0')}`}
}
