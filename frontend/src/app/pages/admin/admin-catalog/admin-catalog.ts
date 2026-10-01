import { Component } from '@angular/core';
import { AdminService } from '../../../services/admin.service';
import { Trainer } from '../../../models/trainer';
import { Sport } from '../../../models/sport';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';

@Component({
  selector: 'app-admin-catalog',
  imports: [FormsModule, RouterLink],
  templateUrl: './admin-catalog.html',
  styleUrl: './admin-catalog.css',
})
export class AdminCatalog {
  sports: Sport[] = [];
  trainers: Trainer[] = [];

  newSportName = '';

  loadingSports = false;
  loadingTrainers = false;
  creatingSport = false;
  processingTrainerId: number | null = null;

  errorMessage = '';
  successMessage = '';

  constructor(private adminService: AdminService) {
  }

  ngOnInit(): void {
    this.loadSports();
    this.loadTrainers();
  }

  loadSports(): void {
    this.loadingSports = true;

    this.adminService.getSports().subscribe({
      next: sports => {
        this.sports = sports;
        this.loadingSports = false;
      },
      error: err => {
        this.loadingSports = false;
        this.errorMessage =
          err.error?.message || 'Could not load sports.';
      }
    });
  }

  loadTrainers(): void {
    this.loadingTrainers = true;

    this.adminService.getTrainers().subscribe({
      next: trainers => {
        this.trainers = trainers;
        this.loadingTrainers = false;
      },
      error: err => {
        this.loadingTrainers = false;
        this.errorMessage =
          err.error?.message || 'Could not load trainers.';
      }
    });
  }

  createSport(): void {
    const name = this.newSportName.trim();

    if(name === '') {
      this.errorMessage = 'Sport name is required.';
      return;
    }

    this.errorMessage = '';
    this.successMessage = '';
    this.creatingSport = true;

    this.adminService.createSport(name).subscribe({
      next: sport => {
        this.sports.push(sport);

        this.sports.sort((a, b) =>
          a.name.localeCompare(b.name)
        );

        this.newSportName = '';
        this.creatingSport = false;
        this.successMessage = `Sport ${sport.name} created.`;
      },
      error: err => {
        this.creatingSport = false;
        this.errorMessage =
          err.error?.message || 'Could not create sport.';
      }
    });
  }

  deactivateTrainer(trainer: Trainer): void {
    this.errorMessage = '';
    this.successMessage = '';
    this.processingTrainerId = trainer.id;

    this.adminService.deactivateTrainer(trainer.id).subscribe({
      next: updatedTrainer => {
        const index = this.trainers.findIndex(
          currentTrainer => currentTrainer.id === updatedTrainer.id
        );

        if(index !== -1) {
          this.trainers[index] = updatedTrainer;
        }

        this.processingTrainerId = null;
        this.successMessage =
          `Trainer ${updatedTrainer.firstName} ${updatedTrainer.lastName} deactivated.`;
      },
      error: err => {
        this.processingTrainerId = null;
        this.errorMessage =
          err.error?.message || 'Could not deactivate trainer.';
      }
    });
  }

  sportName(sportId: number): string {
    return this.sports.find(sport => sport.id === sportId)?.name
      || `Sport #${sportId}`;
  }
}
