package rs.etf.pia.sportsphere.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import rs.etf.pia.sportsphere.dto.TrainingSessionRequest;
import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.Facility;
import rs.etf.pia.sportsphere.entity.Resource;
import rs.etf.pia.sportsphere.entity.Trainer;
import rs.etf.pia.sportsphere.entity.TrainingSession;
import rs.etf.pia.sportsphere.repository.AppUserRepository;
import rs.etf.pia.sportsphere.repository.FacilityRepository;
import rs.etf.pia.sportsphere.repository.ReservationRepository;
import rs.etf.pia.sportsphere.repository.ResourceRepository;
import rs.etf.pia.sportsphere.repository.TrainerRepository;
import rs.etf.pia.sportsphere.repository.TrainingSessionRepository;

@Service
@Transactional
public class TrainingService {
    
    private static final String ROLE_ATHLETE = "ATHLETE";
    private static final String USER_STATUS_ACTIVE = "ACTIVE";
    
    private static final String FACILITY_STATUS_APPROVED = "APPROVED";
    
    private static final String TRAINING_STATUS_SCHEDULED = "SCHEDULED";

    private AppUserRepository appUserRepository;
    private FacilityRepository facilityRepository;
    private ResourceRepository resourceRepository;
    private ReservationRepository reservationRepository;
    private TrainerRepository trainerRepository;
    private TrainingSessionRepository trainingSessionRepository;

    public TrainingService(AppUserRepository appUserRepository, FacilityRepository facilityRepository,
            ResourceRepository resourceRepository, ReservationRepository reservationRepository,
            TrainerRepository trainerRepository, TrainingSessionRepository trainingSessionRepository) {
        this.appUserRepository = appUserRepository;
        this.facilityRepository = facilityRepository;
        this.resourceRepository = resourceRepository;
        this.reservationRepository = reservationRepository;
        this.trainerRepository = trainerRepository;
        this.trainingSessionRepository = trainingSessionRepository;
    }

    private AppUser getAthleteOrThrow(Long userId) {
        return appUserRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Athlete not found"));
    }

    private Facility getFacilityOrThrow(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
    }

    private Trainer getTrainerOrThrow(Long trainerId) {
        return trainerRepository.findById(trainerId)
                .orElseThrow(() -> new IllegalArgumentException("Trainer not found"));
    }

    private Resource getResourceOrThrow(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    private void validateAthlete(AppUser athlete) {
        if(!ROLE_ATHLETE.equalsIgnoreCase(athlete.getRole())) {
            throw new IllegalArgumentException("User is not an athlete");
        }

        if(!USER_STATUS_ACTIVE.equalsIgnoreCase(athlete.getStatus())) {
            throw new IllegalArgumentException("Athlete account is not active");
        }
    }

    private void validateFacility(Facility facility) {
        if(!FACILITY_STATUS_APPROVED.equalsIgnoreCase(facility.getStatus())){
            throw new IllegalArgumentException("Facility is not approved");
        }
    }

    private void validateTrainingRequest(TrainingSessionRequest request) {
        if(request == null) {
            throw new IllegalArgumentException("Training session request is required");
        }

        if(request.getTrainerId() == null   ||
            request.getFacilityId() == null ||
            request.getResourceId() == null ||
            request.getSportId() == null    ||
            request.getStartTime() == null  ||
            request.getEndTime() == null
        ) {
                throw new IllegalArgumentException("All training session fields are required");
        }

        if(!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());

        if(duration.toMinutes() < 60) {
            throw new IllegalArgumentException("Training session must be at least one hour");
        }

        if(request.getStartTime().getMinute() != 0 ||
            request.getStartTime().getSecond() != 0 ||
            request.getStartTime().getNano() != 0 ||
            request.getEndTime().getMinute() != 0 ||
            request.getEndTime().getSecond() != 0 ||
            request.getEndTime().getNano() != 0
        ) {
            throw new IllegalArgumentException("Training session must start and end on a full hour");
        }

        if(!request.getStartTime().toLocalDate().equals(request.getEndTime().toLocalDate())) {
            throw new IllegalArgumentException("Training session must start and end on the same day");
        }

        if(request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Training session cannot start in the past");
        }
    }

    public List<Trainer> getTrainers(Long facilityId, Long sportId) {
        return trainerRepository.searchActiveTrainers(facilityId, sportId);
    }

    public TrainingSession createTrainingSession(Long userId, TrainingSessionRequest request) {
        AppUser athlete = getAthleteOrThrow(userId);
        validateAthlete(athlete);

        validateTrainingRequest(request);

        Facility facility = getFacilityOrThrow(request.getFacilityId());
        validateFacility(facility);

        Trainer trainer = getTrainerOrThrow(request.getTrainerId());

        if(!Boolean.TRUE.equals(trainer.getActive())) {
            throw new IllegalArgumentException("Trainer is not active");
        }

        if(!trainer.getFacilityId().equals(facility.getId())) {
            throw new IllegalArgumentException("Trainer does not belong to selected facility");
        }

        if(!trainer.getSportId().equals(request.getSportId())) {
            throw new IllegalArgumentException("Trainer does not support selected sport");
        }

        Resource resource = getResourceOrThrow(request.getResourceId());

        if(!Boolean.TRUE.equals(resource.getActive())) {
            throw new IllegalArgumentException("Resource is not active");
        }

        if(!resource.getFacilityId().equals(request.getFacilityId())) {
            throw new IllegalArgumentException("Resource does not belong to selected facility");
        }

        if(!resource.getSportId().equals(request.getSportId())) {
            throw new IllegalArgumentException("Resource does not support selected sport");
        }

        if(request.getStartTime().toLocalTime().isBefore(facility.getWorkingTimeFrom()) ||
            request.getEndTime().toLocalTime().isAfter(facility.getWorkingTimeTo())
        ) {
            throw new IllegalArgumentException("Training session is outside facility working hours");
        }

        List<TrainingSession> trainerOverlaps =
            trainingSessionRepository.findOverlappingTrainerSessions(
                request.getTrainerId(), 
                request.getStartTime(), 
                request.getEndTime()
            );

        if(!trainerOverlaps.isEmpty()) {
            throw new IllegalArgumentException("Trainer is already booked for selected time");
        }

        if(!reservationRepository.findOverlappingReservations(
            request.getResourceId(),
            request.getStartTime(),
            request.getEndTime()
        ).isEmpty()) {
            throw new IllegalArgumentException("Selected resource is already reserved");
        }

        if(!trainingSessionRepository.findOverlappingResourceSessions(
            request.getResourceId(),
            request.getStartTime(),
            request.getEndTime()
        ).isEmpty()) {
            throw new IllegalArgumentException("Selected resource already has a training session");
        }

        TrainingSession session = new TrainingSession();

        session.setAthleteId(userId);
        session.setTrainerId(request.getTrainerId());
        session.setFacilityId(request.getFacilityId());
        session.setResourceId(request.getResourceId());
        session.setSportId(request.getSportId());
        session.setStartTime(request.getStartTime());
        session.setEndTime(request.getEndTime());
        session.setPricePerHour(trainer.getPricePerHour());
        session.setStatus(TRAINING_STATUS_SCHEDULED);

        return trainingSessionRepository.save(session);
    }

    public List<TrainingSession> getTrainingSessions(Long userId) {
        AppUser athlete = getAthleteOrThrow(userId);
        validateAthlete(athlete);

        return trainingSessionRepository.findByAthleteIdOrderByStartTimeDesc(userId);
    }
}
