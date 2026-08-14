import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatDialog } from '@angular/material/dialog';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { MatTabsModule } from '@angular/material/tabs';
import { forkJoin } from 'rxjs';
import { ApiService } from '../../core/api.service';
import { errorMessage } from '../../core/http-error';
import { Department, Employee, LeaveType } from '../../core/models';
import { DepartmentDialogComponent, EmployeeDialogComponent, LeaveTypeDialogComponent } from './admin-dialogs.component';

@Component({selector:'app-administration',imports:[MatButtonModule,MatCardModule,MatIconModule,MatProgressSpinnerModule,MatTableModule,MatTabsModule],templateUrl:'./administration.component.html',styleUrl:'./administration.component.scss'})
export class AdministrationComponent implements OnInit{
  private readonly api=inject(ApiService);private readonly dialog=inject(MatDialog);private readonly snack=inject(MatSnackBar);readonly employees=signal<Employee[]>([]);readonly departments=signal<Department[]>([]);readonly types=signal<LeaveType[]>([]);readonly loading=signal(true);readonly employeeColumns=['name','role','department','balance','active','actions'];readonly departmentColumns=['name','manager','employees','limit','actions'];readonly typeColumns=['code','name','attachment','paid','actions'];
  ngOnInit(){this.load()}load(){this.loading.set(true);forkJoin({employees:this.api.employees(),departments:this.api.departments(),types:this.api.leaveTypes()}).subscribe({next:v=>{this.employees.set(v.employees);this.departments.set(v.departments);this.types.set(v.types);this.loading.set(false)},error:e=>{this.loading.set(false);this.notify(errorMessage(e))}})}
  employee(item?:Employee){this.dialog.open(EmployeeDialogComponent,{data:{item,departments:this.departments()}}).afterClosed().subscribe(input=>{if(!input)return;const request=item?this.api.updateEmployee(item.id,input):this.api.createEmployee(input);request.subscribe({next:()=>{this.notify('Utilizatorul a fost salvat.');this.load()},error:e=>this.notify(errorMessage(e))})})}
  department(item?:Department){this.dialog.open(DepartmentDialogComponent,{data:{item,employees:this.employees()}}).afterClosed().subscribe(input=>{if(!input)return;const request=item?this.api.updateDepartment(item.id,input):this.api.createDepartment(input);request.subscribe({next:()=>{this.notify('Departamentul a fost salvat.');this.load()},error:e=>this.notify(errorMessage(e))})})}
  leaveType(item?:LeaveType){this.dialog.open(LeaveTypeDialogComponent,{data:{item}}).afterClosed().subscribe(input=>{if(!input)return;const request=item?this.api.updateLeaveType(item.id,input):this.api.createLeaveType(input);request.subscribe({next:()=>{this.notify('Tipul de concediu a fost salvat.');this.load()},error:e=>this.notify(errorMessage(e))})})}
  removeEmployee(item:Employee){if(confirm(`Stergi utilizatorul ${item.name}?`))this.api.deleteEmployee(item.id).subscribe({next:()=>this.load(),error:e=>this.notify(errorMessage(e))})}removeDepartment(item:Department){if(confirm(`Stergi departamentul ${item.name}?`))this.api.deleteDepartment(item.id).subscribe({next:()=>this.load(),error:e=>this.notify(errorMessage(e))})}removeType(item:LeaveType){if(confirm(`Stergi tipul ${item.name}?`))this.api.deleteLeaveType(item.id).subscribe({next:()=>this.load(),error:e=>this.notify(errorMessage(e))})}roleLabel(role:string){return{EMPLOYEE:'Angajat',MANAGER:'Manager',ADMIN:'Administrator'}[role]??role}private notify(m:string){this.snack.open(m,'Inchide',{duration:4500})}
}
