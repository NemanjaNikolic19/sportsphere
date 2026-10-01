import { Component, OnInit } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Facility } from '../../../models/facility';
import { AdminService } from '../../../services/admin.service';

@Component({
  selector: 'app-admin-pending-facilities',
  imports: [RouterLink],
  templateUrl: './admin-pending-facilities.html',
  styleUrl: './admin-pending-facilities.css',
})
export class AdminPendingFacilities {
  facilities: Facility[] = [];

  loading = false;
  processingFacilityId: number | null = null;

  errroMessage = '';
  successMessage = '';

  constructor(private adminService: AdminService) {
  }

  ngOnInit(): void {
    this.loadPendingFacilities();
  }

  loadPendingFacilities(): void {
    this.loading = true;
    this.errroMessage = '';
    this.successMessage = '';
  
    this.adminService.getPendingFacilities().subscribe({
      next: facilities => {
        this.facilities = facilities;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errroMessage = err.error?.message || 'Could not load pending facilities';
      }
    });
  }

  approve(facility: Facility): void {
    this.processingFacilityId = facility.id;
    this.errroMessage = '';
    this.successMessage = '';

    this.adminService.approveFacility(facility.id).subscribe({
      next: approvedFacility => {
        this.facilities = this.facilities.filter(f => f.id !== approvedFacility.id);
        this.processingFacilityId = null;
        this.successMessage = `Facility ${approvedFacility.name} approved.`;
      },
      error: err => {
        this.processingFacilityId = null;
        this.errroMessage = err.error?.message || 'Could not approve facility.';
      }
    });
  }

  reject(facility: Facility): void {
    this.processingFacilityId = facility.id;
    this.errroMessage = '';
    this.successMessage = '';

    this.adminService.rejectFacility(facility.id).subscribe({
      next: rejectedFacility => {
        this.facilities = this.facilities.filter(f => f.id !== rejectedFacility.id);
        this.processingFacilityId = null;
        this.successMessage = `Facility ${rejectedFacility.name} approved.`;
      },
      error: err => {
        this.processingFacilityId = null;
        this.errroMessage = err.error?.message || 'Could not approve facility.';
      }
    });
  }
}
