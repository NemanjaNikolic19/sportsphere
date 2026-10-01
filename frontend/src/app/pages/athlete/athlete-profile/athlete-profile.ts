import { Component, OnInit } from '@angular/core';
import { FormsModule } from '@angular/forms';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { AppUser } from '../../../models/app-user';
import { ProfileUpdateRequest } from '../../../models/profile-update-request';
import { Sport } from '../../../models/sport';
import { AuthService } from '../../../services/auth.service';
import { AthleteService } from '../../../services/athlete.service';
import { PublicService } from '../../../services/public.service';

@Component({
  selector: 'app-athlete-profile',
  imports: [FormsModule, RouterLink],
  templateUrl: './athlete-profile.html',
  styleUrl: './athlete-profile.css',
})
export class AthleteProfile implements OnInit {

  userId: number | null = null;

  profile: AppUser | null = null;

  isOwnProfile = true;

  editProfile: ProfileUpdateRequest = {
    firstName: '',
    lastName: '',
    phone: null,
    email: ''
  };

  sports: Sport[] = [];
  favoriteSportIds: number[] = [];

  loading = false;
  savingProfile = false;
  savingSports = false;

  errorMessage = '';
  successMessage = '';

  constructor(
    private authService: AuthService,
    private athleteService: AthleteService,
    private publicService: PublicService,
    private route: ActivatedRoute
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

    const idParam = this.route.snapshot.paramMap.get('id');

    if(idParam) {
      const profileId = Number(idParam);

      if(!profileId || Number.isNaN(profileId)) {
        this.errorMessage = 'Invalid athlete id.';
        return;
      }

      this.userId = profileId;
      this.isOwnProfile = profileId === currentUser.id;
    } else {
      this.userId = currentUser.id;
      this.isOwnProfile = true;
    }

    this.loadProfile();
    this.loadSports();
    this.loadFavoriteSports();
  }

  loadProfile(): void {
    if(this.userId === null) {
      return;
    }

    this.loading = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.athleteService.getProfile(this.userId).subscribe({
      next: profile => {
        this.profile = profile;

        this.editProfile = {
          firstName: profile.firstName,
          lastName: profile.lastName,
          phone: profile.phone,
          email: profile.email
        };

        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Could not load profile.';
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

  loadFavoriteSports(): void {
    if(this.userId === null) {
      return;
    }

    this.athleteService.getFavoriteSports(this.userId).subscribe({
      next: sportIds => {
        this.favoriteSportIds = sportIds;
      },
      error: err => {
        this.errorMessage = err.error?.message || 'Could not load favorite sports.';
      }
    });
  }

  saveProfile(): void {
    if(!this.isOwnProfile) {
      this.errorMessage = 'You can only update your own profile.';
      return;
    }

    if(this.userId === null) {
      return;
    }

    this.savingProfile = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.athleteService.updateProfile(this.userId, this.editProfile).subscribe({
      next: updatedProfile => {
        this.profile = updatedProfile;
        this.savingProfile = false;
        this.successMessage = 'Profile updated.';
      },
      error: err => {
        this.savingProfile = false;
        this.errorMessage = err.error?.message || 'Could not update profile.';
      }
    });
  }

  isFavoriteSportSelected(sportId: number): boolean {
    return this.favoriteSportIds.includes(sportId);
  }

  toggleFavoriteSport(sportId: number, event: Event): void {
    const checkbox = event.target as HTMLInputElement;

    if(checkbox.checked) {
      if(this.favoriteSportIds.length >= 5) {
        checkbox.checked = false;
        this.errorMessage = 'You can choose at most five favorite sports.';
        return;
      }

      this.favoriteSportIds.push(sportId);
      this.errorMessage = '';
      return;
    }

    this.favoriteSportIds = this.favoriteSportIds.filter(id => id !== sportId);
  }

  saveFavoriteSports(): void {
    if(!this.isOwnProfile) {
      this.errorMessage = 'You can only update your own favorite sports.';
      return;
    }

    if(this.userId === null) {
      return;
    }

    this.savingSports = true;
    this.errorMessage = '';
    this.successMessage = '';

    this.athleteService.updateFavoriteSports(this.userId, this.favoriteSportIds).subscribe({
      next: sportIds => {
        this.favoriteSportIds = sportIds;
        this.savingSports = false;
        this.successMessage = 'Favorite sports updated.';
      },
      error: err => {
        this.savingSports = false;
        this.errorMessage = err.error?.message || 'Could not update favorite sports.';
      }
    });
  }

  profileImageUrl(): string {
    const path = this.profile?.profileImagePath;

    if(!path) {
      return '/api/profile-images/default.png';
    }

    if(path.startsWith('/api/profile-images/')) {
      return path;
    }

    return `/api/profile-images/${path}`;
  }
}
