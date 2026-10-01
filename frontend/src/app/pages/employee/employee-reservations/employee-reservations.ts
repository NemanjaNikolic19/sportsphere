import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { CalendarEvent } from '../../../models/calendar-event';
import { Facility } from '../../../models/facility';
import { Reservation } from '../../../models/reservation';
import { Resource } from '../../../models/resource';
import { Sport } from '../../../models/sport';
import { TrainingSession } from '../../../models/training-session';
import { AuthService } from '../../../services/auth.service';
import { EmployeeService } from '../../../services/employee.service';
import { PublicService } from '../../../services/public.service';

@Component({
  selector: 'app-employee-reservations',
  imports: [FormsModule, RouterLink],
  templateUrl: './employee-reservations.html',
  styleUrl: './employee-reservations.css'
})
export class EmployeeReservations implements OnInit {

  employeeId: number | null = null;

  reservations: Reservation[] = [];
  trainingSessions: TrainingSession[] = [];
  calendarEvents: CalendarEvent[] = [];

  facilities: Facility[] = [];
  calendarResources: Resource[] = [];
  resourceNames = new Map<number, string>();
  sports: Sport[] = [];

  selectedCalendarFacilityId: number | null = null;
  selectedCalendarResourceId: number | null = null;

  calendarWeekStart = new Date();
  calendarDays: Date[] = [];
  calendarHours: number[] = [];

  loadingReservations = false;
  loadingTrainingSessions = false;
  loadingCalendar = false;
  loadingCalendarResources = false;

  processingEventKey = '';
  movingEventKey = '';
  draggingEventKey = '';
  dragOverSlotKey = '';

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private employeeService: EmployeeService,
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
    this.calendarWeekStart = this.startOfWeek(new Date());
    this.buildCalendarDays();

    this.loadFacilities();
    this.loadSports();
    this.loadReservations();
    this.loadTrainingSessions();
    this.loadCalendar();
  }

  loadFacilities(): void {
    if(this.employeeId === null) {
      return;
    }

    this.employeeService.getFacilities(this.employeeId).subscribe({
      next: facilities => {
        this.facilities = facilities;
        this.resourceNames.clear();

        for(const facility of facilities) {
          this.publicService.getFacilityResources(facility.id).subscribe({
            next: resources => {
              for(const resource of resources) {
                this.resourceNames.set(resource.id, resource.name);
              }
            }
          });
        }

        const selectedStillExists = facilities.some(facility =>
          facility.id === this.selectedCalendarFacilityId
        );

        if(!selectedStillExists) {
          const initialFacility =
            facilities.find(facility => facility.status === 'APPROVED') ??
            facilities[0];

          this.selectedCalendarFacilityId = initialFacility?.id ?? null;
        }

        this.onCalendarFacilityChange();
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

  loadReservations(): void {
    if(this.employeeId === null) {
      return;
    }

    this.loadingReservations = true;

    this.employeeService.getReservations(this.employeeId).subscribe({
      next: reservations => {
        this.reservations = reservations;
        this.loadingReservations = false;
      },
      error: err => {
        this.loadingReservations = false;
        this.errorMessage = err.error?.message || 'Could not load reservations.';
      }
    });
  }

  loadTrainingSessions(): void {
    if(this.employeeId === null) {
      return;
    }

    this.loadingTrainingSessions = true;

    this.employeeService.getTrainingSessions(this.employeeId).subscribe({
      next: sessions => {
        this.trainingSessions = sessions;
        this.loadingTrainingSessions = false;
      },
      error: err => {
        this.loadingTrainingSessions = false;
        this.errorMessage = err.error?.message || 'Could not load trainings.';
      }
    });
  }

  loadCalendar(): void {
    if(this.employeeId === null) {
      return;
    }

    this.loadingCalendar = true;

    this.employeeService.getCalendar(this.employeeId).subscribe({
      next: events => {
        this.calendarEvents = events;
        this.loadingCalendar = false;
      },
      error: err => {
        this.loadingCalendar = false;
        this.errorMessage = err.error?.message || 'Could not load calendar.';
      }
    });
  }

  confirmReservation(reservation: Reservation): void {
    if(this.employeeId === null) {
      return;
    }

    this.processingEventKey = `RESERVATION-${reservation.id}`;
    this.clearMessages();

    this.employeeService.confirmReservation(this.employeeId, reservation.id).subscribe({
      next: updated => {
        this.updateReservation(updated);
        this.processingEventKey = '';
        this.successMessage = 'Reservation confirmed.';
      },
      error: err => {
        this.processingEventKey = '';
        this.errorMessage = err.error?.message || 'Could not confirm reservation.';
      }
    });
  }

  noShowReservation(reservation: Reservation): void {
    if(this.employeeId === null) {
      return;
    }

    this.processingEventKey = `RESERVATION-${reservation.id}`;
    this.clearMessages();

    this.employeeService.noShowReservation(this.employeeId, reservation.id).subscribe({
      next: updated => {
        this.updateReservation(updated);
        this.processingEventKey = '';
        this.successMessage = 'Reservation marked as no-show.';
      },
      error: err => {
        this.processingEventKey = '';
        this.errorMessage = err.error?.message || 'Could not mark reservation no-show.';
      }
    });
  }

  confirmTrainingSession(session: TrainingSession): void {
    if(this.employeeId === null) {
      return;
    }

    this.processingEventKey = `TRAINING-${session.id}`;
    this.clearMessages();

    this.employeeService.confirmTrainingSession(this.employeeId, session.id).subscribe({
      next: updated => {
        this.updateTrainingSession(updated);
        this.processingEventKey = '';
        this.successMessage = 'Training attendance confirmed.';
      },
      error: err => {
        this.processingEventKey = '';
        this.errorMessage = err.error?.message || 'Could not confirm training.';
      }
    });
  }

  noShowTrainingSession(session: TrainingSession): void {
    if(this.employeeId === null) {
      return;
    }

    this.processingEventKey = `TRAINING-${session.id}`;
    this.clearMessages();

    this.employeeService.noShowTrainingSession(this.employeeId, session.id).subscribe({
      next: updated => {
        this.updateTrainingSession(updated);
        this.processingEventKey = '';
        this.successMessage = 'Training marked as no-show.';
      },
      error: err => {
        this.processingEventKey = '';
        this.errorMessage = err.error?.message || 'Could not mark training no-show.';
      }
    });
  }

  canProcessReservation(reservation: Reservation): boolean {
    return reservation.status === 'REQUESTED';
  }

  canProcessTraining(session: TrainingSession): boolean {
    return session.status === 'SCHEDULED';
  }

  onCalendarFacilityChange(): void {
    this.calendarResources = [];
    this.selectedCalendarResourceId = null;
    this.buildCalendarHours();

    if(this.selectedCalendarFacilityId === null) {
      return;
    }

    this.loadingCalendarResources = true;

    this.publicService
      .getFacilityResources(this.selectedCalendarFacilityId)
      .subscribe({
        next: resources => {
          this.calendarResources = resources.filter(resource => resource.active);

          for(const resource of resources) {
            this.resourceNames.set(resource.id, resource.name);
          }

          this.selectedCalendarResourceId =
            this.calendarResources[0]?.id ?? null;

          this.loadingCalendarResources = false;
        },
        error: err => {
          this.loadingCalendarResources = false;
          this.errorMessage =
            err.error?.message || 'Could not load facility resources.';
        }
      });
  }

  selectedCalendarFacility(): Facility | undefined {
    return this.facilities.find(facility =>
      facility.id === this.selectedCalendarFacilityId
    );
  }

  selectedCalendarResource(): Resource | undefined {
    return this.calendarResources.find(resource =>
      resource.id === this.selectedCalendarResourceId
    );
  }

  selectedResourceEvents(): CalendarEvent[] {
    if(this.selectedCalendarResourceId === null) {
      return [];
    }

    return this.calendarEvents
      .filter(event => event.resourceId === this.selectedCalendarResourceId)
      .sort((first, second) =>
        new Date(first.startTime).getTime() -
        new Date(second.startTime).getTime()
      );
  }

  changeWeek(offset: number): void {
    const newStart = new Date(this.calendarWeekStart);

    newStart.setDate(newStart.getDate() + offset * 7);
    this.calendarWeekStart = newStart;
    this.buildCalendarDays();
  }

  goToCurrentWeek(): void {
    this.calendarWeekStart = this.startOfWeek(new Date());
    this.buildCalendarDays();
  }

  calendarRangeLabel(): string {
    if(this.calendarDays.length === 0) {
      return '';
    }

    const first = this.calendarDays[0];
    const last = this.calendarDays[6];

    return (
      `${first.toLocaleDateString('en-GB')} - ` +
      `${last.toLocaleDateString('en-GB')}`
    );
  }

  dayLabel(day: Date): string {
    return day.toLocaleDateString('en-GB', {
      weekday: 'short',
      day: '2-digit',
      month: '2-digit'
    });
  }

  dateKey(day: Date): string {
    return this.toBackendDateTime(day).substring(0, 10);
  }

  hourLabel(hour: number): string {
    return `${hour.toString().padStart(2, '0')}:00`;
  }

  slotKey(day: Date, hour: number): string {
    return `${this.dateKey(day)}-${hour}`;
  }

  eventKey(event: CalendarEvent): string {
    return `${event.eventType}-${event.id}`;
  }

  formatDateTime(value: string): string {
    return value ? value.replace('T', ' ') : '-';
  }

  formatTime(value: string): string {
    return value?.length >= 16 ? value.substring(11, 16) : '-';
  }

  eventStartingInSlot(day: Date, hour: number): CalendarEvent | undefined {
    const slotStart = this.slotStart(day, hour).getTime();
    const slotEnd = slotStart + 60 * 60 * 1000;

    return this.selectedResourceEvents().find(event => {
      const eventStart = new Date(event.startTime).getTime();

      return eventStart >= slotStart && eventStart < slotEnd;
    });
  }

  eventCoveringSlot(day: Date, hour: number): CalendarEvent | undefined {
    const slotStart = this.slotStart(day, hour).getTime();
    const slotEnd = slotStart + 60 * 60 * 1000;

    return this.selectedResourceEvents().find(event => {
      const eventStart = new Date(event.startTime).getTime();
      const eventEnd = new Date(event.endTime).getTime();

      return eventStart < slotEnd && eventEnd > slotStart;
    });
  }

  isSlotPast(day: Date, hour: number): boolean {
    return this.slotStart(day, hour).getTime() <= Date.now();
  }

  canDrag(event: CalendarEvent): boolean {
    const resource = this.selectedCalendarResource();
    const movableStatus =
      (event.eventType === 'RESERVATION' && event.status === 'REQUESTED') ||
      (event.eventType === 'TRAINING' && event.status === 'SCHEDULED');

    return (
      movableStatus &&
      this.isIndoorResource(resource) &&
      this.movingEventKey !== this.eventKey(event)
    );
  }

  onEventDragStart(dragEvent: DragEvent, event: CalendarEvent): void {
    if(!this.canDrag(event)) {
      dragEvent.preventDefault();
      return;
    }

    this.draggingEventKey = this.eventKey(event);

    if(dragEvent.dataTransfer) {
      dragEvent.dataTransfer.effectAllowed = 'move';
      dragEvent.dataTransfer.setData('text/plain', this.draggingEventKey);
    }

    this.clearMessages();
  }

  onEventDragEnd(): void {
    this.clearDragState();
  }

  onSlotDragOver(dragEvent: DragEvent, day: Date, hour: number): void {
    if(!this.canDropAt(day, hour)) {
      return;
    }

    dragEvent.preventDefault();

    if(dragEvent.dataTransfer) {
      dragEvent.dataTransfer.dropEffect = 'move';
    }

    this.dragOverSlotKey = this.slotKey(day, hour);
  }

  onSlotDragLeave(day: Date, hour: number): void {
    if(this.dragOverSlotKey === this.slotKey(day, hour)) {
      this.dragOverSlotKey = '';
    }
  }

  onSlotDrop(dragEvent: DragEvent, day: Date, hour: number): void {
    dragEvent.preventDefault();

    if(this.employeeId === null || !this.canDropAt(day, hour)) {
      this.clearDragState();
      return;
    }

    const event = this.draggingEvent();

    if(!event) {
      this.clearDragState();
      return;
    }

    const eventKey = this.eventKey(event);
    const newStart = this.toBackendDateTime(this.slotStart(day, hour));

    this.clearDragState();
    this.movingEventKey = eventKey;
    this.clearMessages();

    const moveRequest =
      event.eventType === 'RESERVATION'
        ? this.employeeService.moveReservation(
            this.employeeId,
            event.id,
            newStart
          )
        : this.employeeService.moveTrainingSession(
            this.employeeId,
            event.id,
            newStart
          );

    moveRequest.subscribe({
      next: updated => {
        this.updateCalendarEvent(updated);
        this.movingEventKey = '';
        this.successMessage = `${this.eventTypeLabel(updated)} moved successfully.`;
      },
      error: err => {
        this.movingEventKey = '';
        this.errorMessage = err.error?.message || 'Could not move calendar event.';
        this.loadCalendar();
      }
    });
  }

  canDropAt(day: Date, hour: number): boolean {
    const event = this.draggingEvent();
    const facility = this.selectedCalendarFacility();
    const resource = this.selectedCalendarResource();

    if(
      !event ||
      !facility ||
      !this.isIndoorResource(resource) ||
      event.resourceId !== this.selectedCalendarResourceId
    ) {
      return false;
    }

    const oldStart = new Date(event.startTime);
    const oldEnd = new Date(event.endTime);
    const duration = oldEnd.getTime() - oldStart.getTime();
    const newStart = this.slotStart(day, hour);
    const newEnd = new Date(newStart.getTime() + duration);

    if(
      duration <= 0 ||
      newStart.getTime() <= Date.now() ||
      newStart.getTime() === oldStart.getTime() ||
      this.dateKey(newStart) !== this.dateKey(newEnd)
    ) {
      return false;
    }

    const openingMinutes = this.timeToMinutes(facility.workingTimeFrom);
    const closingMinutes = this.timeToMinutes(facility.workingTimeTo);
    const newStartMinutes = newStart.getHours() * 60 + newStart.getMinutes();
    const newEndMinutes = newEnd.getHours() * 60 + newEnd.getMinutes();

    if(
      newStartMinutes < openingMinutes ||
      newEndMinutes > closingMinutes
    ) {
      return false;
    }

    return !this.calendarEvents.some(other => {
      if(
        this.eventKey(other) === this.eventKey(event) ||
        other.resourceId !== event.resourceId
      ) {
        return false;
      }

      const otherStart = new Date(other.startTime).getTime();
      const otherEnd = new Date(other.endTime).getTime();

      return otherStart < newEnd.getTime() && otherEnd > newStart.getTime();
    });
  }

  eventTypeLabel(event: CalendarEvent): string {
    return event.eventType === 'TRAINING' ? 'Training' : 'Reservation';
  }

  facilityName(facilityId: number): string {
    const facility = this.facilities.find(current => current.id === facilityId);

    return facility ? facility.name : `Facility #${facilityId}`;
  }

  resourceName(resourceId: number): string {
    return this.resourceNames.get(resourceId) ?? `Resource #${resourceId}`;
  }

  sportName(sportId: number): string {
    const sport = this.sports.find(current => current.id === sportId);

    return sport ? sport.name : `Sport #${sportId}`;
  }

  private updateReservation(updated: Reservation): void {
    this.reservations = this.reservations.map(reservation =>
      reservation.id === updated.id ? updated : reservation
    );

    this.patchCalendarEvent('RESERVATION', updated.id, {
      startTime: updated.startTime,
      endTime: updated.endTime,
      status: updated.status
    });
  }

  private updateTrainingSession(updated: TrainingSession): void {
    this.trainingSessions = this.trainingSessions.map(session =>
      session.id === updated.id ? updated : session
    );

    this.patchCalendarEvent('TRAINING', updated.id, {
      startTime: updated.startTime,
      endTime: updated.endTime,
      status: updated.status
    });
  }

  private updateCalendarEvent(updated: CalendarEvent): void {
    this.calendarEvents = this.calendarEvents
      .map(event =>
        this.eventKey(event) === this.eventKey(updated) ? updated : event
      )
      .sort((first, second) =>
        new Date(first.startTime).getTime() -
        new Date(second.startTime).getTime()
      );

    if(updated.eventType === 'RESERVATION') {
      this.reservations = this.reservations.map(reservation =>
        reservation.id === updated.id
          ? {
              ...reservation,
              startTime: updated.startTime,
              endTime: updated.endTime,
              status: updated.status
            }
          : reservation
      );
    } else {
      this.trainingSessions = this.trainingSessions.map(session =>
        session.id === updated.id
          ? {
              ...session,
              startTime: updated.startTime,
              endTime: updated.endTime,
              status: updated.status
            }
          : session
      );
    }
  }

  private patchCalendarEvent(
    eventType: 'RESERVATION' | 'TRAINING',
    id: number,
    changes: Partial<CalendarEvent>
  ): void {
    this.calendarEvents = this.calendarEvents.map(event =>
      event.eventType === eventType && event.id === id
        ? { ...event, ...changes }
        : event
    );
  }

  private draggingEvent(): CalendarEvent | undefined {
    return this.calendarEvents.find(event =>
      this.eventKey(event) === this.draggingEventKey
    );
  }

  private isIndoorResource(resource: Resource | undefined): boolean {
    return (
      resource?.type === 'HALL' ||
      resource?.type === 'INDOOR_COURT'
    );
  }

  private clearMessages(): void {
    this.errorMessage = '';
    this.successMessage = '';
  }

  private clearDragState(): void {
    this.draggingEventKey = '';
    this.dragOverSlotKey = '';
  }

  private buildCalendarDays(): void {
    this.calendarDays = Array.from({ length: 7 }, (_, index) => {
      const day = new Date(this.calendarWeekStart);

      day.setDate(day.getDate() + index);

      return day;
    });
  }

  private buildCalendarHours(): void {
    const facility = this.selectedCalendarFacility();

    if(!facility) {
      this.calendarHours = [];
      return;
    }

    const opening = this.timeToMinutes(facility.workingTimeFrom);
    const closing = this.timeToMinutes(facility.workingTimeTo);
    const firstHour = Math.ceil(opening / 60);
    const lastHour = Math.floor((closing - 1) / 60);

    this.calendarHours =
      lastHour < firstHour
        ? []
        : Array.from(
            { length: lastHour - firstHour + 1 },
            (_, index) => firstHour + index
          );
  }

  private startOfWeek(value: Date): Date {
    const date = new Date(value);

    date.setHours(0, 0, 0, 0);

    const dayOfWeek = date.getDay();
    const difference = dayOfWeek === 0 ? -6 : 1 - dayOfWeek;

    date.setDate(date.getDate() + difference);

    return date;
  }

  private slotStart(day: Date, hour: number): Date {
    const value = new Date(day);

    value.setHours(hour, 0, 0, 0);

    return value;
  }

  private timeToMinutes(value: string): number {
    const parts = value.split(':');

    return Number(parts[0]) * 60 + Number(parts[1] ?? 0);
  }

  private toBackendDateTime(value: Date): string {
    const year = value.getFullYear();
    const month = (value.getMonth() + 1).toString().padStart(2, '0');
    const day = value.getDate().toString().padStart(2, '0');
    const hour = value.getHours().toString().padStart(2, '0');
    const minute = value.getMinutes().toString().padStart(2, '0');

    return `${year}-${month}-${day}T${hour}:${minute}:00`;
  }
}
