package rs.etf.pia.sportsphere.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import rs.etf.pia.sportsphere.dto.CalendarEventResponse;
import rs.etf.pia.sportsphere.dto.FavoriteSportsUpdateRequest;
import rs.etf.pia.sportsphere.dto.ProfileUpdateRequest;
import rs.etf.pia.sportsphere.dto.ReservationRequest;
import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.Facility;
import rs.etf.pia.sportsphere.entity.Reservation;
import rs.etf.pia.sportsphere.entity.Resource;
import rs.etf.pia.sportsphere.entity.UserFavoriteSport;
import rs.etf.pia.sportsphere.repository.AppUserRepository;
import rs.etf.pia.sportsphere.repository.FacilityRepository;
import rs.etf.pia.sportsphere.repository.ReservationRepository;
import rs.etf.pia.sportsphere.repository.ResourceRepository;
import rs.etf.pia.sportsphere.repository.SportRepository;
import rs.etf.pia.sportsphere.repository.TrainingSessionRepository;
import rs.etf.pia.sportsphere.repository.UserFavoriteSportRepository;

@Service
@Transactional
public class AthleteService {
    
    private static final String ROLE_ATHLETE = "ATHLETE";

    private static final String FACILITY_STATUS_APPROVED = "APPROVED";

    private static final String RESERVATION_STATUS_REQUESTED = "REQUESTED";
    private static final String RESERVATION_STATUS_CANCELLED = "CANCELLED";
    private static final String RESERVATION_STATUS_NO_SHOW = "NO_SHOW";

    private final AppUserRepository appUserRepository;
    private final UserFavoriteSportRepository userFavoriteSportRepository;
    private final SportRepository sportRepository;
    private final ReservationRepository reservationRepository;
    private final FacilityRepository facilityRepository;
    private final ResourceRepository resourceRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final ProfileImageService profileImageService;

    public AthleteService(AppUserRepository appUserRepository, UserFavoriteSportRepository userFavoriteSportRepository,
            SportRepository sportRepository, ReservationRepository reservationRepository,
            FacilityRepository facilityRepository, ResourceRepository resourceRepository,
            TrainingSessionRepository trainingSessionRepository,
            ProfileImageService profileImageService) {
        this.appUserRepository = appUserRepository;
        this.userFavoriteSportRepository = userFavoriteSportRepository;
        this.sportRepository = sportRepository;
        this.reservationRepository = reservationRepository;
        this.facilityRepository = facilityRepository;
        this.resourceRepository = resourceRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.profileImageService = profileImageService;
    }

    private AppUser getUserOrThrow(Long userId){
        return  appUserRepository.findById(userId)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
    }

    private Facility getFacilityOrThrow(Long facilityId){
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
    }

    private Resource getResourceOrThrow(Long resourceId){
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    private Reservation getReservationOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
    }

    private void validateAthlete(AppUser user) {
        if(!ROLE_ATHLETE.equalsIgnoreCase(user.getRole())){
            throw new IllegalArgumentException("User is not an athlete");
        }

        if(!"ACTIVE".equalsIgnoreCase(user.getStatus())){
            throw new IllegalArgumentException("User account is not active");
        }
    }

    private void validateReservationRequest(ReservationRequest request) {
        if(request == null) {
            throw new IllegalArgumentException("Reservation request is required");
        }

        if(request.getFacilityId()  == null ||
            request.getResourceId() == null ||
            request.getSportId()    == null ||
            request.getStartTime()  == null ||
            request.getEndTime()    == null) {
            throw new IllegalArgumentException("All reservation fields are required");
        }

        if(!request.getEndTime().isAfter(request.getStartTime())) {
            throw new IllegalArgumentException("End time must be after start time");
        }

        Duration duration = Duration.between(request.getStartTime(), request.getEndTime());

        if(duration.toHours() < 1) {
            throw new IllegalArgumentException("Reservation must last at least one hour");
        }

        if(request.getStartTime().getMinute()   != 0 ||
            request.getStartTime().getSecond()  != 0 ||
            request.getStartTime().getNano()    != 0 ) {
            throw new IllegalArgumentException("Reservation must start on full hour");
        }

        if(request.getEndTime().getMinute()   != 0 ||
            request.getEndTime().getSecond()  != 0 ||
            request.getEndTime().getNano()    != 0 ) {
            throw new IllegalArgumentException("Reservation must end on full hour");
        }

        if(request.getStartTime().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reservation cannot start in past");
        }

        if(!request.getStartTime().toLocalDate().equals(request.getEndTime().toLocalDate())) {
            throw new IllegalArgumentException("Reservation must start and end on the same day");
        }
    }

    public AppUser getProfile(Long userId){
        AppUser user = getUserOrThrow(userId);
        validateAthlete(user);

        return user;
    }

    public AppUser updateProfile(Long userId, ProfileUpdateRequest request){
        AppUser user = getUserOrThrow(userId);
        validateAthlete(user);

        if(request == null) {
            throw new IllegalArgumentException("Profile update request is required");
        }

        if(request.getFirstName() != null) {
            user.setFirstName(request.getFirstName());
        }

        if(request.getLastName() != null) {
            user.setLastName(request.getLastName());
        }

        if(request.getPhone() != null) {
            user.setPhone(request.getPhone());
        }

        if(request.getEmail() != null) {
            user.setEmail(request.getEmail());
        }

        return appUserRepository.save(user);
    }

    public AppUser updateProfileImage(
            Long userId,
            MultipartFile image) {

        AppUser user = getUserOrThrow(userId);
        validateAthlete(user);

        String oldImagePath =
            user.getProfileImagePath();

        String newImagePath =
            profileImageService.saveRequired(image);

        user.setProfileImagePath(newImagePath);

        AppUser updatedUser =
            appUserRepository.save(user);

        profileImageService.deleteIfUploaded(
            oldImagePath
        );

        return updatedUser;
    }

    public AppUser removeProfileImage(Long userId) {
        AppUser user = getUserOrThrow(userId);
        validateAthlete(user);

        String oldImagePath =
            user.getProfileImagePath();

        user.setProfileImagePath(
            profileImageService.getDefaultImagePath()
        );

        AppUser updatedUser =
            appUserRepository.save(user);

        profileImageService.deleteIfUploaded(
            oldImagePath
        );

        return updatedUser;
    }

    public List<Long> getFavoriteSports(Long userId) {
        AppUser user = getUserOrThrow(userId);
        validateAthlete(user);

        return userFavoriteSportRepository.findByUserId(userId)
                .stream()
                .map(UserFavoriteSport::getSportId)
                .toList();
    }

    public List<Long> updateFavoriteSports(Long userId, FavoriteSportsUpdateRequest request) {
        AppUser user = getUserOrThrow(userId);
        validateAthlete(user);

        if(request == null || request.getSportIds() == null) {
            throw new IllegalArgumentException("Sport list is required");
        }

        List<Long> sportIds = request.getSportIds()
                                    .stream()
                                    .distinct()
                                    .toList();

        if(sportIds.size() > 5) {
            throw new IllegalArgumentException("User can have at most five favorite sports");
        }

        for(Long sportId : sportIds) {
            if(sportId == null) {
                throw new IllegalArgumentException("Sport id cannot be null");
            }

            if(!sportRepository.existsById(sportId)) {
                throw new IllegalArgumentException("Sport does not exist: " + sportId);
            }
        }

        userFavoriteSportRepository.deleteByUserId(userId);

        for(Long sportId: sportIds) {
            UserFavoriteSport favoriteSport = new UserFavoriteSport(userId, sportId);
            userFavoriteSportRepository.save(favoriteSport);
        }

        return sportIds;
    }

    public List<Reservation> getReservations(Long userId) {
        AppUser user = getUserOrThrow(userId);
        validateAthlete(user);

        return reservationRepository.findByAthleteIdOrderByStartTimeDesc(userId);
    }

    public Reservation createReservation(Long userId, ReservationRequest request){
        AppUser user = getUserOrThrow(userId);
        validateAthlete(user);

        validateReservationRequest(request);

        Facility facility = getFacilityOrThrow(request.getFacilityId());

        if(!FACILITY_STATUS_APPROVED.equalsIgnoreCase(facility.getStatus())) {
            throw new IllegalArgumentException("Facility is not approved");
        }

        long noShowCount =
            reservationRepository.countByAthleteIdAndFacilityIdAndStatus(
                userId,
                facility.getId(),
                RESERVATION_STATUS_NO_SHOW
            );

        Integer allowedNoShows = facility.getAllowedNoShows();

        if(allowedNoShows != null &&
            allowedNoShows > 0 &&
            noShowCount >= allowedNoShows
        ) {
            throw new IllegalArgumentException(
                "You are blocked from making reservations at this facility due to no-shows"
            );
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
            throw new IllegalArgumentException("Reservation is outside facility working hours");
        }

        List<Reservation> overlaps = reservationRepository.findOverlappingReservations(
            request.getResourceId(),
            request.getStartTime(),
            request.getEndTime()
        );

        if(!overlaps.isEmpty()){
            throw new IllegalArgumentException("Selected time interval is already reserved");
        }

        if(!trainingSessionRepository.findOverlappingResourceSessions(
            request.getResourceId(),
            request.getStartTime(),
            request.getEndTime()
        ).isEmpty()) {
            throw new IllegalArgumentException("Selected time interval has a training session");
        }

        Reservation reservation = new Reservation();
        
        reservation.setAthleteId(userId);
        reservation.setFacilityId(request.getFacilityId());
        reservation.setResourceId(request.getResourceId());
        reservation.setSportId(request.getSportId());
        reservation.setStartTime(request.getStartTime());
        reservation.setEndTime(request.getEndTime());
        reservation.setStatus(RESERVATION_STATUS_REQUESTED);

        return reservationRepository.save(reservation);
    }

    public Reservation cancelReservation(Long userId, Long reservationId) {
        AppUser user = getUserOrThrow(userId);
        validateAthlete(user);

        Reservation reservation = getReservationOrThrow(reservationId);

        if(!reservation.getAthleteId().equals(userId)) {
            throw new IllegalArgumentException("Reservation does not belong to this athlete");
        }

        if(!RESERVATION_STATUS_REQUESTED.equals(reservation.getStatus())) {
            throw new IllegalArgumentException(
                "Only requested reservations can be cancelled"
            );
        }

        if(RESERVATION_STATUS_CANCELLED.equals(reservation.getStatus())) {
            return reservation;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime cancellationDeadline = now.plusHours(12);

        if(reservation.getStartTime().isBefore(cancellationDeadline)) {
            throw new IllegalArgumentException("Reservation can be cancelled only 12 or more hours before start");
        }

        reservation.setStatus(RESERVATION_STATUS_CANCELLED);

        return reservationRepository.save(reservation);
    }

    public List<CalendarEventResponse> getResourceReservations(Long resourceId) {
        getResourceOrThrow(resourceId);

        List<CalendarEventResponse> events = new java.util.ArrayList<>();

        reservationRepository
            .findByResourceIdOrderByStartTimeAsc(resourceId)
            .stream()
            .filter(reservation ->
                !RESERVATION_STATUS_CANCELLED.equalsIgnoreCase(reservation.getStatus())
            )
            .map(CalendarEventResponse::fromReservation)
            .forEach(events::add);

        trainingSessionRepository
            .findByResourceIdOrderByStartTimeAsc(resourceId)
            .stream()
            .filter(session ->
                !"CANCELLED".equalsIgnoreCase(session.getStatus())
            )
            .map(CalendarEventResponse::fromTrainingSession)
            .forEach(events::add);

        events.sort(
            java.util.Comparator.comparing(CalendarEventResponse::getStartTime)
        );

        return events;
    }
}
