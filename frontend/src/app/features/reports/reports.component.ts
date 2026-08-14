import { Component, inject, OnInit, signal } from '@angular/core';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar } from '@angular/material/snack-bar';
import { MatTableModule } from '@angular/material/table';
import { ApiService } from '../../core/api.service';
import { AuthService } from '../../core/auth.service';
import { errorMessage, saveBlob } from '../../core/http-error';
import { ReportSummary } from '../../core/models';

@Component({ selector:'app-reports', imports:[MatButtonModule,MatCardModule,MatIconModule,MatProgressSpinnerModule,MatTableModule], templateUrl:'./reports.component.html', styleUrl:'./reports.component.scss' })
export class ReportsComponent implements OnInit {
  private readonly api=inject(ApiService); readonly auth=inject(AuthService); private readonly snack=inject(MatSnackBar); readonly data=signal<ReportSummary|null>(null); readonly loading=signal(true); readonly downloading=signal(''); readonly columns=['department','employees','requests','days','pending'];
  ngOnInit(){this.api.reportSummary().subscribe({next:d=>{this.data.set(d);this.loading.set(false)},error:e=>{this.loading.set(false);this.snack.open(errorMessage(e),'Inchide')}})}
  download(path:string,fileName:string){this.downloading.set(path);this.api.downloadPdf(path).subscribe({next:r=>{this.downloading.set('');if(r.body)saveBlob(r.body,fileName)},error:e=>{this.downloading.set('');this.snack.open(errorMessage(e),'Inchide')}})}
}
