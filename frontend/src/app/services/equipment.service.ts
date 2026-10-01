import { Injectable } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable } from 'rxjs';

import { Equipment } from '../models/equipment';
import { EquipmentOrder } from '../models/equipment-order';
import { EquipmentOrderRequest } from '../models/equipment-order-request';
import { EquipmentRequest } from '../models/equipment-request';

@Injectable({
  providedIn: 'root'
})
export class EquipmentService {

  private readonly apiUrl = '/api';

  constructor(private http: HttpClient) {
  }

  getEquipment(sportId?: number | null): Observable<Equipment[]> {
    let params = new HttpParams();

    if(sportId !== null && sportId !== undefined) {
      params = params.set('sportId', sportId);
    }

    return this.http.get<Equipment[]>(`${this.apiUrl}/equipment`, { params });
  }

  createAthleteOrder(userId: number, request: EquipmentOrderRequest): Observable<EquipmentOrder> {
    return this.http.post<EquipmentOrder>(`${this.apiUrl}/athlete/${userId}/orders`, request);
  }

  getAthleteOrders(userId: number): Observable<EquipmentOrder[]> {
    return this.http.get<EquipmentOrder[]>(`${this.apiUrl}/athlete/${userId}/orders`);
  }

  cancelAthleteOrder(userId: number, orderId: number): Observable<EquipmentOrder> {
    return this.http.put<EquipmentOrder>(
      `${this.apiUrl}/athlete/${userId}/orders/${orderId}/cancel`,
      {}
    );
  }

  getEmployeeEquipment(employeeId: number): Observable<Equipment[]> {
    return this.http.get<Equipment[]>(`${this.apiUrl}/employee/${employeeId}/equipment`);
  }

  createEmployeeEquipment(employeeId: number, request: EquipmentRequest): Observable<Equipment> {
    return this.http.post<Equipment>(`${this.apiUrl}/employee/${employeeId}/equipment`, request);
  }

  updateEmployeeEquipment(employeeId: number, equipmentId: number, request: EquipmentRequest): Observable<Equipment> {
    return this.http.put<Equipment>(
      `${this.apiUrl}/employee/${employeeId}/equipment/${equipmentId}`,
      request
    );
  }

  getEmployeeOrders(employeeId: number): Observable<EquipmentOrder[]> {
    return this.http.get<EquipmentOrder[]>(`${this.apiUrl}/employee/${employeeId}/orders`);
  }

  acceptOrder(employeeId: number, orderId: number): Observable<EquipmentOrder> {
    return this.http.put<EquipmentOrder>(
      `${this.apiUrl}/employee/${employeeId}/orders/${orderId}/accept`,
      {}
    );
  }

  markOrderPickedUp(employeeId: number, orderId: number): Observable<EquipmentOrder> {
    return this.http.put<EquipmentOrder>(
      `${this.apiUrl}/employee/${employeeId}/orders/${orderId}/picked-up`,
      {}
    );
  }

  cancelEmployeeOrder(employeeId: number, orderId: number): Observable<EquipmentOrder> {
    return this.http.put<EquipmentOrder>(
      `${this.apiUrl}/employee/${employeeId}/orders/${orderId}/cancel`,
      {}
    );
  }
}