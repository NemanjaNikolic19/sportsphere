export type FacilityReviewReaction =
  'LIKE' | 'DISLIKE';

export interface FacilityReview {
  id: number;
  facilityId: number;
  athleteId: number;

  athleteUsername: string;
  athleteFirstName: string;
  athleteLastName: string;
  athleteProfileImagePath: string | null;

  reaction: FacilityReviewReaction;
  commentText: string;
  createdAt: string;
}

export interface FacilityReviewQuota {
  remainingReviews: number;
}