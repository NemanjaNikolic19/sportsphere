import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Equipment } from '../../../models/equipment';
import { EquipmentOrder } from '../../../models/equipment-order';
import { EquipmentRequest } from '../../../models/equipment-request';
import { Facility } from '../../../models/facility';
import { Sport } from '../../../models/sport';
import { AuthService } from '../../../services/auth.service';
import { EmployeeService } from '../../../services/employee.service';
import { EquipmentService } from '../../../services/equipment.service';
import { PublicService } from '../../../services/public.service';

@Component({
  selector: 'app-employee-equipment',
  imports: [FormsModule, RouterLink],
  templateUrl: './employee-equipment.html',
  styleUrl: './employee-equipment.css'
})
export class EmployeeEquipment implements OnInit {

  employeeId: number | null = null;

  equipmentList: Equipment[] = [];
  orders: EquipmentOrder[] = [];
  facilities: Facility[] = [];
  sports: Sport[] = [];

  createForm = this.emptyEquipmentForm();

  editing = false;
  editingEquipmentId: number | null = null;
  editForm = this.emptyEquipmentForm();

  loadingEquipment = false;
  loadingOrders = false;
  creating = false;
  saving = false;
  processingOrderId: number | null = null;

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private employeeService: EmployeeService,
    private equipmentService: EquipmentService,
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
    this.loadEquipment();
    this.loadOrders();
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

  loadEquipment(): void {
    if(this.employeeId === null) {
      return;
    }

    this.loadingEquipment = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.equipmentService.getEmployeeEquipment(this.employeeId).subscribe({
      next: equipment => {
        this.equipmentList = equipment;
        this.loadingEquipment = false;
      },
      error: err => {
        this.loadingEquipment = false;
        this.errorMessage = err.error?.message || 'Could not load equipment.';
      }
    });
  }

  loadOrders(): void {
    if(this.employeeId === null) {
      return;
    }

    this.loadingOrders = true;

    this.equipmentService.getEmployeeOrders(this.employeeId).subscribe({
      next: orders => {
        this.orders = orders;
        this.loadingOrders = false;
      },
      error: err => {
        this.loadingOrders = false;
        this.errorMessage = err.error?.message || 'Could not load orders.';
      }
    });
  }

  createEquipment(): void {
    if(this.employeeId === null) {
      return;
    }

    if(!this.isCreateFormValid()) {
      this.errorMessage = 'Fill all required equipment fields.';
      return;
    }

    this.creating = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.equipmentService.createEmployeeEquipment(this.employeeId, this.toEquipmentRequest(this.createForm)).subscribe({
      next: equipment => {
        this.equipmentList = [equipment, ...this.equipmentList];
        this.createForm = this.emptyEquipmentForm();
        this.creating = false;
        this.successMessage = 'Equipment created.';
      },
      error: err => {
        this.creating = false;
        this.errorMessage = err.error?.message || 'Could not create equipment.';
      }
    });
  }

  startEdit(equipment: Equipment): void {
    this.editing = true;
    this.editingEquipmentId = equipment.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.editForm = {
      facilityId: equipment.facilityId,
      sportId: equipment.sportId,
      name: equipment.name,
      imagePath: equipment.imagePath,
      price: equipment.price,
      stock: equipment.stock,
      active: equipment.active
    };
  }

  cancelEdit(): void {
    this.editing = false;
    this.editingEquipmentId = null;
    this.editForm = this.emptyEquipmentForm();
  }

  saveEdit(): void {
    if(this.employeeId === null || this.editingEquipmentId === null) {
      return;
    }

    if(!this.isEditFormValid()) {
      this.errorMessage = 'Fill all required equipment fields.';
      return;
    }

    this.saving = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.equipmentService.updateEmployeeEquipment(
      this.employeeId,
      this.editingEquipmentId,
      this.toEquipmentRequest(this.editForm)
    ).subscribe({
      next: updatedEquipment => {
        this.equipmentList = this.equipmentList.map(equipment =>
          equipment.id === updatedEquipment.id ? updatedEquipment : equipment
        );

        this.saving = false;
        this.editing = false;
        this.editingEquipmentId = null;
        this.editForm = this.emptyEquipmentForm();
        this.successMessage = 'Equipment updated.';
      },
      error: err => {
        this.saving = false;
        this.errorMessage = err.error?.message || 'Could not update equipment.';
      }
    });
  }

  acceptOrder(order: EquipmentOrder): void {
    this.processOrder(order.id, 'accept');
  }

  markPickedUp(order: EquipmentOrder): void {
    this.processOrder(order.id, 'picked-up');
  }

  cancelOrder(order: EquipmentOrder): void {
    this.processOrder(order.id, 'cancel');
  }

  processOrder(orderId: number, action: 'accept' | 'picked-up' | 'cancel'): void {
    if(this.employeeId === null) {
      return;
    }

    this.processingOrderId = orderId;
    this.errorMessage = '';
    this.successMessage = '';

    let request;

    if(action === 'accept') {
      request = this.equipmentService.acceptOrder(this.employeeId, orderId);
    } else if(action === 'picked-up') {
      request = this.equipmentService.markOrderPickedUp(this.employeeId, orderId);
    } else {
      request = this.equipmentService.cancelEmployeeOrder(this.employeeId, orderId);
    }

    request.subscribe({
      next: updatedOrder => {
        this.orders = this.orders.map(order =>
          order.id === updatedOrder.id ? updatedOrder : order
        );

        this.processingOrderId = null;
        this.successMessage = 'Order updated.';
        this.loadEquipment();
      },
      error: err => {
        this.processingOrderId = null;
        this.errorMessage = err.error?.message || 'Could not update order.';
      }
    });
  }

  canAccept(order: EquipmentOrder): boolean {
    return order.status === 'ORDERED';
  }

  canMarkPickedUp(order: EquipmentOrder): boolean {
    return order.status === 'ACCEPTED';
  }

  canCancel(order: EquipmentOrder): boolean {
    return order.status === 'ORDERED' || order.status === 'ACCEPTED';
  }

  facilityName(facilityId: number): string {
    const facility = this.facilities.find(f => f.id === facilityId);

    return facility ? facility.name : `Facility #${facilityId}`;
  }

  sportName(sportId: number): string {
    const sport = this.sports.find(s => s.id === sportId);

    return sport ? sport.name : `Sport #${sportId}`;
  }

  imageSource(imagePath: string | null): string {
    if(!imagePath || imagePath.trim() === '') {
      return 'assets/default-equipment.png';
    }

    if(imagePath.startsWith('http://') || imagePath.startsWith('https://')) {
      return imagePath;
    }

    if(imagePath.startsWith('/')) {
      return imagePath;
    }

    return `assets/${imagePath}`;
  }

  formatDateTime(value: string): string {
    if(!value) {
      return '-';
    }

    return value.replace('T', ' ');
  }

  isCreateFormValid(): boolean {
    return this.createForm.facilityId !== null &&
      this.createForm.sportId !== null &&
      this.createForm.name.trim() !== '' &&
      this.createForm.price >= 0 &&
      this.createForm.stock >= 0;
  }

  isEditFormValid(): boolean {
    return this.editForm.facilityId !== null &&
      this.editForm.sportId !== null &&
      this.editForm.name.trim() !== '' &&
      this.editForm.price >= 0 &&
      this.editForm.stock >= 0;
  }

  private emptyEquipmentForm() {
    return {
      facilityId: null as number | null,
      sportId: null as number | null,
      name: '',
      imagePath: null as string | null,
      price: 0,
      stock: 0,
      active: true
    };
  }

  private toEquipmentRequest(form: ReturnType<EmployeeEquipment['emptyEquipmentForm']>): EquipmentRequest {
    return {
      facilityId: form.facilityId as number,
      sportId: form.sportId as number,
      name: form.name,
      imagePath: form.imagePath,
      price: form.price,
      stock: form.stock,
      active: form.active
    };
  }
}