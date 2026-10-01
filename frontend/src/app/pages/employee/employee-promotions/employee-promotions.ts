import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Facility } from '../../../models/facility';
import { Promotion } from '../../../models/promotion';
import { PromotionRequest } from '../../../models/promotion-request';
import { Sport } from '../../../models/sport';
import { AuthService } from '../../../services/auth.service';
import { EmployeeService } from '../../../services/employee.service';
import { PromotionService } from '../../../services/promotion.service';
import { PublicService } from '../../../services/public.service';

@Component({
  selector: 'app-employee-promotions',
  imports: [FormsModule, RouterLink],
  templateUrl: './employee-promotions.html',
  styleUrl: './employee-promotions.css'
})
export class EmployeePromotions implements OnInit {

  employeeId: number | null = null;

  promotions: Promotion[] = [];
  facilities: Facility[] = [];
  sports: Sport[] = [];

  createForm = this.emptyPromotionForm();

  editing = false;
  editingPromotionId: number | null = null;
  editForm = this.emptyPromotionForm();

  loading = false;
  creating = false;
  saving = false;

  errorMessage = '';
  successMessage = '';

  discountTypes = ['PERCENT', 'FIXED'];

  constructor(
    private authService: AuthService,
    private employeeService: EmployeeService,
    private promotionService: PromotionService,
    private publicService: PublicService
  ) {
  }

  ngOnInit(): void {
    const currentUser = this.authService.currentUser();

    if(!currentUser) {
      this.errorMessage = 'You must be logged in.';
      return;
    }

    if(currentUser.role !== 'EMPLOYEE') {
      this.errorMessage = 'Only employees can access this page.';
      return;
    }

    this.employeeId = currentUser.id;

    this.loadFacilities();
    this.loadSports();
    this.loadPromotions();
  }

  loadFacilities(): void {
    if(this.employeeId === null) {
      return;
    }

    this.employeeService.getFacilities(this.employeeId).subscribe({
      next: facilities => {
        this.facilities = facilities;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Could not load facilities.';
      }
    });
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

  loadPromotions(): void {
    if(this.employeeId === null) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.promotionService.getEmployeePromotions(this.employeeId).subscribe({
      next: promotions => {
        this.promotions = promotions;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Could not load promotions.';
      }
    });
  }

  createPromotion(): void {
    if(this.employeeId === null) {
      return;
    }

    if(!this.isCreateFormValid()) {
      this.errorMessage = 'Fill all required promotion fields correctly.';
      return;
    }

    this.creating = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.promotionService.createPromotion(this.employeeId, this.toPromotionRequest(this.createForm)).subscribe({
      next: promotion => {
        this.promotions = [promotion, ...this.promotions];
        this.createForm = this.emptyPromotionForm();
        this.creating = false;
        this.successMessage = 'Promotion created.';
      },
      error: err => {
        this.creating = false;
        this.errorMessage = err.error?.message || 'Could not create promotion.';
      }
    });
  }

  startEdit(promotion: Promotion): void {
    this.editing = true;
    this.editingPromotionId = promotion.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.editForm = {
      facilityId: promotion.facilityId,
      sportId: promotion.sportId,
      name: promotion.name,
      validFrom: promotion.validFrom,
      validTo: promotion.validTo,
      discountType: promotion.discountType,
      discountValue: promotion.discountValue,
      active: promotion.active
    };
  }

  cancelEdit(): void {
    this.editing = false;
    this.editingPromotionId = null;
    this.editForm = this.emptyPromotionForm();
  }

  saveEdit(): void {
    if(this.employeeId === null || this.editingPromotionId === null) {
      return;
    }

    if(!this.isEditFormValid()) {
      this.errorMessage = 'Fill all required promotion fields correctly.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.promotionService.updatePromotion(
      this.employeeId,
      this.editingPromotionId,
      this.toPromotionRequest(this.editForm)
    ).subscribe({
      next: updatedPromotion => {
        this.promotions = this.promotions.map(promotion =>
          promotion.id === updatedPromotion.id ? updatedPromotion : promotion
        );

        this.saving = false;
        this.editing = false;
        this.editingPromotionId = null;
        this.editForm = this.emptyPromotionForm();
        this.successMessage = 'Promotion updated.';
      },
      error: err => {
        this.saving = false;
        this.errorMessage = err.error?.message || 'Could not update promotion.';
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

  isCreateFormValid(): boolean {
    return this.isFormValid(this.createForm);
  }

  isEditFormValid(): boolean {
    return this.isFormValid(this.editForm);
  }

  private isFormValid(form: ReturnType<EmployeePromotions['emptyPromotionForm']>): boolean {
    if(form.facilityId === null) {
      return false;
    }

    if(form.name.trim() === '') {
      return false;
    }

    if(form.validFrom.trim() === '' || form.validTo.trim() === '') {
      return false;
    }

    if(form.validTo < form.validFrom) {
      return false;
    }

    if(form.discountType !== 'PERCENT' && form.discountType !== 'FIXED') {
      return false;
    }

    if(form.discountValue < 0) {
      return false;
    }

    if(form.discountType === 'PERCENT' && form.discountValue > 100) {
      return false;
    }

    return true;
  }

  private emptyPromotionForm() {
    return {
      facilityId: null as number | null,
      sportId: null as number | null,
      name: '',
      validFrom: '',
      validTo: '',
      discountType: 'PERCENT',
      discountValue: 0,
      active: true
    };
  }

  private toPromotionRequest(form: ReturnType<EmployeePromotions['emptyPromotionForm']>): PromotionRequest {
    return {
      facilityId: form.facilityId as number,
      sportId: form.sportId,
      name: form.name,
      validFrom: form.validFrom,
      validTo: form.validTo,
      discountType: form.discountType,
      discountValue: form.discountValue,
      active: form.active
    };
  }
}