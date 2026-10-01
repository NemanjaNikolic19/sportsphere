import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Equipment } from '../../../models/equipment';
import { EquipmentOrder } from '../../../models/equipment-order';
import { EquipmentOrderRequest } from '../../../models/equipment-order-request';
import { Facility } from '../../../models/facility';
import { Sport } from '../../../models/sport';
import { AuthService } from '../../../services/auth.service';
import { EquipmentService } from '../../../services/equipment.service';
import { PublicService } from '../../../services/public.service';

@Component({
  selector: 'app-athlete-shop',
  imports: [FormsModule, RouterLink],
  templateUrl: './athlete-shop.html',
  styleUrl: './athlete-shop.css'
})
export class AthleteShop implements OnInit {

  userId: number | null = null;

  equipmentList: Equipment[] = [];
  orders: EquipmentOrder[] = [];
  sports: Sport[] = [];
  facilities: Facility[] = [];

  sportFilterId: number | null = null;

  quantities: Record<number, number> = {};

  loadingEquipment = false;
  loadingOrders = false;
  creatingOrder = false;
  cancellingOrderId: number | null = null;

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
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

    if(currentUser.role !== 'ATHLETE') {
      this.errorMessage = 'Only athletes can access this page.';
      return;
    }

    this.userId = currentUser.id;

    this.loadSports();
    this.loadFacilities();
    this.loadEquipment();
    this.loadOrders();
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

  loadFacilities(): void {
    this.publicService.searchFacilities({}).subscribe({
      next: facilities => {
        this.facilities = facilities;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Could not load facilities.';
      }
    });
  }

  loadEquipment(): void {
    this.loadingEquipment = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.equipmentService.getEquipment(this.sportFilterId).subscribe({
      next: equipment => {
        this.equipmentList = equipment;
        this.loadingEquipment = false;
        this.cleanQuantities();
      },
      error: err => {
        this.loadingEquipment = false;
        this.errorMessage = err.error?.message || 'Could not load equipment.';
      }
    });
  }

  loadOrders(): void {
    if(this.userId === null) {
      return;
    }

    this.loadingOrders = true;

    this.equipmentService.getAthleteOrders(this.userId).subscribe({
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

  clearFilter(): void {
    this.sportFilterId = null;
    this.loadEquipment();
  }

  onQuantityChange(equipment: Equipment, event: Event): void {
    const input = event.target as HTMLInputElement;

    let quantity = Number(input.value);

    if(Number.isNaN(quantity) || quantity < 0) {
      quantity = 0;
    }

    if(quantity > equipment.stock) {
      quantity = equipment.stock;
    }

    input.value = String(quantity);

    if(quantity === 0) {
      delete this.quantities[equipment.id];
      return;
    }

    this.quantities[equipment.id] = quantity;
  }

  quantityFor(equipmentId: number): number {
    return this.quantities[equipmentId] || 0;
  }

  cartItems(): { equipment: Equipment; quantity: number }[] {
    return this.equipmentList
      .filter(equipment => this.quantityFor(equipment.id) > 0)
      .map(equipment => ({
        equipment: equipment,
        quantity: this.quantityFor(equipment.id)
      }));
  }

  cartTotal(): number {
    return this.cartItems()
      .reduce((total, item) => total + item.equipment.price * item.quantity, 0);
  }

  cartFacilityIds(): number[] {
    return Array.from(new Set(
      this.cartItems().map(item => item.equipment.facilityId)
    ));
  }

  canCreateOrder(): boolean {
    return this.cartItems().length > 0 && this.cartFacilityIds().length === 1;
  }

  createOrder(): void {
    if(this.userId === null) {
      return;
    }

    if(this.cartItems().length === 0) {
      this.errorMessage = 'Cart is empty.';
      return;
    }

    const facilityIds = this.cartFacilityIds();

    if(facilityIds.length !== 1) {
      this.errorMessage = 'All equipment in one order must belong to the same facility.';
      return;
    }

    const request: EquipmentOrderRequest = {
      facilityId: facilityIds[0],
      items: this.cartItems().map(item => ({
        equipmentId: item.equipment.id,
        quantity: item.quantity
      }))
    };

    this.creatingOrder = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.equipmentService.createAthleteOrder(this.userId, request).subscribe({
      next: order => {
        this.orders = [order, ...this.orders];
        this.quantities = {};
        this.creatingOrder = false;
        this.successMessage = 'Order created.';
        this.loadEquipment();
      },
      error: err => {
        this.creatingOrder = false;
        this.errorMessage = err.error?.message || 'Could not create order.';
      }
    });
  }

  cancelOrder(order: EquipmentOrder): void {
    if(this.userId === null) {
      return;
    }

    this.cancellingOrderId = order.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.equipmentService.cancelAthleteOrder(this.userId, order.id).subscribe({
      next: cancelledOrder => {
        this.orders = this.orders.map(existing =>
          existing.id === cancelledOrder.id ? cancelledOrder : existing
        );

        this.cancellingOrderId = null;
        this.successMessage = 'Order cancelled.';
        this.loadEquipment();
      },
      error: err => {
        this.cancellingOrderId = null;
        this.errorMessage = err.error?.message || 'Could not cancel order.';
      }
    });
  }

  canCancelOrder(order: EquipmentOrder): boolean {
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

  private cleanQuantities(): void {
    const visibleEquipmentIds = new Set(this.equipmentList.map(equipment => equipment.id));

    for(const idText of Object.keys(this.quantities)) {
      const id = Number(idText);

      if(!visibleEquipmentIds.has(id)) {
        delete this.quantities[id];
      }
    }
  }
}