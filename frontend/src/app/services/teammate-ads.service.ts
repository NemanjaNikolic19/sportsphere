import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { TeammateAd } from '../models/teammate-ad';
import { TeammateAdRequest } from '../models/teammate-ad-request';
import { TeammateRequest } from '../models/teammate-request';

@Injectable({
  providedIn: 'root'
})
export class TeammateAdsService {

  private readonly apiUrl = '/api';

  constructor(private http: HttpClient) {
  }

  getActiveAds(): Observable<TeammateAd[]> {
    return this.http.get<TeammateAd[]>(`${this.apiUrl}/teammate-ads/active`);
  }

  createAd(userId: number, request: TeammateAdRequest): Observable<TeammateAd> {
    return this.http.post<TeammateAd>(`${this.apiUrl}/athlete/${userId}/teammate-ads`, request);
  }

  joinAd(userId: number, adId: number): Observable<TeammateRequest> {
    return this.http.post<TeammateRequest>(
      `${this.apiUrl}/athlete/${userId}/teammate-ads/${adId}/join`,
      {}
    );
  }

  getRequestsForOwnerAds(userId: number): Observable<TeammateRequest[]> {
    return this.http.get<TeammateRequest[]>(`${this.apiUrl}/athlete/${userId}/teammate-ads/requests`);
  }

  approveRequest(userId: number, requestId: number): Observable<TeammateRequest> {
    return this.http.put<TeammateRequest>(
      `${this.apiUrl}/athlete/${userId}/teammate-requests/${requestId}/approve`,
      {}
    );
  }

  rejectRequest(userId: number, requestId: number): Observable<TeammateRequest> {
    return this.http.put<TeammateRequest>(
      `${this.apiUrl}/athlete/${userId}/teammate-requests/${requestId}/reject`,
      {}
    );
  }

  closeAd(userId: number, adId: number): Observable<TeammateAd> {
    return this.http.put<TeammateAd>(
      `${this.apiUrl}/athlete/${userId}/teammate-ads/${adId}/close`,
      {}
    );
  }
}