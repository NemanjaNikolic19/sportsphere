import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { AppUser } from '../../../models/app-user';
import { ProfileUpdateRequest } from '../../../models/profile-update-request';
import { AuthService } from '../../../services/auth.service';
import { EmployeeService } from '../../../services/employee.service';

@Component({
  selector: 'app-employee-profile',
  imports: [FormsModule, RouterLink],
  templateUrl: './employee-profile.html',
  styleUrl: './employee-profile.css'
})
export class EmployeeProfile implements OnInit {

  employeeId: number | null = null;

  profile: AppUser | null = null;

  editProfile: ProfileUpdateRequest = {
    firstName: '',
    lastName: '',
    phone: null,
    email: ''
  };

  loading = false;
  saving = false;

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private employeeService: EmployeeService
  ) {
  }

  ngOnInit(): void {
    const currentUser = this.authService.currentUser();

    if(!currentUser) {
      this.errorMessage = 'You must be logged in.';
      return;
    }

    if(currentUser.role !== 'EMPLOYEE') {
      this.errorMessage = 'Only employees can access this page.';
      return;
    }

    this.employeeId = currentUser.id;

    this.loadProfile();
  }

  loadProfile(): void {
    if(this.employeeId === null) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.employeeService.getProfile(this.employeeId).subscribe({
      next: profile => {
        this.profile = profile;

        this.editProfile = {
          firstName: profile.firstName,
          lastName: profile.lastName,
          phone: profile.phone,
          email: profile.email
        };

        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Could not load profile.';
      }
    });
  }

  saveProfile(): void {
    if(this.employeeId === null) {
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.employeeService.updateProfile(this.employeeId, this.editProfile).subscribe({
      next: updatedProfile => {
        this.profile = updatedProfile;
        this.saving = false;
        this.successMessage = 'Profile updated.';
      },
      error: err => {
        this.saving = false;
        this.errorMessage = err.error?.message || 'Could not update profile.';
      }
    });
  }

  profileImageUrl(): string {
    const path = this.profile?.profileImagePath;

    if(!path) {
      return '/api/profile-images/default.png';
    }

    if(path.startsWith('/api/profile-images/')) {
      return path;
    }

    return `/api/profile-images/${path}`;
  }
}
