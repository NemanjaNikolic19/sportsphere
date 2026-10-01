import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../../services/auth.service';
import { Facility } from '../../../models/facility';
import { Promotion } from '../../../models/promotion';
import { PublicService } from '../../../services/public.service';
import { PromotionService } from '../../../services/promotion.service';

@Component({
  selector: 'app-home',
  imports: [RouterLink],
  templateUrl: './home.html',
  styleUrl: './home.css'
})
export class Home implements OnInit {

  activeFacilityCount = 0;
  topFacilities: Facility[] = [];
  currentPromotions: Promotion[] = [];

  promotionFacilityNames: Record<number, string> = {};

  constructor(
    public auth: AuthService,
    private publicService: PublicService,
    private promotionService: PromotionService
  ) {
  }

  ngOnInit(): void {
    this.loadHomeData();
  }

  loadHomeData(): void {
    this.publicService.getActiveFacilityCount().subscribe({
      next: count => {
        this.activeFacilityCount = count;
      }
    });

    this.publicService.getTopFacilities().subscribe({
      next: facilities => {
        this.topFacilities = facilities;
      }
    });

    this.promotionService.getActivePromotions().subscribe({
      next: promotions => {
      this.currentPromotions = promotions.slice(0, 3);

      const facilityIds = Array.from(
        new Set(this.currentPromotions.map(promotion => promotion.facilityId))
      );

      for(const facilityId of facilityIds) {
        this.loadPromotionFacilityName(facilityId);
      }
    }
    });
  }

  loadPromotionFacilityName(facilityId: number): void {
    this.publicService.getFacilityById(facilityId).subscribe({
      next: facility => {
        this.promotionFacilityNames[facilityId] = facility.name;
      },
      error: () => {
        this.promotionFacilityNames[facilityId] = `Facility #${facilityId}`;
      }
    });
  }

  promotionFacilityName(facilityId: number): string {
    return this.promotionFacilityNames[facilityId] || `Facility #${facilityId}`;
  }

  discountLabel(promotion: Promotion): string {
    if(promotion.discountType === 'PERCENT') {
      return `${promotion.discountValue}%`;
    }

    return `${promotion.discountValue}`;
  }
}
