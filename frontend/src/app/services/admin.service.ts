import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppUser } from '../models/app-user';
import { Facility } from '../models/facility';
import { Sport } from '../models/sport';
import { Trainer } from '../models/trainer';

@Injectable({
  providedIn: 'root',
})
export class AdminService {
  
  private readonly apiUrl = '/api/admin';

  constructor(private http: HttpClient){
  }

  getAllUsers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${this.apiUrl}/users`);
  }

  getPendingUsers(): Observable<AppUser[]> {
    return this.http.get<AppUser[]>(`${this.apiUrl}/users/pending`);
  }

  approveUser(id: number): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.apiUrl}/users/${id}/approve`, {});
  }


  rejectUser(id: number): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.apiUrl}/users/${id}/reject`, {});
  }


  updateUser(id: number, user: AppUser): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.apiUrl}/users/${id}`, user);
  }


  deleteUser(id: number): Observable<AppUser> {
    return this.http.delete<AppUser>(`${this.apiUrl}/users/${id}`);
  }


  getPendingFacilities(): Observable<Facility[]> {
    return this.http.get<Facility[]>(`${this.apiUrl}/facilities/pending`);
  }


  approveFacility(id: number): Observable<Facility> {
    return this.http.put<Facility>(`${this.apiUrl}/facilities/${id}/approve`, {});
  }


  rejectFacility(id: number): Observable<Facility> {
    return this.http.put<Facility>(`${this.apiUrl}/facilities/${id}/reject`, {});
  }

  getSports(): Observable<Sport[]> {
    return this.http.get<Sport[]>(`${this.apiUrl}/sports`);
  }

  createSport(name: string): Observable<Sport> {
    return this.http.post<Sport>(`${this.apiUrl}/sports`, { name });
  }

  getTrainers(): Observable<Trainer[]> {
    return this.http.get<Trainer[]>(`${this.apiUrl}/trainers`);
  }

  deactivateTrainer(id: number): Observable<Trainer> {
    return this.http.put<Trainer>(
      `${this.apiUrl}/trainers/${id}/deactivate`,
      {}
    );
  }
}
