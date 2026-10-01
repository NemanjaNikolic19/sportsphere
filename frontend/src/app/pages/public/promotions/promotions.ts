import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';

import { Facility } from '../../../models/facility';
import { Promotion } from '../../../models/promotion';
import { Sport } from '../../../models/sport';
import { PromotionService } from '../../../services/promotion.service';
import { PublicService } from '../../../services/public.service';

@Component({
  selector: 'app-promotions',
  imports: [RouterLink],
  templateUrl: './promotions.html',
  styleUrl: './promotions.css'
})
export class Promotions implements OnInit {

  activePromotions: Promotion[] = [];
  upcomingPromotions: Promotion[] = [];

  facilities: Facility[] = [];
  sports: Sport[] = [];

  loadingActive = false;
  loadingUpcoming = false;

  errorMessage = '';

  constructor(
    private promotionService: PromotionService,
    private publicService: PublicService
  ) {
  }

  ngOnInit(): void {
    this.loadFacilities();
    this.loadSports();
    this.loadPromotions();
  }

  loadFacilities(): void {
    this.publicService.searchFacilities({}).subscribe({
      next: facilities => {
        this.facilities = facilities;
      }
    });
  }

  loadSports(): void {
    this.publicService.getSports().subscribe({
      next: sports => {
        this.sports = sports;
      }
    });
  }

  loadPromotions(): void {
    this.loadingActive = true;
    this.loadingUpcoming = true;
    this.errorMessage = '';

    this.promotionService.getActivePromotions().subscribe({
      next: promotions => {
        this.activePromotions = promotions;
        this.loadingActive = false;
      },
      error: err => {
        this.loadingActive = false;
        this.errorMessage = err.error?.message || 'Could not load active promotions.';
      }
    });

    this.promotionService.getUpcomingPromotions().subscribe({
      next: promotions => {
        this.upcomingPromotions = promotions;
        this.loadingUpcoming = false;
      },
      error: err => {
        this.loadingUpcoming = false;
        this.errorMessage = err.error?.message || 'Could not load upcoming promotions.';
      }
    });
  }

  facilityName(facilityId: number): string {
    const facility = this.facilities.find(f => f.id === facilityId);

    return facility ? facility.name : `Facility #${facilityId}`;
  }

  sportName(sportId: number | null): string {
    if(sportId === null) {
      return 'All sports';
    }

    const sport = this.sports.find(s => s.id === sportId);

    return sport ? sport.name : `Sport #${sportId}`;
  }

  discountLabel(promotion: Promotion): string {
    if(promotion.discountType === 'PERCENT') {
      return `${promotion.discountValue}%`;
    }

    return `${promotion.discountValue}`;
  }
}