import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Sport } from '../../../models/sport';
import { TeammateAd } from '../../../models/teammate-ad';
import { TeammateAdRequest } from '../../../models/teammate-ad-request';
import { TeammateRequest } from '../../../models/teammate-request';
import { AuthService } from '../../../services/auth.service';
import { PublicService } from '../../../services/public.service';
import { TeammateAdsService } from '../../../services/teammate-ads.service';
import { AthleteService } from '../../../services/athlete.service';

@Component({
  selector: 'app-athlete-teammates',
  imports: [FormsModule, RouterLink],
  templateUrl: './athlete-teammates.html',
  styleUrl: './athlete-teammates.css'
})
export class AthleteTeammates implements OnInit {

  userId: number | null = null;

  ads: TeammateAd[] = [];
  requests: TeammateRequest[] = [];

  athleteNames: Record<number, string> = {};

  sports: Sport[] = [];
  cities: string[] = [];

  createForm = {
    sportId: null as number | null,
    city: '',
    playDate: '',
    startTime: '',
    endTime: '',
    neededPlayers: 1
  };

  loadingAds = false;
  loadingRequests = false;
  creating = false;
  processingAdId: number | null = null;
  processingRequestId: number | null = null;

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private publicService: PublicService,
    private teammateAdsService: TeammateAdsService,
    private athleteService: AthleteService
  ) {
  }

  ngOnInit(): void {
    const currentUser = this.authService.currentUser();

    if(!currentUser) {
      this.errorMessage = 'You must be logged in.';
      return;
    }

    if(currentUser.role !== 'ATHLETE') {
      this.errorMessage = 'Only athletes can access this page.';
      return;
    }

    this.userId = currentUser.id;

    this.loadSports();
    this.loadCities();
    this.loadAds();
    this.loadRequests();
  }

  loadSports(): void {
    this.publicService.getSports().subscribe({
      next: sports => {
        this.sports = sports;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Could not load sports.';
      }
    });
  }

  loadCities(): void {
    this.publicService.getCities().subscribe({
      next: cities => {
        this.cities = cities;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Could not load cities.';
      }
    });
  }

  loadAds(): void {
    this.loadingAds = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.teammateAdsService.getActiveAds().subscribe({
      next: ads => {
        this.ads = ads;
        this.loadingAds = false;
      },
      error: err => {
        this.loadingAds = false;
        this.errorMessage = err.error?.message || 'Could not load teammate ads.';
      }
    });
  }

  loadRequests(): void {
    if(this.userId === null) {
      return;
    }

    this.loadingRequests = true;

    this.teammateAdsService.getRequestsForOwnerAds(this.userId).subscribe({
      next: requests => {
        this.requests = requests;
        this.loadingRequests = false;

        for(const request of requests) {
          this.loadAthleteName(request.athleteId);
        }
      },
      error: err => {
        this.loadingRequests = false;
        this.errorMessage = err.error?.message || 'Could not load teammate requests.';
      }
    });
  }

  loadAthleteName(athleteId: number): void {
    if(this.athleteNames[athleteId]) {
      return;
    }

    this.athleteService.getProfile(athleteId).subscribe({
      next: athlete => {
        this.athleteNames[athleteId] = `${athlete.firstName} ${athlete.lastName}`;
      },
      error: () => {
        this.athleteNames[athleteId] = `Athlete #${athleteId}`;
      }
    });
  }

  athleteName(athleteId: number): string {
    return this.athleteNames[athleteId] || `Athlete #${athleteId}`;
  }

  createAd(): void {
    if(this.userId === null) {
      return;
    }

    if(this.createForm.sportId === null) {
      this.errorMessage = 'Sport is required.';
      return;
    }

    if(!this.createForm.city || !this.createForm.playDate || !this.createForm.startTime || !this.createForm.endTime) {
      this.errorMessage = 'City, play date, start time and end time are required.';
      return;
    }

    if(this.createForm.neededPlayers <= 0) {
      this.errorMessage = 'Needed players must be positive.';
      return;
    }

    const request: TeammateAdRequest = {
      sportId: this.createForm.sportId,
      city: this.createForm.city,
      playDate: this.createForm.playDate,
      startTime: this.toBackendTime(this.createForm.startTime),
      endTime: this.toBackendTime(this.createForm.endTime),
      neededPlayers: this.createForm.neededPlayers
    };

    this.creating = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.teammateAdsService.createAd(this.userId, request).subscribe({
      next: ad => {
        this.ads = [ad, ...this.ads];
        this.creating = false;
        this.successMessage = 'Teammate ad created.';
        this.resetCreateForm();
        this.loadRequests();
      },
      error: err => {
        this.creating = false;
        this.errorMessage = err.error?.message || 'Could not create teammate ad.';
      }
    });
  }

  joinAd(ad: TeammateAd): void {
    if(this.userId === null) {
      return;
    }

    this.processingAdId = ad.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.teammateAdsService.joinAd(this.userId, ad.id).subscribe({
      next: () => {
        this.processingAdId = null;
        this.successMessage = 'Join request sent.';
      },
      error: err => {
        this.processingAdId = null;
        this.errorMessage = err.error?.message || 'Could not join ad.';
      }
    });
  }

  closeAd(ad: TeammateAd): void {
    if(this.userId === null) {
      return;
    }

    this.processingAdId = ad.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.teammateAdsService.closeAd(this.userId, ad.id).subscribe({
      next: closedAd => {
        this.ads = this.ads.filter(existingAd => existingAd.id !== closedAd.id);
        this.processingAdId = null;
        this.successMessage = 'Ad closed.';
        this.loadRequests();
      },
      error: err => {
        this.processingAdId = null;
        this.errorMessage = err.error?.message || 'Could not close ad.';
      }
    });
  }

  approveRequest(request: TeammateRequest): void {
    if(this.userId === null) {
      return;
    }

    this.processingRequestId = request.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.teammateAdsService.approveRequest(this.userId, request.id).subscribe({
      next: updatedRequest => {
        this.requests = this.requests.map(existingRequest =>
          existingRequest.id === updatedRequest.id ? updatedRequest : existingRequest
        );

        this.processingRequestId = null;
        this.successMessage = 'Request approved.';
        this.loadAds();
      },
      error: err => {
        this.processingRequestId = null;
        this.errorMessage = err.error?.message || 'Could not approve request.';
      }
    });
  }

  rejectRequest(request: TeammateRequest): void {
    if(this.userId === null) {
      return;
    }

    this.processingRequestId = request.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.teammateAdsService.rejectRequest(this.userId, request.id).subscribe({
      next: updatedRequest => {
        this.requests = this.requests.map(existingRequest =>
          existingRequest.id === updatedRequest.id ? updatedRequest : existingRequest
        );

        this.processingRequestId = null;
        this.successMessage = 'Request rejected.';
      },
      error: err => {
        this.processingRequestId = null;
        this.errorMessage = err.error?.message || 'Could not reject request.';
      }
    });
  }

  canJoin(ad: TeammateAd): boolean {
    return this.userId !== null &&
      ad.athleteId !== this.userId &&
      ad.status === 'ACTIVE';
  }

  canClose(ad: TeammateAd): boolean {
    return this.userId !== null &&
      ad.athleteId === this.userId &&
      ad.status === 'ACTIVE';
  }

  canProcessRequest(request: TeammateRequest): boolean {
    return request.status === 'PENDING';
  }

  sportName(sportId: number): string {
    const sport = this.sports.find(s => s.id === sportId);

    return sport ? sport.name : `Sport #${sportId}`;
  }

  adLabel(adId: number): string {
    const ad = this.ads.find(a => a.id === adId);

    if(!ad) {
      return `Ad #${adId}`;
    }

    return `${this.sportName(ad.sportId)} in ${ad.city}, ${ad.playDate}`;
  }

  resetCreateForm(): void {
    this.createForm = {
      sportId: null,
      city: '',
      playDate: '',
      startTime: '',
      endTime: '',
      neededPlayers: 1
    };
  }

  private toBackendTime(value: string): string {
    if(value.length === 5) {
      return `${value}:00`;
    }

    return value;
  }

  isCreateFormValid(): boolean {
    return this.createForm.sportId !== null &&
      this.createForm.city.trim() !== '' &&
      this.createForm.playDate.trim() !== '' &&
      this.createForm.startTime.trim() !== '' &&
      this.createForm.endTime.trim() !== '' &&
      this.createForm.neededPlayers > 0;
  }

  otherActiveAds(): TeammateAd[] {
    if(this.userId === null) {
      return [];
    }

    return this.ads.filter(ad => ad.athleteId !== this.userId);
  }

  myActiveAds(): TeammateAd[] {
    if(this.userId === null) {
      return [];
    }

    return this.ads.filter(ad => ad.athleteId === this.userId);
  }
}