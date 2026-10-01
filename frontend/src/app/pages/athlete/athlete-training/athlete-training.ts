import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

import { Facility } from '../../../models/facility';
import { Resource } from '../../../models/resource';
import { Sport } from '../../../models/sport';
import { Trainer } from '../../../models/trainer';
import { TrainingSession } from '../../../models/training-session';
import { TrainingSessionRequest } from '../../../models/training-session-request';
import { AuthService } from '../../../services/auth.service';
import { PublicService } from '../../../services/public.service';
import { TrainingService } from '../../../services/training.service';

@Component({
  selector: 'app-athlete-training',
  imports: [FormsModule, RouterLink],
  templateUrl: './athlete-training.html',
  styleUrl: './athlete-training.css'
})
export class AthleteTraining implements OnInit {

  userId: number | null = null;

  facilities: Facility[] = [];
  sports: Sport[] = [];
  trainers: Trainer[] = [];
  resources: Resource[] = [];
  allResources: Resource[] = [];
  sessions: TrainingSession[] = [];

  filters = {
    facilityId: null as number | null,
    sportId: null as number | null
  };

  sessionForm = {
    trainerId: null as number | null,
    resourceId: null as number | null,
    startTime: '',
    endTime: ''
  };

  loadingTrainers = false;
  loadingSessions = false;
  creating = false;

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private publicService: PublicService,
    private trainingService: TrainingService
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

    this.loadFacilities();
    this.loadSports();
    this.loadTrainers();
    this.loadSessions();
  }

  loadFacilities(): void {
    this.publicService.searchFacilities({}).subscribe({
      next: facilities => {
        this.facilities = facilities;
        this.allResources = [];

        for(const facility of facilities) {
          this.publicService.getFacilityResources(facility.id).subscribe({
            next: resources => {
              this.allResources = [
                ...this.allResources.filter(existing =>
                  !resources.some(resource => resource.id === existing.id)
                ),
                ...resources
              ];
            }
          });
        }
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

  loadTrainers(): void {
    this.loadingTrainers = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.trainingService.getTrainers(this.filters.facilityId, this.filters.sportId).subscribe({
      next: trainers => {
        this.trainers = trainers;
        this.loadingTrainers = false;

        if(this.sessionForm.trainerId !== null &&
          !this.trainers.some(trainer => trainer.id === this.sessionForm.trainerId)
        ) {
          this.sessionForm.trainerId = null;
          this.onTrainerChange();
        }
      },
      error: err => {
        this.loadingTrainers = false;
        this.errorMessage = err.error?.message || 'Could not load trainers.';
      }
    });
  }

  loadSessions(): void {
    if(this.userId === null) {
      return;
    }

    this.loadingSessions = true;

    this.trainingService.getTrainingSessions(this.userId).subscribe({
      next: sessions => {
        this.sessions = sessions;
        this.loadingSessions = false;
      },
      error: err => {
        this.loadingSessions = false;
        this.errorMessage = err.error?.message || 'Could not load training sessions.';
      }
    });
  }

  clearFilters(): void {
    this.filters = {
      facilityId: null,
      sportId: null
    };

    this.loadTrainers();
  }

  onTrainerChange(): void {
    this.resources = [];
    this.sessionForm.resourceId = null;

    const trainer = this.selectedTrainer();

    if(!trainer) {
      return;
    }

    this.publicService.getFacilityResources(trainer.facilityId).subscribe({
      next: resources => {
        this.resources = resources.filter(resource =>
          resource.active &&
          resource.sportId === trainer.sportId
        );

        this.allResources = [
          ...this.allResources.filter(existing =>
            !resources.some(resource => resource.id === existing.id)
          ),
          ...resources
        ];
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Could not load training resources.';
      }
    });
  }

  createSession(): void {
    if(this.userId === null) {
      return;
    }

    const trainer = this.selectedTrainer();

    if(!trainer) {
      this.errorMessage = 'Trainer is required.';
      return;
    }

    if(this.sessionForm.resourceId === null) {
      this.errorMessage = 'Resource is required.';
      return;
    }

    if(!this.sessionForm.startTime || !this.sessionForm.endTime) {
      this.errorMessage = 'Start and end time are required.';
      return;
    }

    const request: TrainingSessionRequest = {
      trainerId: trainer.id,
      facilityId: trainer.facilityId,
      resourceId: this.sessionForm.resourceId,
      sportId: trainer.sportId,
      startTime: this.toBackendDateTime(this.sessionForm.startTime),
      endTime: this.toBackendDateTime(this.sessionForm.endTime)
    };

    this.creating = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.trainingService.createTrainingSession(this.userId, request).subscribe({
      next: session => {
        this.sessions = [session, ...this.sessions];
        this.creating = false;
        this.successMessage = 'Training session scheduled.';
        this.resetSessionForm();
      },
      error: err => {
        this.creating = false;
        this.errorMessage = err.error?.message || 'Could not schedule training session.';
      }
    });
  }

  selectedTrainer(): Trainer | undefined {
    if(this.sessionForm.trainerId === null) {
      return undefined;
    }

    return this.trainers.find(trainer => trainer.id === this.sessionForm.trainerId);
  }

  trainerName(trainer: Trainer): string {
    return `${trainer.firstName} ${trainer.lastName}`;
  }

  trainerNameById(trainerId: number): string {
    const trainer = this.trainers.find(t => t.id === trainerId);

    return trainer ? this.trainerName(trainer) : `Trainer #${trainerId}`;
  }

  facilityName(facilityId: number): string {
    const facility = this.facilities.find(f => f.id === facilityId);

    return facility ? facility.name : `Facility #${facilityId}`;
  }

  sportName(sportId: number): string {
    const sport = this.sports.find(s => s.id === sportId);

    return sport ? sport.name : `Sport #${sportId}`;
  }

  resourceName(resourceId: number): string {
    const resource = this.allResources.find(current => current.id === resourceId);

    return resource ? resource.name : `Resource #${resourceId}`;
  }

  selectedTrainerInfo(): string {
    const trainer = this.selectedTrainer();

    if(!trainer) {
      return '-';
    }

    return `${this.trainerName(trainer)}, ${this.sportName(trainer.sportId)}, ${trainer.pricePerHour}/h`;
  }

  formatDateTime(value: string): string {
    if(!value) {
      return '-';
    }

    return value.replace('T', ' ');
  }

  resetSessionForm(): void {
    this.sessionForm = {
      trainerId: null,
      resourceId: null,
      startTime: '',
      endTime: ''
    };

    this.resources = [];
  }

  isCreateFormValid(): boolean {
    return this.sessionForm.trainerId !== null &&
      this.sessionForm.resourceId !== null &&
      this.sessionForm.startTime.trim() !== '' &&
      this.sessionForm.endTime.trim() !== '';
  }

  private toBackendDateTime(value: string): string {
    if(value.length === 16) {
      return `${value}:00`;
    }

    return value;
  }
}
