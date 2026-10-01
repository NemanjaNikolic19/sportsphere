import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Promotion } from '../models/promotion';
import { PromotionRequest } from '../models/promotion-request';

@Injectable({
  providedIn: 'root'
})
export class PromotionService {

  private readonly apiUrl = '/api';

  constructor(private http: HttpClient) {
  }

  getActivePromotions(): Observable<Promotion[]> {
    return this.http.get<Promotion[]>(`${this.apiUrl}/promotions/active`);
  }

  getUpcomingPromotions(): Observable<Promotion[]> {
    return this.http.get<Promotion[]>(`${this.apiUrl}/promotions/upcoming`);
  }

  getEmployeePromotions(employeeId: number): Observable<Promotion[]> {
    return this.http.get<Promotion[]>(`${this.apiUrl}/employee/${employeeId}/promotions`);
  }

  createPromotion(employeeId: number, request: PromotionRequest): Observable<Promotion> {
    return this.http.post<Promotion>(`${this.apiUrl}/employee/${employeeId}/promotions`, request);
  }

  updatePromotion(employeeId: number, promotionId: number, request: PromotionRequest): Observable<Promotion> {
    return this.http.put<Promotion>(
      `${this.apiUrl}/employee/${employeeId}/promotions/${promotionId}`,
      request
    );
  }
}