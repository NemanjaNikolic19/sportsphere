import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, RouterLink } from '@angular/router';
import { Facility } from '../../../models/facility';
import { Resource } from '../../../models/resource';
import { FacilityImage } from '../../../models/facility-image';
import { PublicService } from '../../../services/public.service';
import { DomSanitizer, SafeResourceUrl } from '@angular/platform-browser';
import { FormsModule } from '@angular/forms';
import { User } from '../../../models/user';
import { AthleteService } from '../../../services/athlete.service';
import { AuthService } from '../../../services/auth.service';
import { FacilityReview, FacilityReviewReaction } from '../../../models/facility-review';

@Component({
  selector: 'app-facility-details',
  imports: [RouterLink, FormsModule],
  templateUrl: './facility-details.html',
  styleUrl: './facility-details.css',
})
export class FacilityDetails {
  facility: Facility | null = null;
  resources: Resource[] = [];
  images: FacilityImage[] = [];

  mapUrl: SafeResourceUrl | null = null;

  facilityId: number | null = null;
  currentUser: User | null = null;

  reviews: FacilityReview[] = [];

  selectedReaction: FacilityReviewReaction | null = null;
  remainingReviews = 0;
  reviewCommentText = '';

  loadingReviews = false;
  loadingReviewQuota = false;
  submittingReview = false;

  interactionError = '';
  interactionMessage = '';

  loading = false;
  errorMessage = '';

  constructor(
    private route: ActivatedRoute,
    private publicService: PublicService,
    private sanitizer: DomSanitizer,
    private athleteService: AthleteService,
    private authService: AuthService
  ) {

  }

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    const id = Number(idParam);

    if(!id || Number.isNaN(id)) {
      this.errorMessage = 'Invalid facility id.';
      return;
    }

    this.facilityId = id;
    this.currentUser = this.authService.currentUser();

    this.loadFacility(id);
    this.loadResource(id);
    this.loadImages(id);
    this.loadReviews(id);

    if(this.isLoggedAthlete()) {
      this.loadReviewQuota(id);
    }
  }

  loadFacility(id: number): void {
    this.loading = true;
    this.errorMessage = '';

    this.publicService.getFacilityById(id).subscribe({
      next: facility => {
        this.facility = facility;

        const location = encodeURIComponent(`${facility.address}, ${facility.city}, Serbia`);

        this.mapUrl =
          this.sanitizer.bypassSecurityTrustResourceUrl(
            `https://www.google.com/maps?q=${location}&output=embed`
          );

        this.loading = false;
      },
      error: err => {
        this.loading = false;
        this.errorMessage = err.error?.message || 'Could not load facility details.';
      }
    });
  }

  loadResource(id: number): void {
    this.publicService.getFacilityResources(id).subscribe({
      next: resources => {
        this.resources = resources;
      },
      error: () => {
        this.resources = [];
      }
    })
  }

  loadImages(id: number): void {
    this.publicService.getFacilityImages(id).subscribe({
      next: images => {
        this.images = images;
      },
      error: () => {
        this.images = [];
      }
    })
  }

  resourceTypeLabel(type: string): string {
    if(type == "OUTDOOR_FIELD") {
      return 'Outdoor field';
    }

    if(type == "INDOOR_COURT") {
      return 'Indoor court';
    }

    if(type == "HALL") {
      return 'Hall';
    }

    return type;
  }

  imageSource(imagePath: string): string {
    if(imagePath.startsWith('http://') || imagePath.startsWith("https://")) {
      return imagePath;
    }

    if(imagePath.startsWith('/')){
      return imagePath;
    }

    return `assets/${imagePath}`;
  }

  isLoggedAthlete(): boolean {
    return (
      this.currentUser !== null &&
      this.currentUser.role === 'ATHLETE' &&
      this.currentUser.status === 'ACTIVE'
    );
  }

  loadReviews(facilityId: number): void {
    this.loadingReviews = true;

    this.publicService.getFacilityReviews(facilityId).subscribe({
      next: reviews => {
        this.reviews = reviews;
        this.loadingReviews = false;
      },
      error: err => {
        this.reviews = [];
        this.loadingReviews = false;
        this.interactionError =
          err.error?.message || 'Could not load reviews.';
      }
    });
  }

  loadReviewQuota(facilityId: number): void {
    if(!this.currentUser) {
      return;
    }

    this.loadingReviewQuota = true;

    this.athleteService.getRemainingFacilityReviews(
      this.currentUser.id,
      facilityId
    ).subscribe({
      next: response => {
        this.remainingReviews = response.remainingReviews;
        this.loadingReviewQuota = false;
      },
      error: err => {
        this.remainingReviews = 0;
        this.loadingReviewQuota = false;
        this.interactionError =
          err.error?.message ||
          'Could not load your remaining review count.';
      }
    });
  }

  selectReaction(
    reaction: FacilityReviewReaction
  ): void {
    this.selectedReaction = reaction;
    this.interactionError = '';
    this.interactionMessage = '';
  }

  canSubmitReview(): boolean {
    const text = this.reviewCommentText.trim();

    return (
      this.isLoggedAthlete() &&
      this.remainingReviews > 0 &&
      this.selectedReaction !== null &&
      text.length > 0 &&
      text.length <= 1000 &&
      !this.submittingReview
    );
  }

  submitReview(): void {
    if(
      !this.currentUser ||
      this.facilityId === null
    ) {
      return;
    }

    if(this.remainingReviews <= 0) {
      this.interactionError =
        'You have no remaining reviews for this facility.';
      return;
    }

    if(this.selectedReaction === null) {
      this.interactionError =
        'Select Like or Dislike.';
      return;
    }

    const commentText =
      this.reviewCommentText.trim();

    if(commentText === '') {
      this.interactionError =
        'Comment cannot be empty.';
      return;
    }

    if(commentText.length > 1000) {
      this.interactionError =
        'Comment can contain at most 1000 characters.';
      return;
    }

    this.submittingReview = true;
    this.interactionError = '';
    this.interactionMessage = '';

    this.athleteService.addFacilityReview(
      this.currentUser.id,
      this.facilityId,
      this.selectedReaction,
      commentText
    ).subscribe({
      next: savedReview => {
        this.reviews = [
          savedReview,
          ...this.reviews
        ].slice(0, 5);

        this.remainingReviews = Math.max(
          this.remainingReviews - 1,
          0
        );

        this.updateDisplayedCounters(savedReview);

        this.selectedReaction = null;
        this.reviewCommentText = '';
        this.submittingReview = false;
        this.interactionMessage =
          'Review published.';
      },
      error: err => {
        this.submittingReview = false;
        this.interactionError =
          err.error?.message ||
          'Could not publish review.';
      }
    });
  }

  private updateDisplayedCounters(
    review: FacilityReview
  ): void {
    if(!this.facility) {
      return;
    }

    if(review.reaction === 'LIKE') {
      this.facility.likeCount++;
    } else {
      this.facility.dislikeCount++;
    }
  }

  isOwnReview(review: FacilityReview): boolean {
    return (
      this.currentUser !== null &&
      review.athleteId === this.currentUser.id
    );
  }

  reviewAuthor(review: FacilityReview): string {
    const fullName =
      `${review.athleteFirstName} ` +
      `${review.athleteLastName}`;

    return (
      fullName.trim() ||
      review.athleteUsername
    );
  }

  formatCommentDate(createdAt: string): string {
    return new Date(createdAt).toLocaleString();
  }

}
