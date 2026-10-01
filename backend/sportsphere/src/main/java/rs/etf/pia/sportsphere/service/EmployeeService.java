package rs.etf.pia.sportsphere.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import rs.etf.pia.sportsphere.dto.CalendarEventResponse;
import rs.etf.pia.sportsphere.dto.CalendarMoveRequest;
import rs.etf.pia.sportsphere.dto.FacilityRequest;
import rs.etf.pia.sportsphere.dto.ProfileUpdateRequest;
import rs.etf.pia.sportsphere.entity.AppUser;
import rs.etf.pia.sportsphere.entity.Facility;
import rs.etf.pia.sportsphere.entity.OrganizationEmployee;
import rs.etf.pia.sportsphere.entity.Reservation;
import rs.etf.pia.sportsphere.entity.Resource;
import rs.etf.pia.sportsphere.entity.TrainingSession;
import rs.etf.pia.sportsphere.repository.AppUserRepository;
import rs.etf.pia.sportsphere.repository.FacilityRepository;
import rs.etf.pia.sportsphere.repository.OrganizationEmployeeRepository;
import rs.etf.pia.sportsphere.repository.ReservationRepository;
import rs.etf.pia.sportsphere.repository.ResourceRepository;
import rs.etf.pia.sportsphere.repository.TrainingSessionRepository;

@Service
@Transactional
public class EmployeeService {
    
    private static final String ROLE_EMPLOYEE = "EMPLOYEE";
    private static final String USER_STATUS_ACTIVE = "ACTIVE";

    private static final String FACILITY_STATUS_PENDING = "PENDING";

    private static final String RESERVATION_STATUS_REQUESTED = "REQUESTED";
    private static final String RESERVATION_STATUS_CONFIRMED = "CONFIRMED";
    private static final String RESERVATION_STATUS_CANCELLED = "CANCELLED";
    private static final String RESERVATION_STATUS_NO_SHOW = "NO_SHOW";

    private static final String TRAINING_STATUS_SCHEDULED = "SCHEDULED";
    private static final String TRAINING_STATUS_HELD = "HELD";
    private static final String TRAINING_STATUS_CANCELLED = "CANCELLED";
    private static final String TRAINING_STATUS_NO_SHOW = "NO_SHOW";

    private static final String RESOURCE_TYPE_HALL = "HALL";
    private static final String RESOURCE_TYPE_INDOOR_COURT = "INDOOR_COURT";

    private final AppUserRepository appUserRepository;
    private final OrganizationEmployeeRepository organizationEmployeeRepository;
    private final FacilityRepository facilityRepository;
    private final ReservationRepository reservationRepository;
    private final ResourceRepository resourceRepository;
    private final TrainingSessionRepository trainingSessionRepository;
    private final ProfileImageService profileImageService;

    public EmployeeService(AppUserRepository appUserRepository,
            OrganizationEmployeeRepository organizationEmployeeRepository, FacilityRepository facilityRepository,
            ReservationRepository reservationRepository, ResourceRepository resourceRepository,
            TrainingSessionRepository trainingSessionRepository,
            ProfileImageService profileImageService) {
        this.appUserRepository = appUserRepository;
        this.organizationEmployeeRepository = organizationEmployeeRepository;
        this.facilityRepository = facilityRepository;
        this.reservationRepository = reservationRepository;
        this.resourceRepository = resourceRepository;
        this.trainingSessionRepository = trainingSessionRepository;
        this.profileImageService = profileImageService;
    }

    private AppUser getEmployeeOrThrow(Long employeeId) {
        return appUserRepository.findById(employeeId)
                .orElseThrow(() -> new IllegalArgumentException("Employee not found"));
    }

    private Facility getFacilityOrThrow(Long facilityId) {
        return facilityRepository.findById(facilityId)
                .orElseThrow(() -> new IllegalArgumentException("Facility not found"));
    }

    private Reservation getReservationOrThrow(Long reservationId) {
        return reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found"));
    }

    private Resource getResourceOrThrow(Long resourceId) {
        return resourceRepository.findById(resourceId)
                .orElseThrow(() -> new IllegalArgumentException("Resource not found"));
    }

    private TrainingSession getTrainingSessionOrThrow(Long sessionId) {
        return trainingSessionRepository.findById(sessionId)
                .orElseThrow(() -> new IllegalArgumentException("Training session not found"));
    }

    private void validateEmployee(AppUser employee){
        if(!ROLE_EMPLOYEE.equalsIgnoreCase(employee.getRole())) {
            throw new IllegalArgumentException("User is not an employee");
        }

        if(!USER_STATUS_ACTIVE.equalsIgnoreCase(employee.getStatus())) {
            throw new IllegalArgumentException("Employee account is not active");
        }
    }

    private List<Long> getEmployeeOrganizationIds(Long employeeId){
        return organizationEmployeeRepository.findByEmployeeId(employeeId)
                .stream()
                .map(OrganizationEmployee::getOrganizationId)
                .distinct()
                .toList();
    }

    private void validateFacilityRequest(FacilityRequest request) {
        if(request == null) {
            throw new IllegalArgumentException("Facility request is required");
        }

        if(request.getName() == null || request.getName().isBlank()) {
            throw new IllegalArgumentException("Facility name is required");
        }

        if(request.getCity() == null || request.getCity().isBlank()) {
            throw new IllegalArgumentException("Facility city is required");
        }

        if(request.getAddress() == null || request.getAddress().isBlank()) {
            throw new IllegalArgumentException("Facility address is required");
        }

        if(request.getPricePerHour() == null || request.getPricePerHour().signum() <= 0) {
            throw new IllegalArgumentException("Facility price per hour must be positive");
        }

        if(request.getWorkingTimeFrom() == null || request.getWorkingTimeTo() == null){
            throw new IllegalArgumentException("Facility working time is required");
        }

        if(!request.getWorkingTimeFrom().isBefore(request.getWorkingTimeTo())) {
            throw new IllegalArgumentException("Working time from must be before working time to");
        }

        if(request.getAllowedNoShows() == null || request.getAllowedNoShows() < 0 ) {
            throw new IllegalArgumentException("Allowed no-shows must be zero or positive");
        }
    }

    private void validateReservationBelongsToEmployee(Long employeeId, Reservation reservation) {
        Facility facility = getFacilityOrThrow(reservation.getFacilityId());
        validateEmployeeCanManageFacility(employeeId, facility);
    }

    private void validateTrainingBelongsToEmployee(Long employeeId, TrainingSession session) {
        Facility facility = getFacilityOrThrow(session.getFacilityId());
        validateEmployeeCanManageFacility(employeeId, facility);
    }

    private void validateReservationProcessingTime(Reservation reservation) {
        LocalDateTime now = LocalDateTime.now();

        LocalDateTime startTime = reservation.getStartTime();
        LocalDateTime deadline = startTime.plusMinutes(10);

        if(now.isBefore(startTime) || now.isAfter(deadline)) {
            throw new IllegalArgumentException("Reservation can be processed only from start time until 10 minutes after start");
        }
    }

    private void validateReservationCanBeProcessed(Reservation reservation) {
        if(!RESERVATION_STATUS_REQUESTED.equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalArgumentException("Only requested reservations can be processed");
        }
    }

    private void validateTrainingProcessingTime(TrainingSession session) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime deadline = session.getStartTime().plusMinutes(10);

        if(now.isBefore(session.getStartTime()) || now.isAfter(deadline)) {
            throw new IllegalArgumentException(
                "Training can be processed only from start time until 10 minutes after start"
            );
        }
    }

    private void validateTrainingCanBeProcessed(TrainingSession session) {
        if(!TRAINING_STATUS_SCHEDULED.equalsIgnoreCase(session.getStatus())) {
            throw new IllegalArgumentException("Only scheduled trainings can be processed");
        }
    }

    private LocalDateTime validateCalendarMove(
        Long employeeId,
        Long facilityId,
        Long resourceId,
        LocalDateTime oldStart,
        LocalDateTime oldEnd,
        LocalDateTime newStart
    ) {
        if(newStart == null) {
            throw new IllegalArgumentException("New start time is required");
        }

        Facility facility = getFacilityOrThrow(facilityId);
        validateEmployeeCanManageFacility(employeeId, facility);

        Resource resource = getResourceOrThrow(resourceId);

        if(!Boolean.TRUE.equals(resource.getActive())) {
            throw new IllegalArgumentException("Resource is not active");
        }

        if(!resource.getFacilityId().equals(facilityId)) {
            throw new IllegalArgumentException("Resource does not belong to facility");
        }

        boolean indoor =
            RESOURCE_TYPE_HALL.equalsIgnoreCase(resource.getType()) ||
            RESOURCE_TYPE_INDOOR_COURT.equalsIgnoreCase(resource.getType());

        if(!indoor) {
            throw new IllegalArgumentException(
                "Only events in halls and indoor courts can be moved"
            );
        }

        if(!newStart.isAfter(LocalDateTime.now())) {
            throw new IllegalArgumentException("Event cannot be moved into the past");
        }

        if(newStart.getMinute() != 0 ||
            newStart.getSecond() != 0 ||
            newStart.getNano() != 0
        ) {
            throw new IllegalArgumentException("Event must start on a full hour");
        }

        Duration duration = Duration.between(oldStart, oldEnd);

        if(duration.isZero() || duration.isNegative()) {
            throw new IllegalArgumentException("Event duration is invalid");
        }

        LocalDateTime newEnd = newStart.plus(duration);

        if(!newStart.toLocalDate().equals(newEnd.toLocalDate())) {
            throw new IllegalArgumentException("Event must end on the same day");
        }

        if(newStart.toLocalTime().isBefore(facility.getWorkingTimeFrom()) ||
            newEnd.toLocalTime().isAfter(facility.getWorkingTimeTo())
        ) {
            throw new IllegalArgumentException("New interval is outside facility working hours");
        }

        return newEnd;
    }

    private Long resolveEmployeeOrganizationId(Long employeeId){
        List<Long> organizationIds = getEmployeeOrganizationIds(employeeId);

        if(organizationIds.isEmpty()) {
            throw new IllegalArgumentException("Employee is not assigned to any organization");
        }

        if(organizationIds.size() > 1) {
            throw new IllegalArgumentException("Employee is assigned to multiple organizations");
        }

        return organizationIds.get(0);
    }

    private void validateEmployeeCanManageFacility(Long employeeId, Facility facility) {
        List<Long> organizationIds = getEmployeeOrganizationIds(employeeId);

        if(!organizationIds.contains(facility.getOrganizationId())) {
            throw new IllegalArgumentException("Employee cannot manage this facility");
        }
    }

    public AppUser getProfile(Long employeeId) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        return employee;
    }
    
    public AppUser updateProfile(Long employeeId, ProfileUpdateRequest request) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        if(request == null) {
            throw new IllegalArgumentException("Profile update request is required");
        }

        if(request.getFirstName() != null) {
            employee.setFirstName(request.getFirstName());
        }

        if(request.getLastName() != null) {
            employee.setLastName(request.getLastName());
        }

        if(request.getPhone() != null) {
            employee.setPhone(request.getPhone());
        }

        if(request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }

        return appUserRepository.save(employee);
    }

    public AppUser updateProfileImage(
            Long employeeId,
            MultipartFile image) {

        AppUser employee =
            getEmployeeOrThrow(employeeId);

        validateEmployee(employee);

        String oldImagePath =
            employee.getProfileImagePath();

        String newImagePath =
            profileImageService.saveRequired(image);

        employee.setProfileImagePath(newImagePath);

        AppUser updatedEmployee =
            appUserRepository.save(employee);

        profileImageService.deleteIfUploaded(
            oldImagePath
        );

        return updatedEmployee;
    }

    public AppUser removeProfileImage(
            Long employeeId) {

        AppUser employee =
            getEmployeeOrThrow(employeeId);

        validateEmployee(employee);

        String oldImagePath =
            employee.getProfileImagePath();

        employee.setProfileImagePath(
            profileImageService.getDefaultImagePath()
        );

        AppUser updatedEmployee =
            appUserRepository.save(employee);

        profileImageService.deleteIfUploaded(
            oldImagePath
        );

        return updatedEmployee;
    }

    public List<Facility> getFacilities(Long employeeId) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        List<Long> organizationIds = getEmployeeOrganizationIds(employeeId);

        if(organizationIds.isEmpty()) {
            return List.of();
        }

        return facilityRepository.findByOrganizationIdIn(organizationIds);
    }

    public Facility createFacility(Long employeeId, FacilityRequest request) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        validateFacilityRequest(request);

        Long organizationId = resolveEmployeeOrganizationId(employeeId);

        Facility facility = new Facility();

        facility.setOrganizationId(organizationId);

        facility.setName(request.getName());
        facility.setCity(request.getCity());
        facility.setAddress(request.getAddress());
        facility.setDescription(request.getDescription());
        facility.setPricePerHour(request.getPricePerHour());
        facility.setWorkingTimeFrom(request.getWorkingTimeFrom());
        facility.setWorkingTimeTo(request.getWorkingTimeTo());
        facility.setAllowedNoShows(request.getAllowedNoShows());

        facility.setStatus(FACILITY_STATUS_PENDING);
        facility.setLikeCount(0);
        facility.setDislikeCount(0);
        facility.setCreatedAt(LocalDateTime.now());

        return facilityRepository.save(facility);
    }

    public Facility updateFacility(Long employeeId, Long facilityId, FacilityRequest request) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);
    
        Facility facility = getFacilityOrThrow(facilityId);
        validateEmployeeCanManageFacility(employeeId, facility);

        validateFacilityRequest(request);

        facility.setName(request.getName());
        facility.setCity(request.getCity());
        facility.setAddress(request.getAddress());
        facility.setDescription(request.getDescription());
        facility.setPricePerHour(request.getPricePerHour());
        facility.setWorkingTimeFrom(request.getWorkingTimeFrom());
        facility.setWorkingTimeTo(request.getWorkingTimeTo());
        facility.setAllowedNoShows(request.getAllowedNoShows());

        return facilityRepository.save(facility);
    }

    public List<Facility> importFacilitiesFromJson(Long employeeId, List<FacilityRequest> requests) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        if(requests == null || requests.isEmpty()) {
            throw new IllegalArgumentException("JSON file does not contain facilities");
        }

        List<Facility> savedFacilities = new ArrayList<>();

        for(FacilityRequest request : requests) {
            savedFacilities.add(createFacility(employeeId, request));
        }

        return savedFacilities;
    }

    public List<Reservation> getReservations(Long employeeId) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        List<Facility> facilities = getFacilities(employeeId);

        List<Reservation> reservations = new ArrayList<>();

        for(Facility facility : facilities) {
            reservations.addAll(
                reservationRepository.findByFacilityIdOrderByStartTimeDesc(facility.getId())
            );
        }

        reservations.sort(
            Comparator.comparing(Reservation::getStartTime).reversed()
        );

        return reservations;
    }

    public Reservation confirmReservation(Long employeeId, Long reservationId) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        Reservation reservation = getReservationOrThrow(reservationId);

        validateReservationBelongsToEmployee(employeeId, reservation);
        validateReservationCanBeProcessed(reservation);
        validateReservationProcessingTime(reservation);

        reservation.setStatus(RESERVATION_STATUS_CONFIRMED);

        return reservationRepository.save(reservation);
    }

    public Reservation noShowReservation(Long employeeId, Long reservationId) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        Reservation reservation = getReservationOrThrow(reservationId);

        validateReservationBelongsToEmployee(employeeId, reservation);
        validateReservationCanBeProcessed(reservation);
        validateReservationProcessingTime(reservation);

        reservation.setStatus(RESERVATION_STATUS_NO_SHOW);

        return reservationRepository.save(reservation);
    }

    public List<TrainingSession> getTrainingSessions(Long employeeId) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        List<Facility> facilities = getFacilities(employeeId);
        List<TrainingSession> sessions = new ArrayList<>();

        for(Facility facility : facilities) {
            sessions.addAll(
                trainingSessionRepository.findByFacilityIdOrderByStartTimeDesc(facility.getId())
            );
        }

        sessions.sort(
            Comparator.comparing(TrainingSession::getStartTime).reversed()
        );

        return sessions;
    }

    public TrainingSession confirmTrainingSession(Long employeeId, Long sessionId) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        TrainingSession session = getTrainingSessionOrThrow(sessionId);

        validateTrainingBelongsToEmployee(employeeId, session);
        validateTrainingCanBeProcessed(session);
        validateTrainingProcessingTime(session);

        session.setStatus(TRAINING_STATUS_HELD);

        return trainingSessionRepository.save(session);
    }

    public TrainingSession noShowTrainingSession(Long employeeId, Long sessionId) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        TrainingSession session = getTrainingSessionOrThrow(sessionId);

        validateTrainingBelongsToEmployee(employeeId, session);
        validateTrainingCanBeProcessed(session);
        validateTrainingProcessingTime(session);

        session.setStatus(TRAINING_STATUS_NO_SHOW);

        return trainingSessionRepository.save(session);
    }

    public CalendarEventResponse moveReservation(
        Long employeeId,
        Long reservationId,
        CalendarMoveRequest request
    ) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        if(request == null) {
            throw new IllegalArgumentException("Move request is required");
        }

        Reservation reservation = getReservationOrThrow(reservationId);
        validateReservationBelongsToEmployee(employeeId, reservation);

        if(!RESERVATION_STATUS_REQUESTED.equalsIgnoreCase(reservation.getStatus())) {
            throw new IllegalArgumentException("Only requested reservations can be moved");
        }

        LocalDateTime newStart = request.getStartTime();
        LocalDateTime newEnd = validateCalendarMove(
            employeeId,
            reservation.getFacilityId(),
            reservation.getResourceId(),
            reservation.getStartTime(),
            reservation.getEndTime(),
            newStart
        );

        if(!reservationRepository.findOverlappingReservationsExcluding(
            reservation.getResourceId(),
            reservation.getId(),
            newStart,
            newEnd
        ).isEmpty()) {
            throw new IllegalArgumentException("Selected time interval is already reserved");
        }

        if(!trainingSessionRepository.findOverlappingResourceSessions(
            reservation.getResourceId(),
            newStart,
            newEnd
        ).isEmpty()) {
            throw new IllegalArgumentException("Selected time interval has a training session");
        }

        reservation.setStartTime(newStart);
        reservation.setEndTime(newEnd);

        return CalendarEventResponse.fromReservation(
            reservationRepository.save(reservation)
        );
    }

    public CalendarEventResponse moveTrainingSession(
        Long employeeId,
        Long sessionId,
        CalendarMoveRequest request
    ) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        if(request == null) {
            throw new IllegalArgumentException("Move request is required");
        }

        TrainingSession session = getTrainingSessionOrThrow(sessionId);
        validateTrainingBelongsToEmployee(employeeId, session);

        if(!TRAINING_STATUS_SCHEDULED.equalsIgnoreCase(session.getStatus())) {
            throw new IllegalArgumentException("Only scheduled trainings can be moved");
        }

        LocalDateTime newStart = request.getStartTime();
        LocalDateTime newEnd = validateCalendarMove(
            employeeId,
            session.getFacilityId(),
            session.getResourceId(),
            session.getStartTime(),
            session.getEndTime(),
            newStart
        );

        if(!reservationRepository.findOverlappingReservations(
            session.getResourceId(),
            newStart,
            newEnd
        ).isEmpty()) {
            throw new IllegalArgumentException("Selected time interval is already reserved");
        }

        if(!trainingSessionRepository.findOverlappingResourceSessionsExcluding(
            session.getResourceId(),
            session.getId(),
            newStart,
            newEnd
        ).isEmpty()) {
            throw new IllegalArgumentException("Selected resource already has a training session");
        }

        if(!trainingSessionRepository.findOverlappingTrainerSessionsExcluding(
            session.getTrainerId(),
            session.getId(),
            newStart,
            newEnd
        ).isEmpty()) {
            throw new IllegalArgumentException("Trainer is already booked for selected time");
        }

        session.setStartTime(newStart);
        session.setEndTime(newEnd);

        return CalendarEventResponse.fromTrainingSession(
            trainingSessionRepository.save(session)
        );
    }

    public List<CalendarEventResponse> getCalendar(Long employeeId) {
        AppUser employee = getEmployeeOrThrow(employeeId);
        validateEmployee(employee);

        List<Facility> facilities = getFacilities(employeeId);
        List<CalendarEventResponse> events = new ArrayList<>();

        for(Facility facility : facilities) {
            reservationRepository
                .findByFacilityIdOrderByStartTimeDesc(facility.getId())
                .stream()
                .filter(reservation ->
                    !RESERVATION_STATUS_CANCELLED.equalsIgnoreCase(reservation.getStatus())
                )
                .map(CalendarEventResponse::fromReservation)
                .forEach(events::add);

            trainingSessionRepository
                .findByFacilityIdOrderByStartTimeDesc(facility.getId())
                .stream()
                .filter(session ->
                    !TRAINING_STATUS_CANCELLED.equalsIgnoreCase(session.getStatus())
                )
                .map(CalendarEventResponse::fromTrainingSession)
                .forEach(events::add);
        }

        events.sort(
            Comparator.comparing(CalendarEventResponse::getStartTime)
        );

        return events;
    }
}
