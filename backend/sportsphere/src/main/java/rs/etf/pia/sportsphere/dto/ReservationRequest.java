package rs.etf.pia.sportsphere.dto;

import java.time.LocalDateTime;

public class ReservationRequest {
    
    private Long facilityId;
    private Long resourceId;
    private Long sportId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;

    public ReservationRequest() {
    }

    public ReservationRequest(Long facilityId, Long resourceId, Long sportId, LocalDateTime startTime,
            LocalDateTime endTime) {
        this.facilityId = facilityId;
        this.resourceId = resourceId;
        this.sportId = sportId;
        this.startTime = startTime;
        this.endTime = endTime;
    }

    public Long getFacilityId() {
        return facilityId;
    }

    public void setFacilityId(Long facilityId) {
        this.facilityId = facilityId;
    }

    public Long getResourceId() {
        return resourceId;
    }

    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    public Long getSportId() {
        return sportId;
    }

    public void setSportId(Long sportId) {
        this.sportId = sportId;
    }

    public LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(LocalDateTime startTime) {
        this.startTime = startTime;
    }

    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }
    
}
