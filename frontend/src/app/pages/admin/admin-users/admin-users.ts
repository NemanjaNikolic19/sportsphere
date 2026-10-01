import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AppUser } from '../../../models/app-user';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-users',
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-users.html',
  styleUrl: './admin-users.css',
})
export class AdminUsers {

  users: AppUser[] = [];

  loading = false;
  saving = false;
  deletingUserId: number | null = null;

  editing = false;
  editUser: AppUser = this.emptyUser();

  private emptyUser(): AppUser {
    return {
      id: 0,
      username: '',
      role: '',
      status: '',
      firstName: '',
      lastName: '',
      phone: '',
      email: '',
      profileImagePath: '',
      createdAt: ''
    }
  }

  errorMessage = '';
  successMessage = '';

  roles = ['ATHLETE', 'EMPLOYEE', 'ADMIN'];
  statuses = ['PENDING', 'ACTIVE', 'REJECTED', 'DELETED'];

  constructor(private adminService: AdminService) {
  }

  ngOnInit(): void {
    this.loadUsers();
  }

  loadUsers(): void {
    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';
    this.editing = false;

    this.adminService.getAllUsers().subscribe({
      next: users => {
        this.users = users;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Could not load users';
      }
    });
  }

  startEdit(user: AppUser) {
    this.editing = true;
    this.errorMessage = '';
    this.successMessage = '';
    
    this.editUser = {
      ...user
    };
  }

  cancelEdit(): void {
    this.editing = false;
    this.editUser = this.emptyUser();
  }

  saveEdit(): void {
    this.saving = false;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminService.updateUser(this.editUser.id, this.editUser).subscribe({
      next: updatedUser => {
        this.users = this.users.map(user =>
          user.id === updatedUser.id ? updatedUser : user
        );

        this.saving = false;
        this.editing = false;
        this.editUser =this.emptyUser();
        this.successMessage = `User ${updatedUser.username} updated.`
      },
      error: err => {
        this.saving = false;
        this.errorMessage = err.error?.message || 'Could not update user.';
      }
    });
  }

  deleteUser(user: AppUser): void {
    if(user.role === 'ADMIN') {
      this.errorMessage = 'Admin users should not be deleted from this page.'
      return;
    }

    this.deletingUserId = user.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.adminService.deleteUser(user.id).subscribe({
      next: deletedUser => {
        this.users = this.users.map(existingUser =>
          existingUser.id === deletedUser.id ? deletedUser : existingUser
        );

        this.deletingUserId = null;
        this.successMessage = `User ${deletedUser.username} marked as deleted.`;
      },
      error: err => {
        this.deletingUserId = null;
        this.errorMessage = err.error?.message || 'Could not delete user.';
      }
    });
  }

  fullName(user: AppUser): string {
    return `${user.firstName} ${user.lastName}`;
  }

  canDelete(user: AppUser): boolean {
    return user.role !== 'ADMIN' && user.status !== 'DELETED';
  }
}
