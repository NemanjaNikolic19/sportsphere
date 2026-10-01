import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { LoginRequest, RegisterRequest, RegisterResponse, User } from '../models/user';
import { Observable, tap } from 'rxjs';

@Injectable({
  providedIn: 'root',
})
export class AuthService {
  private readonly apiUrl = "/api/auth";
  private readonly storageKey = "sportsphereUser";

  constructor(private http: HttpClient){
  }

  login(request: LoginRequest) : Observable<User> {
    return this.http.post<User>(`${this.apiUrl}/login`, request).pipe(
      tap(user => {
        localStorage.setItem(this.storageKey, JSON.stringify(user));
      })
    );
  }

  register(request: RegisterRequest, profileImage: File | null): Observable<RegisterResponse> {
    const formData = new FormData();

    formData.append(
      'request',
      new Blob(
        [JSON.stringify(request)],
        { type: 'application/json' }
      )
    );

    if(profileImage !== null) {
      formData.append(
        'profileImage',
        profileImage,
        profileImage.name
      );
    }

    return this.http.post<RegisterResponse>(
      `${this.apiUrl}/register`,
      formData
    );
  }

  logout(): void {
    localStorage.removeItem(this.storageKey);
  }

  currentUser(): User | null {
    const raw = localStorage.getItem(this.storageKey)

    if(!raw) {
      return null;
    }

    try{
      return JSON.parse(raw) as User;
    } catch {
      localStorage.removeItem(this.storageKey);
      return null;
    }
  }

  updateCurrentUserProfileImage(
    profileImagePath: string | null
  ): void {
    const user = this.currentUser();

    if(!user) {
      return;
    }

    user.profileImagePath = profileImagePath;

    localStorage.setItem(
      this.storageKey,
      JSON.stringify(user)
    );
  }

  isLoggedIn(): boolean {
    return this.currentUser() !== null;
  }

  homeRouteForRole(role: string): string {
    if(role === 'ADMIN') {
      return '/admin';
    }

    if(role === 'ATHLETE') {
      return '/athlete';
    }

    if(role === 'EMPLOYEE') {
      return '/employee';
    }

    return '/';
  }

  forgotPassword(identifier: string): Observable<{
    message: string,
    resetLink: string
  }> {
    return this.http.post<{
      message: string,
      resetLink: string
    }>(
      `${this.apiUrl}/forgot-password`,
      { identifier }
    );
  }

  resetPassword(
    token: string,
    newPassword: string
  ): Observable<{ message: string }> {
    return this.http.post<{ message: string }>(
      `${this.apiUrl}/reset-password`,
      {
        token,
        newPassword
      }
    );
  }
}
