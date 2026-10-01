package rs.etf.pia.sportsphere.dto;

import java.time.LocalDateTime;

public class FacilityReviewResponse {

    private Long id;
    private Long facilityId;
    private Long athleteId;

    private String athleteUsername;
    private String athleteFirstName;
    private String athleteLastName;
    private String athleteProfileImagePath;

    private String reaction;
    private String commentText;
    private LocalDateTime createdAt;

    public FacilityReviewResponse() {
    }

    public FacilityReviewResponse(
            Long id,
            Long facilityId,
            Long athleteId,
            String athleteUsername,
            String athleteFirstName,
            String athleteLastName,
            String athleteProfileImagePath,
            String reaction,
            String commentText,
            LocalDateTime createdAt) {

        this.id = id;
        this.facilityId = facilityId;
        this.athleteId = athleteId;
        this.athleteUsername = athleteUsername;
        this.athleteFirstName = athleteFirstName;
        this.athleteLastName = athleteLastName;
        this.athleteProfileImagePath =
            athleteProfileImagePath;
        this.reaction = reaction;
        this.commentText = commentText;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public Long getAthleteId() {
        return athleteId;
    }

    public String getAthleteUsername() {
        return athleteUsername;
    }

    public String getAthleteFirstName() {
        return athleteFirstName;
    }

    public String getAthleteLastName() {
        return athleteLastName;
    }

    public String getAthleteProfileImagePath() {
        return athleteProfileImagePath;
    }

    public String getReaction() {
        return reaction;
    }

    public String getCommentText() {
        return commentText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}