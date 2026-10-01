import { Component } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { FormsModule } from '@angular/forms';

@Component({
  selector: 'app-reset-password',
  imports: [FormsModule, RouterLink],
  templateUrl: './reset-password.html',
  styleUrl: './reset-password.css',
})
export class ResetPassword {
   token = '';

  newPassword = '';
  repeatedPassword = '';

  errorMessage = '';
  successMessage = '';
  loading = false;
  passwordChanged = false;

  constructor(
    private route: ActivatedRoute,
    private authService: AuthService
  ) {
  }

  ngOnInit(): void {
    this.token =
      this.route.snapshot.queryParamMap.get('token') || '';

    if(this.token === '') {
      this.errorMessage = 'Reset token is missing.';
    }
  }

  resetPassword(): void {
    if(this.token === '') {
      this.errorMessage = 'Reset token is missing.';
      return;
    }

    if(this.newPassword === '') {
      this.errorMessage = 'New password is required.';
      return;
    }

    if(this.newPassword !== this.repeatedPassword) {
      this.errorMessage = 'Passwords do not match.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.loading = true;

    this.authService.resetPassword(
      this.token,
      this.newPassword
    ).subscribe({
      next: response => {
        this.loading = false;
        this.passwordChanged = true;
        this.successMessage = response.message;

        this.newPassword = '';
        this.repeatedPassword = '';
      },
      error: err => {
        this.loading = false;
        this.errorMessage =
          err.error?.message || 'Could not reset password.';
      }
    });
  }
}
