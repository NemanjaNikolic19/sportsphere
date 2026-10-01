package rs.etf.pia.sportsphere.dto;

import java.time.LocalDateTime;

import rs.etf.pia.sportsphere.entity.Reservation;
import rs.etf.pia.sportsphere.entity.TrainingSession;

public class CalendarEventResponse {

    private String eventType;
    private Long id;
    private Long athleteId;
    private Long trainerId;
    private Long facilityId;
    private Long resourceId;
    private Long sportId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private String status;

    public CalendarEventResponse() {
    }

    public static CalendarEventResponse fromReservation(Reservation reservation) {
        CalendarEventResponse response = new CalendarEventResponse();

        response.setEventType("RESERVATION");
        response.setId(reservation.getId());
        response.setAthleteId(reservation.getAthleteId());
        response.setFacilityId(reservation.getFacilityId());
        response.setResourceId(reservation.getResourceId());
        response.setSportId(reservation.getSportId());
        response.setStartTime(reservation.getStartTime());
        response.setEndTime(reservation.getEndTime());
        response.setStatus(reservation.getStatus());

        return response;
    }

    public static CalendarEventResponse fromTrainingSession(TrainingSession session) {
        CalendarEventResponse response = new CalendarEventResponse();

        response.setEventType("TRAINING");
        response.setId(session.getId());
        response.setAthleteId(session.getAthleteId());
        response.setTrainerId(session.getTrainerId());
        response.setFacilityId(session.getFacilityId());
        response.setResourceId(session.getResourceId());
        response.setSportId(session.getSportId());
        response.setStartTime(session.getStartTime());
        response.setEndTime(session.getEndTime());
        response.setStatus(session.getStatus());

        return response;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getAthleteId() {
        return athleteId;
    }

    public void setAthleteId(Long athleteId) {
        this.athleteId = athleteId;
    }

    public Long getTrainerId() {
        return trainerId;
    }

    public void setTrainerId(Long trainerId) {
        this.trainerId = trainerId;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
