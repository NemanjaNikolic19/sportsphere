import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { User } from '../../../models/user';

@Component({
  selector: 'app-login',
  imports: [FormsModule, RouterLink],
  templateUrl: './login.html',
  styleUrl: './login.css',
})
export class Login implements OnInit {
  username = '';
  password = '';

  loading = false;
  errorMessage = '';

  constructor(
    private auth: AuthService,
    private router: Router
  ) {

  }

  ngOnInit(): void {
    const user = this.auth.currentUser();

    if(user) {
      this.router.navigateByUrl(this.auth.homeRouteForRole(user.role))
    }
  }

  isAdminRoute(): boolean {
    return this.router.url.startsWith('/admin-login');
  }

  login():void {
    this.errorMessage = ''

    if(!this.username || !this.password){
      this.errorMessage = 'Username and password are required';
      return;
    }

    this.loading = true;

    this.auth.login({
      username: this.username,
      password : this.password
    }).subscribe({
      next: (user: User) => {
        this.loading = false;

        if(this.isAdminRoute() && user.role !== 'ADMIN') {
          this.auth.logout();
          this.errorMessage = 'Only admin can login on this route';
          return;
        }

        if(!this.isAdminRoute() && user.role === 'ADMIN') {
          this.auth.logout();
          this.errorMessage = 'Admin must use the hidden admin login route';
          return;
        }

        this.router.navigateByUrl(this.auth.homeRouteForRole(user.role));
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Login failed';
      }
    });
  }
}
