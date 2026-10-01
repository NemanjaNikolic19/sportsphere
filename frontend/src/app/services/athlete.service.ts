import { HttpClient } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { AppUser } from '../models/app-user';
import { ProfileUpdateRequest } from '../models/profile-update-request';
import { Reservation } from '../models/reservation';
import { CalendarEvent } from '../models/calendar-event';
import { ReservationRequest } from '../models/reservation-request';
import { FacilityReview, FacilityReviewQuota, FacilityReviewReaction } from '../models/facility-review';
import { Statistics } from '../models/statistics';

@Injectable({
  providedIn: 'root',
})
export class AthleteService {
  
  private readonly apiUrl = 'api/athlete';
  private readonly resourceUrl = '/api/resources'

  constructor(private http: HttpClient) { 
  }

  getProfile(userId: number): Observable<AppUser> {
    return this.http.get<AppUser>(`${this.apiUrl}/${userId}/profile`);
  }

  updateProfile(userId: number, request: ProfileUpdateRequest): Observable<AppUser> {
    return this.http.put<AppUser>(`${this.apiUrl}/${userId}/profile`, request);
  }

  updateProfileImage(
    userId: number,
    profileImage: File
  ): Observable<AppUser> {
    const formData = new FormData();

    formData.append(
      'profileImage',
      profileImage,
      profileImage.name
    );

    return this.http.put<AppUser>(
      `${this.apiUrl}/${userId}/profile-image`,
      formData
    );
  }

  removeProfileImage(
    userId: number
  ): Observable<AppUser> {
    return this.http.delete<AppUser>(
      `${this.apiUrl}/${userId}/profile-image`
    );
  }

  getFavoriteSports(userId: number): Observable<number[]> {
    return this.http.get<number[]>(`${this.apiUrl}/${userId}/favorite-sports`);
  }

  updateFavoriteSports(userId: number, sportIds: number[]): Observable<number[]> {
    return this.http.put<number[]>(
      `${this.apiUrl}/${userId}/favorite-sports`,
      {sportIds: sportIds}
    );
  }

  getReservations(userId: number): Observable<Reservation[]> {
    return this.http.get<Reservation[]>(`${this.apiUrl}/${userId}/reservations`);
  }

  createReservation(userId: number, request: ReservationRequest): Observable<Reservation> {
    return this.http.post<Reservation>(`${this.apiUrl}/${userId}/reservations`, request);
  }

  cancelReservation(userId: number, reservationId: number): Observable<Reservation> {
    return this.http.put<Reservation>(
      `${this.apiUrl}/${userId}/reservations/${reservationId}/cancel`,
      {}
    );
  }

  getResourceReservations(resourceId: number): Observable<CalendarEvent[]> {
    return this.http.get<CalendarEvent[]>(`${this.resourceUrl}/${resourceId}/reservations`);
  }

  getRemainingFacilityReviews(athleteId: number, facilityId: number ): Observable<FacilityReviewQuota> {
    return this.http.get<FacilityReviewQuota>(`${this.apiUrl}/${athleteId}` + `/facilities/${facilityId}/reviews/remaining`);
  }

  addFacilityReview(athleteId: number, facilityId: number, reaction: FacilityReviewReaction, commentText: string): Observable<FacilityReview> {
    return this.http.post<FacilityReview>(
      `${this.apiUrl}/${athleteId}` + `/facilities/${facilityId}/reviews`,
      {
        reaction: reaction,
        commentText: commentText
      }
    );
  }

  getStatistics(athleteId: number): Observable<Statistics> {
    return this.http.get<Statistics>(`${this.apiUrl}/${athleteId}/statistics`);
  }
}
