import { Component, inject, signal } from '@angular/core';
import { FormBuilder, ReactiveFormsModule, Validators } from '@angular/forms';
import { Router } from '@angular/router';
import { MatButtonModule } from '@angular/material/button';
import { MatCardModule } from '@angular/material/card';
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatIconModule } from '@angular/material/icon';
import { MatInputModule } from '@angular/material/input';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';
import { MatSnackBar, MatSnackBarModule } from '@angular/material/snack-bar';
import { AuthService } from '../../core/auth.service';
import { errorMessage } from '../../core/http-error';

@Component({
  selector: 'app-login',
  imports: [ReactiveFormsModule, MatButtonModule, MatCardModule, MatFormFieldModule, MatIconModule, MatInputModule, MatProgressSpinnerModule, MatSnackBarModule],
  templateUrl: './login.component.html',
  styleUrl: './login.component.scss',
})
export class LoginComponent {
  private readonly fb = inject(FormBuilder);
  private readonly auth = inject(AuthService);
  private readonly router = inject(Router);
  private readonly snack = inject(MatSnackBar);
  readonly loading = signal(false);
  readonly hidePassword = signal(true);
  readonly form = this.fb.nonNullable.group({
    email: ['ana.popescu@leavehub.ro', [Validators.required, Validators.email]],
    password: ['Demo123!', Validators.required],
  });

  constructor() { if (this.auth.authenticated()) void this.router.navigate(['/dashboard']); }

  useAccount(email: string): void { this.form.setValue({ email, password: 'Demo123!' }); }

  submit(): void {
    if (this.form.invalid || this.loading()) { this.form.markAllAsTouched(); return; }
    this.loading.set(true);
    this.auth.login(this.form.getRawValue().email, this.form.getRawValue().password).subscribe({
      next: () => void this.router.navigate(['/dashboard']),
      error: error => { this.loading.set(false); this.snack.open(errorMessage(error, 'Datele de autentificare nu sunt corecte.'), 'Inchide', { duration: 5000 }); },
    });
  }
}
