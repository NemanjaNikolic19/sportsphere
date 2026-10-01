import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Trainer } from '../models/trainer';
import { TrainingSession } from '../models/training-session';
import { TrainingSessionRequest } from '../models/training-session-request';

@Injectable({
  providedIn: 'root'
})
export class TrainingService {

  private readonly apiUrl = '/api';

  constructor(private http: HttpClient) {
  }

  getTrainers(facilityId?: number | null, sportId?: number | null): Observable<Trainer[]> {
    let params = new HttpParams();

    if(facilityId !== null && facilityId !== undefined) {
      params = params.set('facilityId', facilityId);
    }

    if(sportId !== null && sportId !== undefined) {
      params = params.set('sportId', sportId);
    }

    return this.http.get<Trainer[]>(`${this.apiUrl}/trainers`, { params });
  }

  createTrainingSession(userId: number, request: TrainingSessionRequest): Observable<TrainingSession> {
    return this.http.post<TrainingSession>(
      `${this.apiUrl}/athlete/${userId}/training-sessions`,
      request
    );
  }

  getTrainingSessions(userId: number): Observable<TrainingSession[]> {
    return this.http.get<TrainingSession[]>(`${this.apiUrl}/athlete/${userId}/training-sessions`);
  }
}