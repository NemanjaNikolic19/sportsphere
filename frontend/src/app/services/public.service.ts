import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable } from '@angular/core';
import { Observable } from 'rxjs';
import { Facility } from '../models/facility';
import { Sport } from '../models/sport';
import { PublicSearchParams } from '../models/public-search-params';
import { Resource } from '../models/resource';
import { FacilityImage } from '../models/facility-image';
import { FacilityReview } from '../models/facility-review';

@Injectable({
  providedIn: 'root',
})
export class PublicService {
  
  private readonly publciApiUrl = '/api/public'

  constructor(private http:HttpClient) {

  }

  getActiveFacilityCount(): Observable<number> {
    return this.http.get<number>(`${this.publciApiUrl}/facilities/active-count`);
  }

  getTopFacilities(): Observable<Facility[]> {
    return this.http.get<Facility[]>(`${this.publciApiUrl}/facilities/top3`);
  }

  getCities(): Observable<string[]> {
    return this.http.get<string[]>(`${this.publciApiUrl}/cities`);
  }

  getSports(): Observable<Sport[]> {
    return this.http.get<Sport[]>(`${this.publciApiUrl}/sports`);
  }

  searchFacilities(searchParams: PublicSearchParams): Observable<Facility[]> {
    let params = new HttpParams();

    if(searchParams.name && searchParams.name.trim()) {
      params = params.set('name', searchParams.name.trim());
    }

    if(searchParams.city && searchParams.city.trim()) {
      params = params.set('city', searchParams.city.trim());
    }

    if(searchParams.sportId !== null && searchParams.sportId !== undefined) {
      params = params.set('sportId', searchParams.sportId);
    }

    if(searchParams.resourceType && searchParams.resourceType.trim()) {
      params = params.set('resourceType', searchParams.resourceType.trim());
    }
    
    return this.http.get<Facility[]>(`${this.publciApiUrl}/facilities/search`, { params });
  }

  getFacilityById(id: number): Observable<Facility> {
    return this.http.get<Facility>(`${this.publciApiUrl}/facilities/${id}`);
  }

  getFacilityResources(id: number): Observable<Resource[]> {
    return this.http.get<Resource[]>(`${this.publciApiUrl}/facilities/${id}/resources`);
  }

  getFacilityImages(id: number): Observable<FacilityImage[]> {
    return this.http.get<FacilityImage[]>(`${this.publciApiUrl}/facilities/${id}/images`);
  }

  getFacilityReviews(facilityId: number): Observable<FacilityReview[]> {
    return this.http.get<FacilityReview[]>(`${this.publciApiUrl}/facilities/${facilityId}/reviews`);
  }
}
