import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { AppUser } from '../models/app-user';
import { CalendarEvent } from '../models/calendar-event';
import { Facility } from '../models/facility';
import { FacilityRequest } from '../models/facility-request';
import { ProfileUpdateRequest } from '../models/profile-update-request';
import { Reservation } from '../models/reservation';
import { TrainingSession } from '../models/training-session';

@Injectable({
  providedIn: 'root'
})
export class EmployeeService {

  private readonly apiUrl = '/api/employee';

  constructor(private http: HttpClient) {
  }

  getProfile(employeeId: number): Observable<AppUser> {
    return this.http.get<AppUser>(`${this.apiUrl}/${employeeId}/profile`);
  }

  updateProfile(employeeId: number, request: ProfileUpdateRequest): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.apiUrl}/${employeeId}/profile`, request);
  }

  updateProfileImage(
    employeeId: number,
    profileImage: File
  ): Observable<AppUser> {
    const formData = new FormData();

    formData.append(
      'profileImage',
      profileImage,
      profileImage.name
    );

    return this.http.put<AppUser>(
      `${this.apiUrl}/${employeeId}/profile-image`,
      formData
    );
  }

  removeProfileImage(
    employeeId: number
  ): Observable<AppUser> {
    return this.http.delete<AppUser>(
      `${this.apiUrl}/${employeeId}/profile-image`
    );
  }

  getFacilities(employeeId: number): Observable<Facility[]> {
    return this.http.get<Facility[]>(`${this.apiUrl}/${employeeId}/facilities`);
  }

  createFacility(employeeId: number, request: FacilityRequest): Observable<Facility> {
    return this.http.post<Facility>(`${this.apiUrl}/${employeeId}/facilities`, request);
  }

  updateFacility(employeeId: number, facilityId: number, request: FacilityRequest): Observable<Facility> {
    return this.http.put<Facility>(
      `${this.apiUrl}/${employeeId}/facilities/${facilityId}`,
      request
    );
  }

  importFacilitiesJson(employeeId: number, file: File): Observable<Facility[]> {
    const formData = new FormData();
    formData.append('file', file);

    return this.http.post<Facility[]>(
      `${this.apiUrl}/${employeeId}/facilities/import-json`,
      formData
    );
  }

  getReservations(employeeId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.apiUrl}/${employeeId}/reservations`);
  }

  confirmReservation(employeeId: number, reservationId: number): Observable<Reservation> {
    return this.http.put<Reservation>(
      `${this.apiUrl}/${employeeId}/reservations/${reservationId}/confirm`,
      {}
    );
  }

  noShowReservation(employeeId: number, reservationId: number): Observable<Reservation> {
    return this.http.put<Reservation>(
      `${this.apiUrl}/${employeeId}/reservations/${reservationId}/no-show`,
      {}
    );
  }

  moveReservation(
    employeeId: number,
    reservationId: number,
    startTime: string
  ): Observable<CalendarEvent> {
    return this.http.put<CalendarEvent>(
      `${this.apiUrl}/${employeeId}/reservations/${reservationId}/move`,
      { startTime }
    );
  }

  getTrainingSessions(employeeId: number): Observable<TrainingSession[]> {
    return this.http.get<TrainingSession[]>(
      `${this.apiUrl}/${employeeId}/training-sessions`
    );
  }

  confirmTrainingSession(
    employeeId: number,
    sessionId: number
  ): Observable<TrainingSession> {
    return this.http.put<TrainingSession>(
      `${this.apiUrl}/${employeeId}/training-sessions/${sessionId}/confirm`,
      {}
    );
  }

  noShowTrainingSession(
    employeeId: number,
    sessionId: number
  ): Observable<TrainingSession> {
    return this.http.put<TrainingSession>(
      `${this.apiUrl}/${employeeId}/training-sessions/${sessionId}/no-show`,
      {}
    );
  }

  moveTrainingSession(
    employeeId: number,
    sessionId: number,
    startTime: string
  ): Observable<CalendarEvent> {
    return this.http.put<CalendarEvent>(
      `${this.apiUrl}/${employeeId}/training-sessions/${sessionId}/move`,
      { startTime }
    );
  }

  getCalendar(employeeId: number): Observable<CalendarEvent[]> {
    return this.http.get<CalendarEvent[]>(`${this.apiUrl}/${employeeId}/calendar`);
  }
}
