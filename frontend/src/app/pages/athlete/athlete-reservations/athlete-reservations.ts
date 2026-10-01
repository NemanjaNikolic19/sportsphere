import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { Reservation } from '../../../models/reservation';
import { Facility } from '../../../models/facility';
import { Resource } from '../../../models/resource';
import { Sport } from '../../../models/sport';
import { AuthService } from '../../../services/auth.service';
import { AthleteService } from '../../../services/athlete.service';
import { PublicService } from '../../../services/public.service';
import { ReservationRequest } from '../../../models/reservation-request';
import { CalendarEvent } from '../../../models/calendar-event';

@Component({
  selector: 'app-athlete-reservations',
  imports: [FormsModule, RouterLink],
  templateUrl: './athlete-reservations.html',
  styleUrl: './athlete-reservations.css',
})
export class AthleteReservations implements OnInit {
  userId: number | null = null;

  reservations: Reservation[] = [];
  resourceReservations: CalendarEvent[] = [];

  facilities: Facility[] = [];
  resources: Resource[] = [];
  sports: Sport[] = [];

  reservationForm = {
    facilityId: null as number | null,
    resourceId: null as number | null,
    startTime: '',
    endTime: ''
  };

  calendarWeekStart = new Date();
  calendarDays: Date[] = [];
  calendarHours: number[] =
    Array.from({ length: 15 }, (_, index) => index + 8);

  loading = false;
  loadingFacilities = false;
  loadingResources = false;
  loadingResourceReservations = false;
  creating = false;
  cancellingReservationId: number | null = null;

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private athleteService: AthleteService,
    private publicService: PublicService
  ) {
  }

  ngOnInit(): void {
    this.calendarWeekStart = this.startOfWeek(new Date());
    this.buildCalendarDays();

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

    this.loadReservations();
    this.loadFacilities();
    this.loadSports();
  }

  loadReservations(): void {
    if(this.userId === null) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.athleteService.getReservations(this.userId).subscribe({
      next: reservations => {
        this.reservations = reservations;
        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Could not load reservations.';
      }
    });
  }

  loadFacilities(): void {
    this.loadingFacilities = true;

    this.publicService.searchFacilities({}).subscribe({
      next: facilities => {
        this.facilities = facilities;
        this.loadingFacilities = false;
      },
      error: err => {
        this.loadingFacilities = false;
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

  onFacilityChange(): void {
    this.resources = [];
    this.resourceReservations = [];
    this.reservationForm.resourceId = null;

    if(this.reservationForm.facilityId === null) {
      return;
    }

    this.loadingResources = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.publicService.getFacilityResources(this.reservationForm.facilityId).subscribe({
      next: resources => {
        this.resources = resources.filter(resource => resource.active);
        this.loadingResources = false;
      },
      error: err => {
        this.loadingResources = false;
        this.errorMessage = err.error?.message || 'Could not load facility resources.';
      }
    });
  }

  onResourceChange(): void {
    this.resourceReservations = [];

    if(this.reservationForm.resourceId === null) {
      return;
    }

    this.loadingResourceReservations = true;

    this.athleteService.getResourceReservations(this.reservationForm.resourceId).subscribe({
      next: reservations => {
        this.resourceReservations = reservations;
        this.loadingResourceReservations = false;
      },
      error: err => {
        this.loadingResourceReservations = false;
        this.errorMessage = err.error?.message || 'Could not load resource reservations.';
      }
    });
  }

  createReservation(): void {
    if(this.userId === null) {
      return;
    }

    if(this.reservationForm.facilityId === null || this.reservationForm.resourceId === null) {
      this.errorMessage = 'Facility and resource are required.';
      return;
    }

    if(!this.reservationForm.startTime || !this.reservationForm.endTime) {
      this.errorMessage = 'Start and end time are required.';
      return;
    }

    const resource = this.selectedResource();

    if(!resource) {
      this.errorMessage = 'Selected resource is invalid.';
      return;
    }

    const request: ReservationRequest = {
      facilityId: this.reservationForm.facilityId,
      resourceId: this.reservationForm.resourceId,
      sportId: resource.sportId,
      startTime: this.toBackendDateTime(this.reservationForm.startTime),
      endTime: this.toBackendDateTime(this.reservationForm.endTime)
    };

    this.creating = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.athleteService.createReservation(this.userId, request).subscribe({
      next: reservation => {
        this.reservations = [reservation, ...this.reservations];
        this.creating = false;
        this.successMessage = 'Reservation created.';
        this.onResourceChange();
      },
      error: err => {
        this.creating = false;
        this.errorMessage = err.error?.message || 'Could not create reservation.';
      }
    });
  }

  cancelReservation(reservation: Reservation): void {
    if(this.userId === null) {
      return;
    }

    this.cancellingReservationId = reservation.id;
    this.errorMessage = '';
    this.successMessage = '';

    this.athleteService.cancelReservation(this.userId, reservation.id).subscribe({
      next: cancelledReservation => {
        this.reservations = this.reservations.map(existing =>
          existing.id === cancelledReservation.id ? cancelledReservation : existing
        );

        this.cancellingReservationId = null;
        this.successMessage = 'Reservation cancelled.';

        if(this.reservationForm.resourceId !== null) {
          this.onResourceChange();
        }
      },
      error: err => {
        this.cancellingReservationId = null;
        this.errorMessage = err.error?.message || 'Could not cancel reservation.';
      }
    });
  }

  selectedResource(): Resource | undefined {
    if(this.reservationForm.resourceId === null) {
      return undefined;
    }

    return this.resources.find(resource => resource.id === this.reservationForm.resourceId);
  }

  selectedResourceSportName(): string {
    const resource = this.selectedResource();

    if(!resource) {
      return '-';
    }

    return this.sportName(resource.sportId);
  }

  facilityName(facilityId: number): string {
    const facility = this.facilities.find(f => f.id === facilityId);

    return facility ? facility.name : `Facility #${facilityId}`;
  }

  sportName(sportId: number): string {
    const sport = this.sports.find(s => s.id === sportId);

    return sport ? sport.name : `Sport #${sportId}`;
  }

  resourceLabel(resource: Resource): string {
    return `${resource.name} (${resource.type}, ${this.sportName(resource.sportId)})`;
  }

  canCancel(reservation: Reservation): boolean {
    return reservation.status === 'REQUESTED' &&
          new Date(reservation.startTime).getTime() - Date.now()
            >= 12 * 60 * 60 * 1000;
  }

  formatDateTime(value: string): string {
    if(!value) {
      return '-';
    }

    return value.replace('T', ' ');
  }

  private toBackendDateTime(value: string): string {
    if(value.length === 16) {
      return `${value}:00`;
    }

    return value;
  }

  rotatableResources(): Resource[] {
    const selected = this.selectedResource();

    if(!selected) {
      return [];
    }

    return this.resources.filter(resource =>
      resource.active &&
      resource.type === selected.type &&
      resource.sportId === selected.sportId
    );
  }

  rotateResource(direction: number): void {
    const candidates = this.rotatableResources();

    if(candidates.length <= 1) {
      return;
    }

    const currentIndex = candidates.findIndex(
      resource =>
        resource.id === this.reservationForm.resourceId
    );

    const nextIndex =
      (currentIndex + direction + candidates.length)
      % candidates.length;

    this.reservationForm.resourceId =
      candidates[nextIndex].id;

    this.onResourceChange();
  }

  resourcePosition(): string {
    const candidates = this.rotatableResources();

    const currentIndex = candidates.findIndex(
      resource =>
        resource.id === this.reservationForm.resourceId
    );

    if(currentIndex === -1) {
      return '';
    }

    return `${currentIndex + 1} / ${candidates.length}`;
  }

  changeWeek(offset: number): void {
    const newStart = new Date(this.calendarWeekStart);

    newStart.setDate(
      newStart.getDate() + offset * 7
    );

    this.calendarWeekStart = newStart;
    this.buildCalendarDays();
  }

  calendarRangeLabel(): string {
    if(this.calendarDays.length === 0) {
      return '';
    }

    const first = this.calendarDays[0];
    const last = this.calendarDays[6];

    return `${first.toLocaleDateString('en-GB')} – ` +
          `${last.toLocaleDateString('en-GB')}`;
  }

  dayLabel(day: Date): string {
    return day.toLocaleDateString('en-GB', {
      weekday: 'short',
      day: '2-digit',
      month: '2-digit'
    });
  }

  dateKey(day: Date): string {
    return this.toLocalDateTime(day).substring(0, 10);
  }

  hourLabel(hour: number): string {
    return `${hour.toString().padStart(2, '0')}:00`;
  }

  isSlotOccupied(day: Date, hour: number): boolean {
    const slotStart = this.slotStart(day, hour).getTime();
    const slotEnd = slotStart + 60 * 60 * 1000;

    return this.resourceReservations.some(reservation => {
      if(reservation.status === 'CANCELLED') {
        return false;
      }

      const reservationStart =
        new Date(reservation.startTime).getTime();

      const reservationEnd =
        new Date(reservation.endTime).getTime();

      return reservationStart < slotEnd &&
            reservationEnd > slotStart;
    });
  }

  isSlotPast(day: Date, hour: number): boolean {
    return this.slotStart(day, hour).getTime() <= Date.now();
  }

  isSlotUnavailable(day: Date, hour: number): boolean {
    return this.isSlotPast(day, hour) ||
          this.isSlotOccupied(day, hour);
  }

  isSelectedSlot(day: Date, hour: number): boolean {
    const slotValue =
      this.toLocalDateTime(this.slotStart(day, hour));

    return this.reservationForm.startTime === slotValue;
  }

  selectCalendarSlot(day: Date, hour: number): void {
    if(this.isSlotUnavailable(day, hour)) {
      return;
    }

    const start = this.slotStart(day, hour);
    const end = new Date(start);

    end.setHours(end.getHours() + 1);

    this.reservationForm.startTime =
      this.toLocalDateTime(start);

    this.reservationForm.endTime =
      this.toLocalDateTime(end);

    this.errorMessage = '';
    this.successMessage =
      'One-hour interval selected. Confirm the reservation below.';
  }

  private buildCalendarDays(): void {
    this.calendarDays = Array.from(
      { length: 7 },
      (_, index) => {
        const day = new Date(this.calendarWeekStart);

        day.setDate(day.getDate() + index);

        return day;
      }
    );
  }

  private startOfWeek(value: Date): Date {
    const date = new Date(value);

    date.setHours(0, 0, 0, 0);

    const dayOfWeek = date.getDay();

    const difference =
      dayOfWeek === 0 ? -6 : 1 - dayOfWeek;

    date.setDate(date.getDate() + difference);

    return date;
  }

  private slotStart(day: Date, hour: number): Date {
    const date = new Date(day);

    date.setHours(hour, 0, 0, 0);

    return date;
  }

  private toLocalDateTime(value: Date): string {
    const year = value.getFullYear();

    const month =
      (value.getMonth() + 1).toString().padStart(2, '0');

    const day =
      value.getDate().toString().padStart(2, '0');

    const hour =
      value.getHours().toString().padStart(2, '0');

    const minute =
      value.getMinutes().toString().padStart(2, '0');

    return `${year}-${month}-${day}T${hour}:${minute}`;
  }
}
