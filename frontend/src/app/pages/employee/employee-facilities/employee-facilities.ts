import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Facility } from '../../../models/facility';
import { FacilityRequest } from '../../../models/facility-request';
import { AuthService } from '../../../services/auth.service';
import { EmployeeService } from '../../../services/employee.service';

@Component({
  selector: 'app-employee-facilities',
  imports: [FormsModule, RouterLink],
  templateUrl: './employee-facilities.html',
  styleUrl: './employee-facilities.css'
})
export class EmployeeFacilities implements OnInit {

  employeeId: number | null = null;

  facilities: Facility[] = [];

  createForm: FacilityRequest = this.emptyFacilityRequest();

  editing = false;
  editingFacilityId: number | null = null;
  editForm: FacilityRequest = this.emptyFacilityRequest();

  selectedImportFile: File | null = null;

  loading = false;
  creating = false;
  saving = false;
  importing = false;

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private employeeService: EmployeeService
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
  }

  loadFacilities(): void {
    if(this.employeeId === null) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.employeeService.getFacilities(this.employeeId).subscribe({
      next: facilities => {
        this.facilities = facilities;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Could not load facilities.';
      }
    });
  }

  createFacility(): void {
    if(this.employeeId === null) {
      return;
    }

    this.creating = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.employeeService.createFacility(this.employeeId, this.toBackendRequest(this.createForm)).subscribe({
      next: facility => {
        this.facilities = [facility, ...this.facilities];
        this.createForm = this.emptyFacilityRequest();
        this.creating = false;
        this.successMessage = 'Facility created and sent for admin approval.';
      },
      error: err => {
        this.creating = false;
        this.errorMessage = err.error?.message || 'Could not create facility.';
      }
    });
  }

  startEdit(facility: Facility): void {
    this.editing = true;
    this.editingFacilityId = facility.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.editForm = {
      name: facility.name,
      city: facility.city,
      address: facility.address,
      description: facility.description,
      pricePerHour: facility.pricePerHour,
      workingTimeFrom: this.toInputTime(facility.workingTimeFrom),
      workingTimeTo: this.toInputTime(facility.workingTimeTo),
      allowedNoShows: facility.allowedNoShows
    };
  }

  cancelEdit(): void {
    this.editing = false;
    this.editingFacilityId = null;
    this.editForm = this.emptyFacilityRequest();
  }

  saveEdit(): void {
    if(this.employeeId === null || this.editingFacilityId === null) {
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.employeeService.updateFacility(
      this.employeeId,
      this.editingFacilityId,
      this.toBackendRequest(this.editForm)
    ).subscribe({
      next: updatedFacility => {
        this.facilities = this.facilities.map(facility =>
          facility.id === updatedFacility.id ? updatedFacility : facility
        );

        this.saving = false;
        this.editing = false;
        this.editingFacilityId = null;
        this.editForm = this.emptyFacilityRequest();
        this.successMessage = 'Facility updated.';
      },
      error: err => {
        this.saving = false;
        this.errorMessage = err.error?.message || 'Could not update facility.';
      }
    });
  }

  onImportFileSelected(event: Event): void {
    const input = event.target as HTMLInputElement;

    if(input.files && input.files.length > 0) {
      this.selectedImportFile = input.files[0];
    } else {
      this.selectedImportFile = null;
    }
  }

  importJson(): void {
    if(this.employeeId === null) {
      return;
    }

    if(this.selectedImportFile === null) {
      this.errorMessage = 'Choose JSON file first.';
      return;
    }

    this.importing = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.employeeService.importFacilitiesJson(this.employeeId, this.selectedImportFile).subscribe({
      next: importedFacilities => {
        this.facilities = [...importedFacilities, ...this.facilities];
        this.importing = false;
        this.selectedImportFile = null;
        this.successMessage = `Imported ${importedFacilities.length} facilities.`;
      },
      error: err => {
        this.importing = false;
        this.errorMessage = err.error?.message || 'Could not import facilities.';
      }
    });
  }

  statusClass(status: string): string {
    return status.toLowerCase();
  }

  isCreateFormValid(): boolean {
    return this.createForm.name.trim() !== '' &&
      this.createForm.city.trim() !== '' &&
      this.createForm.address.trim() !== '' &&
      this.createForm.pricePerHour > 0 &&
      this.createForm.workingTimeFrom.trim() !== '' &&
      this.createForm.workingTimeTo.trim() !== '' &&
      this.createForm.allowedNoShows >= 0;
  }

  isEditFormValid(): boolean {
    return this.editForm.name.trim() !== '' &&
      this.editForm.city.trim() !== '' &&
      this.editForm.address.trim() !== '' &&
      this.editForm.pricePerHour > 0 &&
      this.editForm.workingTimeFrom.trim() !== '' &&
      this.editForm.workingTimeTo.trim() !== '' &&
      this.editForm.allowedNoShows >= 0;
  }

  private emptyFacilityRequest(): FacilityRequest {
    return {
      name: '',
      city: '',
      address: '',
      description: '',
      pricePerHour: 0,
      workingTimeFrom: '08:00',
      workingTimeTo: '22:00',
      allowedNoShows: 3
    };
  }

  private toBackendRequest(request: FacilityRequest): FacilityRequest {
    return {
      ...request,
      workingTimeFrom: this.toBackendTime(request.workingTimeFrom),
      workingTimeTo: this.toBackendTime(request.workingTimeTo)
    };
  }

  private toBackendTime(value: string): string {
    if(value.length === 5) {
      return `${value}:00`;
    }

    return value;
  }

  private toInputTime(value: string): string {
    if(!value) {
      return '';
    }

    return value.substring(0, 5);
  }
}