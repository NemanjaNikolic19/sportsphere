import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AppUser } from '../../../models/app-user';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-pending-users',
  imports: [RouterLink],
  templateUrl: './admin-pending-users.html',
  styleUrl: './admin-pending-users.css',
})
export class AdminPendingUsers {
  
  users: AppUser[] = [];

  loading = false;
  processingUserId: number | null = null;

  errorMessage = '';
  successMessage = '';

  constructor(private adminService: AdminService){
  }

  ngOnInit(): void {
    this.loadPendingUsers();
  }

  loadPendingUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminService.getPendingUsers().subscribe({
      next: users => {
        this.users = users;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Could not load pending users.';
      }
    });
  }

  approve(user: AppUser): void {
    this.processingUserId = user.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminService.approveUser(user.id).subscribe({
      next: approvedUser => {
        this.users = this.users.filter(u => u.id !== approvedUser.id);
        this.processingUserId = null;
        this.successMessage = `User ${approvedUser.username} approved`;
      },
      error: err => {
        this.processingUserId = null;
        this.errorMessage = err.error?.message || 'Could not approve user.';
      }
    });
  }

  reject(user: AppUser): void {
    this.processingUserId = user.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminService.rejectUser(user.id).subscribe({
      next: rejectedUser => {
        this.users = this.users.filter(u => u.id !== rejectedUser.id);
        this.processingUserId = null;
        this.successMessage = `User ${rejectedUser.username} rejected`;
      },
      error: err => {
        this.processingUserId = null;
        this.errorMessage = err.error?.message || 'Could not reject user.';
      }
    });
  }

  fullName(user: AppUser): string {
    return `${user.firstName} ${user.lastName}`;
  }
}
