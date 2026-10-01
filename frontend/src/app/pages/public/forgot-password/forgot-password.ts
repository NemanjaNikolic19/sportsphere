import { Component } from '@angular/core';
import { AuthService } from '../../../services/auth.service';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-forgot-password',
  imports: [FormsModule, RouterLink],
  templateUrl: './forgot-password.html',
  styleUrl: './forgot-password.css',
})
export class ForgotPassword {

  identifier = '';

  resetLink = '';
  errorMessage = '';
  successMessage = '';
  loading = false;

  constructor(private authService: AuthService) {
  }

  createResetLink(): void {
    const identifier = this.identifier.trim();

    if(identifier === '') {
      this.errorMessage = 'Username or email is required.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.resetLink = '';
    this.loading = true;

    this.authService.forgotPassword(identifier).subscribe({
      next: response => {
        this.loading = false;
        this.successMessage = response.message;
        this.resetLink = response.resetLink;
      },
      error: err => {
        this.loading = false;
        this.errorMessage =
          err.error?.message || 'Could not create reset link.';
      }
    });
  }
}
